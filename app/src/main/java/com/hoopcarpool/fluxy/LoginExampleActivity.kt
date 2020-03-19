package com.hoopcarpool.fluxy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class LoginExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_example)

        val dispatcher = Dispatcher()
        val loginController = LoginController(dispatcher)
        val loginStore = LoginStore(loginController)
        dispatcher.stores = listOf(loginStore)
        listOf(loginStore).initAll()

        GlobalScope.launch {
            loginStore.observe {
                when (val result = it.loginResult) {
                    is Result.Success -> {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        findViewById<Button>(R.id.doLogin).isEnabled = true
                        Toast.makeText(applicationContext, "Success!! your token is: ${result.value}", Toast.LENGTH_SHORT).show()
                        // Here we could navigate to another screen
                    }
                    is Result.Loading -> {
                        findViewById<Button>(R.id.doLogin).isEnabled = false
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    }
                    is Result.Failure -> {
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        findViewById<Button>(R.id.doLogin).isEnabled = true
                        Toast.makeText(applicationContext, "An error has occurred: ${result.exception}", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Empty -> findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                }
            }
        }


        findViewById<Button>(R.id.doLogin).setOnClickListener {
            val username = findViewById<EditText>(R.id.username_edit_text).text.toString()
            val password = findViewById<EditText>(R.id.password_edit_text).text.toString()

            dispatcher.dispatch(LoginAction(username, password))
        }

    }
}

data class LoginAction(val username: String, val password: String) : BaseAction
data class LoginResultAction(val loginResult: Result<String>) : BaseAction

data class LoginState(
    val loginResult: Result<String> = Result.Empty()
)

class LoginStore(val loginController: LoginController) : FluxyStore<LoginState>() {

    override fun init() {
        reduce<LoginAction> {
            loginController.doLogin(it.username, it.password)
            state.copy(loginResult = Result.Loading())
        }

        reduce<LoginResultAction> {
            state.copy(loginResult = it.loginResult)
        }
    }
}

class LoginController(private val dispatcher: Dispatcher) {
    fun doLogin(username: String, password: String) {
        GlobalScope.launch {
            kotlinx.coroutines.delay(2000) // Simulate an API Call
            val loginApiResultSuccess = Random.nextBoolean()

            if (loginApiResultSuccess) {
                dispatcher.dispatch(LoginResultAction(Result.Success("ASDFGHJKLQWERTYUIOP")))
            } else {
                dispatcher.dispatch(LoginResultAction(Result.Failure(exception = Exception("Invalid username"))))
            }
        }
    }
}
