package com.app.manga.ui.screens.signinscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class SignInState(
    val email: String = "",
    val password: String = ""
)
class SignInViewModel(): ViewModel() {
    var signInState = mutableStateOf(SignInState())
        private set

    private val email
        get () = signInState.value.email

    private val password
        get () = signInState.value.password
    fun onEmailChange(email: String) {
        signInState.value = signInState.value.copy(email = email)
    }
    fun onPasswordChange(password: String) {
        signInState.value = signInState.value.copy(password = password)

    }

}