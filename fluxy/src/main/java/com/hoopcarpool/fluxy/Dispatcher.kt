package com.hoopcarpool.fluxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Dispatcher {

    var stores: List<FluxyStore<*>> = emptyList()
        set(value) {
            field = value
            interceptors = listOf(
                LogInterceptor(DefaultLogger()),
                StoreInterceptor(value.toList())
            )
        }

    private var interceptors = emptyList<FluxyInterceptor>()

    fun dispatch(action: BaseAction) {
        RealFluxyChain(interceptors).proceed(action)
    }

    suspend fun dispatchAsync(action: AsyncAction) {
        withContext(Dispatchers.IO) {
            RealFluxyChain(interceptors).proceed(action)
        }
    }
}
