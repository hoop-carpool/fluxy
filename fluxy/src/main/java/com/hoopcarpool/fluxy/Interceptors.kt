package com.hoopcarpool.fluxy

interface FluxyInterceptor {
    fun intercept(chain: FluxyChain): List<StoresChanges>
}

class LogInterceptor(private val logger: Logger) : FluxyInterceptor {

    override fun intercept(chain: FluxyChain): List<StoresChanges> {

        val action = chain.action

        if (action is AsyncAction) {
            logger.i(
                " \n" +
                        """ 
                ┌────────────────────────────────────────────
                ├─> ${action::class.simpleName} = $action
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
                ├─> ${action::class.simpleName} [${totalTime}ms] = $action

            """.trimIndent()

        storesChangedList.forEach { (store, state) ->
            msg += "│   ${store.javaClass.simpleName} = ${state}\n"
        }

        msg += "└────────────────────────────────────────────"

        logger.i(msg)
        return storesChangedList
    }
}

class StoreInterceptor(private val stores: List<FluxyStore<*>>) : FluxyInterceptor {

    override fun intercept(chain: FluxyChain): List<StoresChanges> {
        val storesChanged = mutableListOf<StoresChanges>()
        stores.forEach { store ->
            if (store.canHandle(chain.action)) {
                val newState = store.dispatch(chain.action)
                if (newState != null) storesChanged.add(StoresChanges(store, newState))
            }
        }

        return storesChanged
    }
}
