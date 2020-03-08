package com.hoopcarpool.fluxy

/**
 * Contains the new state that belongs to a [FluxyStore]
 */
data class StoresChanges(val store: FluxyStore<*>, val newState: Any)

interface FluxyChain {
    val action: BaseAction
    fun proceed(): List<StoresChanges>
}

/**
 * Basic Chain to give an [BaseAction] to each interceptor
 */
class RealFluxyChain(private val interceptors: List<FluxyInterceptor>) : FluxyChain {

    override lateinit var action: BaseAction

    private var index = 0

    fun proceed(action: BaseAction): List<StoresChanges> {
        this.action = action
        index = 0
        return proceed()
    }

    override fun proceed(): List<StoresChanges> {
        return interceptors[index++].intercept(this)
    }
}
