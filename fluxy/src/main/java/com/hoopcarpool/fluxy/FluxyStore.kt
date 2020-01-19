package com.hoopcarpool.fluxy

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking

open class FluxyStore<S : Any> {

    protected val reducers = ReducerMap<S>()
    protected val asyncReducers = AsyncReducerMap<S>()

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

    /** Hook for write only property */
    var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) = performStateChange(value)

    private fun performStateChange(newState: Any) {
        if (newState != _state) {
            _state = newState
            channel.offer(newState as S)
        }
    }

    open fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                as Class<S>
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
        asyncReducers.addNewAsync(T::class, block)
    }

    fun canHandle(action: BaseAction): Boolean =
        reducers.map.containsKey(action::class) || asyncReducers.map.containsKey(action::class)

    fun dispatch(action: BaseAction): S? {
        synchronized(this) {
            reducers.map.forEach { (key, value) ->
                if (action::class == key) {
                    newState = value(action)
                }
            }

            asyncReducers.map.forEach { (key, value) ->
                if (action::class == key) {
                    runBlocking {
                        newState = value(action)
                    }
                }
            }
            return state
        }
    }

    protected class ReducerMap<S> {

        val map: MutableMap<KClass<*>, (BaseAction) -> S> = mutableMapOf()

        fun <T : BaseAction> addNew(clazz: KClass<T>, cb: (T) -> S) {
            map[clazz] = cb as (BaseAction) -> S
        }
    }

    protected class AsyncReducerMap<S> {

        val map: MutableMap<KClass<*>, suspend (BaseAction) -> S> = mutableMapOf()

        fun <T : AsyncAction> addNewAsync(clazz: KClass<T>, cb: suspend (T) -> S) {
            map[clazz] = cb as suspend (BaseAction) -> S
        }
    }
}
