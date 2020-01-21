package com.hoopcarpool.fluxy

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

open class FluxyStore<S : Any> {

    protected val reducers = ReducerMap<S>()

    companion object {
        val NO_STATE = Any()
    }

    private var _state: Any? = NO_STATE

    val state: S
        get() {
            if (_state === NO_STATE) {
                synchronized(this) {
                    if (_state === NO_STATE) {
                        _state = initialState()
                    }
                }
            }
            return _state as S
        }

    private val channel = BroadcastChannel<S>(Channel.BUFFERED)

    fun flow(hotStart: Boolean = true): Flow<S> = channel
        .asFlow()
        .onStart {
            if (hotStart) {
                emit(state)
            }
        }

    suspend inline fun observe(hotStart: Boolean = true, crossinline block: (S) -> Unit) {
        flow(hotStart).collect {
            withContext(Dispatchers.Main) { block(it) }
        }
    }

    private fun performStateChange(newState: Any): Boolean {
        if (newState != _state) {
            _state = newState
            channel.offer(newState as S)
            return true
        }
        return false
    }

    open fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<S>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }

    inline fun <reified T : BaseAction> reduce(noinline block: (T) -> S) {
        reducers.addNew(T::class, block)
    }

    inline fun <reified T : AsyncAction> asyncReduce(noinline block: suspend (T) -> S) {
        reducers.addNew(T::class) { t: T -> runBlocking { block(t) } }
    }

    fun canHandle(action: BaseAction): Boolean = reducers.map.containsKey(action::class)

    fun dispatch(action: BaseAction): S? {
        synchronized(this) {
            var stateReduced: S? = null

            reducers.map[action::class]?.let { reducer ->
                val newState = reducer(action)
                if (performStateChange(newState))
                    stateReduced = newState
            }

            return stateReduced
        }
    }

    protected class ReducerMap<S> {

        val map: MutableMap<KClass<*>, (BaseAction) -> S> = mutableMapOf()

        fun <T : BaseAction> addNew(clazz: KClass<T>, cb: (T) -> S) {
            map[clazz] = cb as (BaseAction) -> S
        }
    }
}
