package com.hoopcarpool.fluxy

import com.hoopcarpool.timberlogger.TimberLogger
import timber.log.Timber

/**
 * Created by Daniel S on 10/10/2020.
 */
class DI {
    companion object {
        var dispatcher: Dispatcher = Dispatcher(TimberLogger())
        var loginController: LoginController
        var loginStore: LoginStore

        var accountController: AccountController
        var accountStore: AccountStore

        init {
            loginController = LoginController(dispatcher)
            loginStore = LoginStore(loginController)

            accountController = AccountController(dispatcher)
            accountStore = AccountStore(accountController)

            dispatcher.stores = listOf(loginStore, accountStore)

            Timber.plant(Timber.DebugTree())
        }
    }
}