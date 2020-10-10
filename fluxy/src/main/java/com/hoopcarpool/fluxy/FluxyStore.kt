package com.hoopcarpool.fluxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 * Base store.
 *
 * Contains a [state]
 *
 * Subscribe to [BaseAction] via [reduce]
 */
abstract class FluxyStore<S : Any> {

    /**
     * Map that contains the reducers for each [BaseAction]
     */
    val reducers: MutableMap<KClass<*>, (BaseAction) -> Unit> = mutableMapOf()

    /** Var por testing purposes  */
    var initTime: Long = 0

    val state: S
        get() = stateFlow.value

    /** Hook for write only property */
    var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) {
            performStateChange(value)
        }

    /** Extension for setting a state as a new state */
    fun S.asNewState() = performStateChange(this)

    private val stateFlow = MutableStateFlow(initialState())

    /**
     * Returns the [channel] as a flow
     *
     * If [hotStart] is true, emits the current [state]
     */
    fun flow(hotStart: Boolean = true): Flow<S> = stateFlow.drop(count = if(hotStart) 0 else 1).distinctUntilChanged()

    abstract fun init()

    /**
     * Utility function that emit state changes at Main thread
     */
    suspend inline fun observe(hotStart: Boolean = true, crossinline block: (S) -> Unit) {
        flow(hotStart).collect {
            withContext(Dispatchers.Main) { block(it) }
        }
    }

    private fun performStateChange(newState: S): Boolean {
        if (newState != state) stateFlow.value = newState
        return newState != state
    }

    @Suppress("UNCHECKED_CAST")
    private fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<S>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }

    inline fun <reified A : BaseAction> reduce(noinline block: (A) -> Unit) {
        reducers.addNew(A::class, block)
    }

    @FluxyPreview
    inline fun <reified A : AsyncAction> asyncReduce(noinline block: suspend (A) -> Unit) {
        reducers.addNew(A::class) { t: A -> runBlocking { block(t) } }
    }

    /** Returns if the store has a reducer for a [BaseAction] */
    fun canHandle(action: BaseAction): Boolean = reducers.containsKey(action::class)

    /**
     * Execute the reducer for a given [BaseAction]
     *
     * There's only one reducer per [BaseAction] per [FluxyStore]
     */
    var reducing = false
    fun dispatch(action: BaseAction): S? {
        return synchronized(this) {
            if (reducing) throw CyclicActionDispatchException(action, this)

            reducing = true
            val oldState = state
            reducers[action::class]?.let { reducer -> reducer(action) }
            reducing = false

            if (oldState != state) state else null
        }
    }

    /**
     * Holder for reducers functions
     *
     * A reducer it's a function that given a [BaseAction] returns a [state]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseAction> MutableMap<KClass<*>, (BaseAction) -> Unit>.addNew(clazz: KClass<T>, cb: (T) -> Unit) {
        if (this[clazz] != null) throw DuplicateReducerException("$clazz")
        this[clazz] = cb as (BaseAction) -> Unit
    }
}
