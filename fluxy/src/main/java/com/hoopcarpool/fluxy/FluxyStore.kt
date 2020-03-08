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

/**
 * Base store.
 *
 * Contains a [state]
 *
 * Subscribe to [BaseAction] via [reduce]
 */
abstract class FluxyStore<S : Any> {

    val reducers = ReducerMap<S>()

    companion object {
        val NO_STATE = Any()
    }

    val initTime: Long

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

    /** Hook for write only property */
    var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) {
            performStateChange(value)
        }

    private val channel = BroadcastChannel<S>(Channel.BUFFERED)

    /**
     * Returns the [channel] as a flow
     *
     * If [hotStart] is true, emits the current [state]
     */
    fun flow(hotStart: Boolean = true): Flow<S> = channel
        .asFlow()
        .onStart {
            if (hotStart) {
                emit(state)
            }
        }

    init {
        val startTime = System.currentTimeMillis()
        init()
        initTime = System.currentTimeMillis() - startTime
    }

    protected abstract fun init()

    /**
     * Utility function that emit state changes at Main thread
     */
    suspend inline fun observe(hotStart: Boolean = true, crossinline block: (S) -> Unit) {
        flow(hotStart).collect {
            withContext(Dispatchers.Main) { block(it) }
        }
    }

    private fun performStateChange(newState: S): Boolean {
        if (newState != _state) {
            _state = newState
            channel.offer(newState)
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

    @FluxyPreview
    inline fun <reified T : AsyncAction> asyncReduce(noinline block: suspend (T) -> S) {
        reducers.addNew(T::class) { t: T -> runBlocking { block(t) } }
    }

    fun canHandle(action: BaseAction): Boolean = reducers.map.containsKey(action::class)

    /**
     * Execute the reducer for a given [action]
     *
     * There's only one reducer per [action] per [FluxyStore]
     */
    fun dispatch(action: BaseAction): S? {
        return synchronized(this) {
            var stateReduced: S? = null

            reducers.map[action::class]?.let { reducer ->
                val newState = reducer(action)
                if (performStateChange(newState))
                    stateReduced = newState
            }

            stateReduced
        }
    }

    /**
     * Holder for reducers functions
     *
     * A reducer it's a function that given a [BaseAction] returns a [state]
     */
    class ReducerMap<S> {

        val map: MutableMap<KClass<*>, (BaseAction) -> S> = mutableMapOf()

        fun <T : BaseAction> addNew(clazz: KClass<T>, cb: (T) -> S) {
            if (map[clazz] != null) throw UnsupportedOperationException("Reducer already exists for $clazz at this store")
            map[clazz] = cb as (BaseAction) -> S
        }
    }
}
