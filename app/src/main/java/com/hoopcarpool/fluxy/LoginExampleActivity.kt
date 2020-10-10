package com.hoopcarpool.fluxy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hoopcarpool.timberlogger.TimberLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

class LoginExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_example)

        Timber.plant(Timber.DebugTree())

        val dispatcher = Dispatcher(TimberLogger())
        val loginController = LoginController(dispatcher)
        val loginStore = LoginStore(loginController)
        dispatcher.stores = listOf(loginStore)

        GlobalScope.launch {
            loginStore.observe {
                Timber.d(it.toString())
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
