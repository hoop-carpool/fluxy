package com.hoopcarpool.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import java.lang.reflect.ParameterizedType

/**
 * Base class to model a viewmodel that holds an state and a side effects channel
 */
open class FluxyViewModel<VIEW_STATE, SIDE_EFFECT : SideEffect> {

    private val job: Job = SupervisorJob()
    val viewModelScope = CoroutineScope(job + Dispatchers.IO)

    private val mainJob: Job = SupervisorJob()
    val mainScope = CoroutineScope(job + Dispatchers.Main)

    private val sideEffectChannel = Channel<SIDE_EFFECT>(Channel.UNLIMITED)
    val sideEffectFlow = sideEffectChannel.consumeAsFlow().flowOn(Dispatchers.Main)

    val stateFlow = MutableStateFlow(initialState())

    fun publishSideEffect(sideEffect: SIDE_EFFECT) {
        sideEffectChannel.offer(sideEffect)
    }

    fun SIDE_EFFECT.publish() {
        publishSideEffect(this)
    }

    fun postState(state: VIEW_STATE) {
        stateFlow.value = state
    }

    fun onCleared() {
        viewModelScope.cancel()
    }

    @Suppress("UNCHECKED_CAST")
    private fun initialState(): VIEW_STATE {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VIEW_STATE>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }
}

interface SideEffect