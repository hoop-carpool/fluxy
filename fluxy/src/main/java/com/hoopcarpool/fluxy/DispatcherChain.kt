package com.hoopcarpool.fluxy

typealias StoresChanges = Pair<FluxyStore<*>, Any>

interface FluxyChain {
    val action: BaseAction
    fun proceed(): List<StoresChanges>
}

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