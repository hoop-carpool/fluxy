package com.hoopcarpool.fluxy

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class TestAction(val content: String, val delay: Long = 0) : BaseAction
data class TestTwoAction(val content: String, val delay: Long = 0) : BaseAction
data class TestOneAction(val content: String, val delay: Long = 0) : BaseAction
data class TestNoAction(val content: String, val delay: Long = 0) : BaseAction

data class TestState(val content: String = "initial")

class TestStoreOne : FluxyStore<TestState>() {

    override fun init() {
        reduce<TestAction> {
            runBlocking { delay(it.delay) }
            state.copy(content = it.content).asNewState()
        }

        reduce<TestOneAction> {
            runBlocking { delay(it.delay) }
            state.copy(content = it.content).asNewState()
        }
    }
}

class TestStoreTwo : FluxyStore<TestState>() {

    override fun init() {
        reduce<TestAction> {
            runBlocking { delay(it.delay) }
            state.copy(content = it.content).asNewState()
        }

        reduce<TestTwoAction> {
            runBlocking { delay(it.delay) }
            state.copy(content = it.content).asNewState()
        }
    }
}
