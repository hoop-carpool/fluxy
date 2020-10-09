package com.hoopcarpool.fluxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Call each store inside [stores] the action dispatched via [dispatch] or [dispatchAsync]
 */
class Dispatcher(private val logger: Logger = DefaultLogger()) {

    /**
     * List of [FluxyStore]s
     *
     * Only needed for initialize [StoreInterceptor]
     */
    var stores: List<FluxyStore<*>> = emptyList()
        set(value) {
            field = value
            interceptors = listOf(
                LogInterceptor(logger),
                StoreInterceptor(logger, value)
            )

            stores.initAll()
            logInit()
        }

    private var interceptors = emptyList<FluxyInterceptor>()

    /**
     * Send the [action] to each store inside [stores]
     */
    fun dispatch(action: BaseAction) {
        RealFluxyChain(interceptors).proceed(action)
    }

    /**
     * Send the [action] to each store inside [stores]
     */
    @FluxyPreview
    suspend fun dispatchAsync(action: AsyncAction) {
        withContext(Dispatchers.IO) {
            RealFluxyChain(interceptors).proceed(action)
        }
    }

    private fun logInit() {
        val storesLog = stores.joinToString("\n") {
            "├-> ${it::class.java.simpleName} with state = ${it.state} in ${it.initTime}ms"
        }

        val msg = " \n" + """
            ══════════════════════════════════════════════════════════════
            
            
               ▄████████  ▄█       ███    █▄  ▀████    ▐████▀ ▄██   ▄   
              ███    ███ ███       ███    ███   ███▌   ████▀  ███   ██▄ 
              ███    █▀  ███       ███    ███    ███  ▐███    ███▄▄▄███ 
             ▄███▄▄▄     ███       ███    ███    ▀███▄███▀    ▀▀▀▀▀▀███ 
            ▀▀███▀▀▀     ███       ███    ███    ████▀██▄     ▄██   ███ 
              ███        ███       ███    ███   ▐███  ▀███    ███   ███ 
              ███        ███▌    ▄ ███    ███  ▄███     ███▄  ███   ███ 
              ███        █████▄▄██ ████████▀  ████       ███▄  ▀█████▀  
                         ▀
                         
            ══════════════════════════════════════════════════════════════
                        
            Dispatcher was initialized with this stores:
            
            ┌────────────────────────────────────────
            $storesLog
            └────────────────────────────────────────
    
            in case of emergency press: ▲,▲,▼,▼,◄,►,◄,►,(B),(A),[Start]
            
        """.trimIndent()

        logger.d(msg)
    }
}
