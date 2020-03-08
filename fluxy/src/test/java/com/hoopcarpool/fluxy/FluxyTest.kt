package com.hoopcarpool.fluxy

/* ktlint-disable no-wildcard-imports */
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/* ktlint-enable no-wildcard-imports */

class FluxyTest {

    data class TestAction(val content: String, val delay: Long = 0) : BaseAction
    data class TestTwoAction(val content: String, val delay: Long = 0) : BaseAction
    data class TestOneAction(val content: String, val delay: Long = 0) : BaseAction
    data class TestNoAction(val content: String, val delay: Long = 0) : BaseAction

    data class TestState(val content: String = "initial")

    class TestStoreOne : FluxyStore<TestState>() {

        override fun init() {
            reduce<TestAction> {
                runBlocking { delay(it.delay) }
                state.copy(content = it.content)
            }

            reduce<TestOneAction> {
                runBlocking { delay(it.delay) }
                state.copy(content = it.content)
            }
        }
    }

    class TestStoreTwo : FluxyStore<TestState>() {

        override fun init() {
            reduce<TestAction> {
                runBlocking { delay(it.delay) }
                state.copy(content = it.content)
            }

            reduce<TestTwoAction> {
                runBlocking { delay(it.delay) }
                state.copy(content = it.content)
            }
        }
    }

    @get:Rule
    var repeatRule = RepeatRule()

    var storeOne = TestStoreOne()
    var storeTwo = TestStoreTwo()

    lateinit var dispatcher: Dispatcher

    @Before
    fun setup() {
        storeOne = TestStoreOne()
        storeTwo = TestStoreTwo()
        dispatcher = Dispatcher().apply {
            stores = listOf(storeOne, storeTwo)
        }
    }

    @Test
    @Repeat(10)
    fun `initial state`() {
        Assert.assertTrue(storeOne.state.content == "initial")
        Assert.assertTrue(storeTwo.state.content == "initial")
    }

    @Test
    @Repeat(10)
    fun `dispatch action change one store`() {
        dispatcher.dispatch(TestOneAction("test"))
        Assert.assertTrue(storeOne.state.content == "test")
        Assert.assertTrue(storeTwo.state.content == "initial")
    }

    @Test
    @Repeat(10)
    fun `dispatch action change two store`() {
        dispatcher.dispatch(TestTwoAction("test"))
        Assert.assertTrue(storeTwo.state.content == "test")
        Assert.assertTrue(storeOne.state.content == "initial")
    }

    @Test
    @Repeat(10)
    fun `dispatch action change one and two store`() {
        dispatcher.dispatch(TestAction("test"))
        Assert.assertTrue(storeOne.state.content == "test")
        Assert.assertTrue(storeTwo.state.content == "test")
    }

    @Test
    @Repeat(10)
    fun `dispatch action change none store`() {
        dispatcher.dispatch(TestNoAction("test"))
        Assert.assertTrue(storeOne.state.content == "initial")
        Assert.assertTrue(storeTwo.state.content == "initial")
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `dispatch multiple action change state`() {
        runBlocking {
            val states = mutableListOf<TestState>()
            val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                storeOne.flow(false).take(3).collect {
                    states.add(it)
                }
            }

            dispatcher.dispatch(TestOneAction("test1"))
            dispatcher.dispatch(TestOneAction("test2"))
            dispatcher.dispatch(TestOneAction("test3"))

            Assert.assertTrue(storeOne.state.content == "test3")
            Assert.assertTrue(storeTwo.state.content == "initial")

            job.join()

            Assert.assertTrue(states.size == 3)
            Assert.assertTrue(states[0].content == "test1")
            Assert.assertTrue(states[1].content == "test2")
            Assert.assertTrue(states[2].content == "test3")
        }
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `dispatch wait for all stores to finish`() {
        val states = mutableListOf<TestState>()
        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            storeOne.flow(false).take(1).collect {
                states.add(it)
            }
        }

        val job2 = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            storeTwo.flow(false).take(1).collect {
                states.add(it)
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            dispatcher.dispatch(TestOneAction("test1"))
        }
        GlobalScope.launch(Dispatchers.IO) {
            dispatcher.dispatch(TestTwoAction("test1", 500))
        }

        runBlocking {
            job.join()
            job2.join()

            Assert.assertTrue(storeOne.state.content == "test1")
            Assert.assertTrue(storeTwo.state.content == "test1")

            Assert.assertTrue(states.size == 2)
        }
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `multi dispatch`() {
        runBlocking {
            val job = GlobalScope.launch {
                dispatcher.dispatch(TestOneAction("test1", 500))
            }

            Assert.assertFalse(storeOne.state.content == "test1")

            delay(50)

            dispatcher.dispatch(TestOneAction("test2"))
            job.join()

            Assert.assertTrue(storeOne.state.content == "test2")
        }
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `conditional dispatch`() {

        dispatcher.dispatch(TestOneAction("test1"))

        dispatcher.dispatch(TestOneAction(storeOne.state.content + "1"))

        Assert.assertTrue(storeOne.state.content == "test11")
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `one store dont block other`() {
        runBlocking {
            GlobalScope.launch {
                dispatcher.dispatch(TestOneAction("test1", 5000))
            }

            Assert.assertFalse(storeOne.state.content == "test1")

            delay(50)

            dispatcher.dispatch(TestTwoAction("test2"))

            Assert.assertTrue(storeTwo.state.content == "test2")
        }
    }

    @Test(timeout = 1000)
    @Repeat(10)
    fun `one store dont block others action`() {
        runBlocking {
            GlobalScope.launch {
                dispatcher.dispatch(TestOneAction("test1", 500))
            }

            Assert.assertFalse(storeOne.state.content == "test1")

            delay(50)

            dispatcher.dispatch(TestAction("test2"))

            Assert.assertTrue(storeTwo.state.content == "test2")
        }
    }

    @Test(timeout = 10000)
    @Repeat(10)
    fun `hot flow emit before state`() {
        runBlocking {
            val states = mutableListOf<TestState>()

            dispatcher.dispatch(TestOneAction("test"))

            storeOne.flow(true).take(1).collect {
                states.add(it)
            }

            Assert.assertTrue(states.size == 1)
            Assert.assertTrue(states[0].content == "test")
        }
    }

    @Test(timeout = 10000)
    @Repeat(10)
    fun `cold flow don't emit before state`() {
        runBlocking {
            val states = mutableListOf<TestState>()

            dispatcher.dispatch(TestOneAction("test"))

            GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                storeOne.flow(false).take(1).collect {
                    states.add(it)
                }
            }

            Assert.assertTrue(states.size == 0)
            Assert.assertTrue(storeOne.state.content == "test")
        }
    }
}
