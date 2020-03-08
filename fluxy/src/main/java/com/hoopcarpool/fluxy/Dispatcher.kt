package com.hoopcarpool.fluxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Dispatcher(private val logger: Logger = DefaultLogger()) {

    var stores: List<FluxyStore<*>> = emptyList()
        set(value) {
            field = value
            interceptors = listOf(
                LogInterceptor(logger),
                StoreInterceptor(value.toList())
            )

            logInit()
        }

    private var interceptors = emptyList<FluxyInterceptor>()

    fun dispatch(action: BaseAction) {
        RealFluxyChain(interceptors).proceed(action)
    }

    @FluxyPreview
    suspend fun dispatchAsync(action: AsyncAction) {
        withContext(Dispatchers.IO) {
            RealFluxyChain(interceptors).proceed(action)
        }
    }

    private fun logInit() {
        logger.d("Dispatcher initialized with this stores:")
        stores.forEach {
            logger.d("|-> ${it::class.java.simpleName} with state = ${it.state} in ${it.initTime}ms")
        }
    }
}
