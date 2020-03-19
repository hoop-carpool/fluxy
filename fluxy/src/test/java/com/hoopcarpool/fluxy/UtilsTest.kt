package com.hoopcarpool.fluxy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UtilsTest {

    @get:Rule
    val scope = CoroutineTestRule()

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

    @Test(timeout = 10000)
    @Repeat(10)
    fun testMerge() {

        val emittedItems = mutableListOf<Pair<String, String>>()

        val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(scope.testDispatcher) {
                mergeStates<String> {
                    merge(storeOne) { content }
                    merge(storeTwo) { content }
                }.flowOn(scope.testDispatcher)
                    .take(10)
                    .collect {
                        emittedItems += it[0] to it[1]
                        println(it.joinToString())
                    }
            }
        }

        dispatcher.dispatch(TestAction("1"))
        dispatcher.dispatch(TestTwoAction("2"))
        dispatcher.dispatch(TestAction("3"))
        dispatcher.dispatch(TestTwoAction("4"))
        dispatcher.dispatch(TestOneAction("5"))
        dispatcher.dispatch(TestAction("6"))

        runBlocking {
            job.join()
        }

        Assert.assertEquals(10, emittedItems.size)
        Assert.assertEquals("initial" to "initial", emittedItems[0])
        Assert.assertEquals("1" to "initial", emittedItems[1])
        Assert.assertEquals("1" to "1", emittedItems[2])
        Assert.assertEquals("1" to "2", emittedItems[3])
        Assert.assertEquals("3" to "2", emittedItems[4])
        Assert.assertEquals("3" to "3", emittedItems[5])
        Assert.assertEquals("3" to "4", emittedItems[6])
        Assert.assertEquals("5" to "4", emittedItems[7])
        Assert.assertEquals("6" to "4", emittedItems[8])
        Assert.assertEquals("6" to "6", emittedItems[9])
    }
}
