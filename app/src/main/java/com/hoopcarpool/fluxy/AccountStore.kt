package com.hoopcarpool.fluxy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Created by Daniel S on 10/10/2020.
 */

data class LoadAccountAction(val username: String) : BaseAction
data class LoadAccountResultAction(val accountResult: Result<Account>) : BaseAction

data class AccountState(
    val accountResult: Result<Account> = Result.Empty()
)

class AccountStore(private val accountController: AccountController) : FluxyStore<AccountState>() {

    override fun init() {
        reduce<LoadAccountAction> {
            state.copy(accountResult = Result.Loading()).asNewState()
            accountController.fetchAccount(it.username)
        }

        reduce<LoadAccountResultAction> {
            state.copy(accountResult = it.accountResult).asNewState()
        }
    }
}

class AccountController(private val dispatcher: Dispatcher) {
    fun fetchAccount(username: String) {
        GlobalScope.launch {
            kotlinx.coroutines.delay(1337) // Simulate an API Call
            val loginApiResultSuccess = Random.nextBoolean()

            if (loginApiResultSuccess) {
                dispatcher.dispatch(LoadAccountResultAction(Result.Success(Account("Kaladin", true))))
            } else {
                dispatcher.dispatch(LoadAccountResultAction(Result.Failure(exception = Exception("No account for username = $username"))))
            }
        }
    }
}
data class Account(val name: String, val active: Boolean)