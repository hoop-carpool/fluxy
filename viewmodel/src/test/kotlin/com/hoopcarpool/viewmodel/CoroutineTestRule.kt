package com.hoopcarpool.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CoroutineTestRule : TestRule {

    val testDispatcher = TestCoroutineDispatcher()

    override fun apply(base: Statement, description: Description?) = object : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(testDispatcher)
            base.evaluate()
            Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
            testDispatcher.cleanupTestCoroutines()
        }
    }
}
