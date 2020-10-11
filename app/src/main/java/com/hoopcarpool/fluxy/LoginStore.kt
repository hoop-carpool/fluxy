package com.hoopcarpool.fluxy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Created by Daniel S on 10/10/2020.
 */

data class LoginAction(val username: String, val password: String) : BaseAction
data class LoginResultAction(val loginResult: Result<String>) : BaseAction

data class LoginState(
    val loginResult: Result<String> = Result.Empty()
)

class LoginStore(private val loginController: LoginController) : FluxyStore<LoginState>() {

    override fun init() {
        reduce<LoginAction> {
            state.copy(loginResult = Result.Loading()).asNewState()
            loginController.doLogin(it.username, it.password)
        }

        reduce<LoginResultAction> {
            state.copy(loginResult = it.loginResult).asNewState()
        }
    }
}

class LoginController(private val dispatcher: Dispatcher) {
    fun doLogin(username: String, password: String) {
        GlobalScope.launch {
            kotlinx.coroutines.delay(1337) // Simulate an API Call
            val loginApiResultSuccess = Random.nextBoolean()

            if (loginApiResultSuccess) {
                dispatcher.dispatch(LoginResultAction(Result.Success("ASDFGHJKLQWERTYUIOP")))
            } else {
                dispatcher.dispatch(LoginResultAction(Result.Failure(exception = Exception("Invalid username = $username"))))
            }
        }
    }
}
