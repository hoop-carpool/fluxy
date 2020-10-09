package com.hoopcarpool.fluxy

interface FluxyInterceptor {
    fun intercept(chain: FluxyChain): List<StoresChanges>
}

/**
 * Log each action and the changes that it produced
 */
class LogInterceptor(private val logger: Logger) : FluxyInterceptor {

    override fun intercept(chain: FluxyChain): List<StoresChanges> {

        val action = chain.action

        if (action is AsyncAction) {
            logger.i(
                " \n" +
                    """ 
                ┌────────────────────────────────────────────
                │─> ${action::class.simpleName} = $action
                └────────────────────────────────────────────
                    """.trimIndent()
            )
        }

        val startTime = System.currentTimeMillis()

        val storesChangedList = chain.proceed()

        val totalTime = System.currentTimeMillis() - startTime

        var msg = " \n" +
            """ 
                ┌────────────────────────────────────────────
                │─> ${action::class.simpleName} [${totalTime}ms] = $action

            """.trimIndent()

        storesChangedList.forEach { (store, state) ->
            msg += "│   ${store.javaClass.simpleName} = ${state}\n"
        }

        msg += "└────────────────────────────────────────────"

        logger.i(msg)
        return storesChangedList
    }
}

/**
 * Final chain Interceptor that dispatch the action to each [FluxyStore]
 *
 * Returns a list with the [StoresChanges] produced
 */
class StoreInterceptor(private val logger: Logger, private val stores: List<FluxyStore<*>>) : FluxyInterceptor {

    override fun intercept(chain: FluxyChain): List<StoresChanges> {
        val storesChanged = mutableListOf<StoresChanges>()
        stores.forEach { store ->
            if (store.canHandle(chain.action)) {
                logger.d("Dispatching ${chain.action} on $store ")
                val newState = store.dispatch(chain.action)
                if (newState != null) storesChanged.add(StoresChanges(store, newState))
            }
        }

        return storesChanged
    }
}
