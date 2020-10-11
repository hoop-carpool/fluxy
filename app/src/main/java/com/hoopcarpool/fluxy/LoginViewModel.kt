package com.hoopcarpool.fluxy

import com.hoopcarpool.fluxy.LoginViewModel.LoginSideEffect
import com.hoopcarpool.fluxy.LoginViewModel.LoginSideEffect.GoHome
import com.hoopcarpool.fluxy.LoginViewModel.LoginSideEffect.LoginError
import com.hoopcarpool.fluxy.LoginViewModel.LoginViewState
import com.hoopcarpool.viewmodel.FluxyViewModel
import com.hoopcarpool.viewmodel.SideEffect
import kotlinx.coroutines.launch

/**
 * Created by Daniel S on 10/10/2020.
 */
class LoginViewModel(
    private val dispatcher: Dispatcher,
    private val loginStore: LoginStore,
    private val accountStore: AccountStore
) : FluxyViewModel<LoginViewState, LoginSideEffect>() {

    fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            SequenceBuilder()
                .next(
                    origin = {
                        postState(LoginViewState(loading = true))
                        dispatcher.dispatch(LoginAction(username, password))
                        loginStore.onConcluded { it.loginResult }
                    },
                    onSuccess = { dispatcher.dispatch(LoadAccountAction(username)) },
                    onFailure = { LoginError(it.exception).publish() }
                )
                .next(
                    origin = { accountStore.onConcluded { it.accountResult } },
                    onSuccess = { GoHome().publish() },
                    onFailure = { LoginError(it.exception).publish() }
                )
                .onFinally { postState(LoginViewState(loading = false)) }.build()
        }
    }

    data class LoginViewState(val loading: Boolean = false)

    sealed class LoginSideEffect : SideEffect {
        class GoHome : LoginSideEffect()
        class LoginError(val exception: Throwable?) : LoginSideEffect()
    }
}