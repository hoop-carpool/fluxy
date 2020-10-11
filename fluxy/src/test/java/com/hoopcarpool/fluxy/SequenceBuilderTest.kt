package com.hoopcarpool.fluxy

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * Created by Daniel S on 10/10/2020.
 */
class SequenceBuilderTest {

    @get:Rule
    val scope = CoroutineTestRule()

    private var storeOne = TestStoreOne()
    private var storeTwo = TestStoreTwo()

    private lateinit var dispatcher: Dispatcher

    @Before
    fun setup() {
        storeOne = TestStoreOne()
        storeTwo = TestStoreTwo()
        val storeList = listOf(storeOne, storeTwo)
        dispatcher = Dispatcher().apply {
            stores = storeList
        }
    }

    @Test
    fun `simple block success`() {
        runBlocking {
            var finallyCalled = false
            SequenceBuilder().next(
                origin = {
                    dispatcher.dispatch(TestOneAction(Result.Success("test")))
                    storeOne.onConcluded { it.content }
                },
                onSuccess = {
                    assertEquals("test", it.value)
                },
                onFailure = {
                    fail()
                }
            ).onFinally {
                finallyCalled = true
            }.build()

            assertTrue(finallyCalled)
        }
    }

    @Test
    fun `simple block failure`() {
        runBlocking {
            var finallyCalled = false
            SequenceBuilder().next(
                origin = {
                    dispatcher.dispatch(TestOneAction(Result.Failure(InstantiationError())))
                    storeOne.onConcluded { it.content }
                },
                onSuccess = {
                    fail()
                },
                onFailure = {
                    assertTrue(it.exception is InstantiationError)
                }
            ).onFinally {
                finallyCalled = true
            }.build()

            assertTrue(finallyCalled)
        }
    }

    @Test
    fun `multi block success call next block`() {
        runBlocking {
            var finallyCalled = false
            SequenceBuilder().next(
                origin = {
                    dispatcher.dispatch(TestOneAction(Result.Success("test")))
                    storeOne.onConcluded { it.content }
                },
                onSuccess = {
                    assertEquals("test", it.value)
                },
                onFailure = {
                    fail()
                }
            ).next(
                origin = {
                    dispatcher.dispatch(TestTwoAction(Result.Success("test")))
                    storeTwo.onConcluded { it.content }
                },
                onSuccess = {
                    assertEquals("test", it.value)
                },
                onFailure = {
                    fail()
                }
            ).onFinally {
                finallyCalled = true
            }.build()

            assertTrue(finallyCalled)
        }
    }

    @Test
    fun `multi block failure dont next block`() {
        runBlocking {
            var finallyCalled = false
            SequenceBuilder().next(
                origin = {
                    dispatcher.dispatch(TestOneAction(Result.Failure(InstantiationError())))
                    storeOne.onConcluded { it.content }
                },
                onSuccess = {
                    fail()
                },
                onFailure = {
                    assertTrue(it.exception is InstantiationError)
                }
            ).next(
                origin = {
                    fail()
                    dispatcher.dispatch(TestTwoAction(Result.Success("test")))
                    storeTwo.onConcluded { it.content }
                },
                onSuccess = {
                    fail()
                },
                onFailure = {
                    fail()
                }
            ).onFinally {
                finallyCalled = true
            }.build()
            assertTrue(finallyCalled)
        }
    }

    private data class TestAction(val content: Result<String>, val delay: Long = 0) : BaseAction
    private data class TestTwoAction(val content: Result<String>, val delay: Long = 0) : BaseAction
    private data class TestOneAction(val content: Result<String>, val delay: Long = 0) : BaseAction
    private data class TestNoAction(val content: Result<String>, val delay: Long = 0) : BaseAction

    private data class TestState(val content: Result<String> = Result.Empty())

    private class TestStoreOne : FluxyStore<TestState>() {

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

    private class TestStoreTwo : FluxyStore<TestState>() {

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
}

