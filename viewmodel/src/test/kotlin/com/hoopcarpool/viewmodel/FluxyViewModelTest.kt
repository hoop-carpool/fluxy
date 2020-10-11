package com.hoopcarpool.viewmodel

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Daniel S on 11/10/2020.
 */
class FluxyViewModelTest {

    @get:Rule
    val scope = CoroutineTestRule()

    data class ViewState(val data: String = "")
    enum class ViewEffects : SideEffect {
        EFFECT_1, EFFECT_2, EFFECT_3
    }

    private lateinit var testViewModel: FluxyViewModel<ViewState, ViewEffects>

    @Before
    fun before() {
        testViewModel = object : FluxyViewModel<ViewState, ViewEffects>() {}
    }

    @Test
    fun `side effect emits after subscription`() {
        runBlocking {
            val sideEffectList = mutableListOf<ViewEffects>()
            testViewModel.mainScope.launch {
                testViewModel.sideEffectFlow.collect { sideEffectList.add(it) }
            }

            testViewModel.run { ViewEffects.EFFECT_1.publish() }

            assertEquals(1, sideEffectList.size)
        }
    }

    @Test
    fun `side effect emits before subscription`() {
        runBlocking {
            val sideEffectList = mutableListOf<ViewEffects>()

            testViewModel.run { ViewEffects.EFFECT_1.publish() }

            testViewModel.mainScope.launch {
                testViewModel.sideEffectFlow.collect { sideEffectList.add(it) }
            }

            assertEquals(1, sideEffectList.size)
            assertEquals(ViewEffects.EFFECT_1, sideEffectList[0])
        }
    }

    @Test
    fun `multiple side effect emits before subscription`() {
        runBlocking {
            val sideEffectList = mutableListOf<ViewEffects>()

            testViewModel.run {
                ViewEffects.EFFECT_1.publish()
                ViewEffects.EFFECT_2.publish()
            }

            testViewModel.mainScope.launch {
                testViewModel.sideEffectFlow.collect { sideEffectList.add(it) }
            }

            assertEquals(2, sideEffectList.size)
            assertEquals(ViewEffects.EFFECT_1, sideEffectList[0])
            assertEquals(ViewEffects.EFFECT_2, sideEffectList[1])

        }
    }

    @Test
    fun `multiple side effect emits before and after subscription`() {
        runBlocking {
            val sideEffectList = mutableListOf<ViewEffects>()

            testViewModel.run {
                ViewEffects.EFFECT_1.publish()
                ViewEffects.EFFECT_2.publish()
            }

            testViewModel.mainScope.launch {
                testViewModel.sideEffectFlow.collect { sideEffectList.add(it) }
            }

            assertEquals(2, sideEffectList.size)
            assertEquals(ViewEffects.EFFECT_1, sideEffectList[0])
            assertEquals(ViewEffects.EFFECT_2, sideEffectList[1])

            testViewModel.run { ViewEffects.EFFECT_3.publish() }

            assertEquals(3, sideEffectList.size)
            assertEquals(ViewEffects.EFFECT_3, sideEffectList[2])

            testViewModel.run { ViewEffects.EFFECT_1.publish() }


            assertEquals(4, sideEffectList.size)
            assertEquals(ViewEffects.EFFECT_1, sideEffectList[3])
        }
    }
}