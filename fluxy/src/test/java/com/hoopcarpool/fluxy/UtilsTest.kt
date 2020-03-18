package com.hoopcarpool.fluxy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
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

    @Test
    fun testMerge() =
        runBlocking {

            val emitedItems = mutableListOf<Pair<String, String>>()

            val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
                mergeStates<String> {
                    merge(storeOne) { content }
                    merge(storeTwo) { content }
                }
                    .take(10)
                    .collect {
                        emitedItems += it[0] to it[1]
                    }
            }

            dispatcher.dispatch(TestAction("1"))
            dispatcher.dispatch(TestTwoAction("2"))
            dispatcher.dispatch(TestAction("3"))
            dispatcher.dispatch(TestTwoAction("4"))
            dispatcher.dispatch(TestOneAction("5"))
            dispatcher.dispatch(TestAction("6"))

            job.join()

            Assert.assertEquals(10, emitedItems.size)
            Assert.assertEquals("initial" to "initial", emitedItems[0])
            Assert.assertEquals("1" to "initial", emitedItems[1])
            Assert.assertEquals("1" to "1", emitedItems[2])
            Assert.assertEquals("1" to "2", emitedItems[3])
            Assert.assertEquals("3" to "2", emitedItems[4])
            Assert.assertEquals("3" to "3", emitedItems[5])
            Assert.assertEquals("3" to "4", emitedItems[6])
            Assert.assertEquals("5" to "4", emitedItems[7])
            Assert.assertEquals("6" to "4", emitedItems[8])
            Assert.assertEquals("6" to "6", emitedItems[9])
        }
}
