package com.hoopcarpool.fluxy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hoopcarpool.fluxy.LoginViewModel.LoginSideEffect.GoHome
import com.hoopcarpool.fluxy.LoginViewModel.LoginSideEffect.LoginError
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_example)

        viewModel = LoginViewModel(DI.dispatcher, DI.loginStore, DI.accountStore)

        setup()
    }

    private fun setup() {
        findViewById<Button>(R.id.doLogin).setOnClickListener {
            val username = findViewById<EditText>(R.id.username_edit_text).text.toString()
            val password = findViewById<EditText>(R.id.password_edit_text).text.toString()
            viewModel.doLogin(username, password)
        }

        viewModel.stateFlow.onEach {
            findViewById<View>(R.id.progressBar).visibility = if (it.loading) View.VISIBLE else View.GONE
        }.launchIn(viewModel.mainScope)

        viewModel.sideEffectFlow.onEach {
            when (it) {
                is GoHome -> Toast.makeText(applicationContext, "Success!! Navigating to home...", Toast.LENGTH_SHORT).show()
                is LoginError -> Toast.makeText(applicationContext, "An error has occurred: ${it.exception}", Toast.LENGTH_SHORT).show()
            }
        }.launchIn(viewModel.mainScope)
    }
}
