package com.app.manga.ui.screens.signinscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manga.data.local.datastore.DataStoreRepository
import kotlinx.coroutines.launch

data class SignInState(
    val email: String = "",
    val password: String = ""
)
class SignInViewModel(
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {
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
    fun signIn(){
        viewModelScope.launch {
            if (email != null && password != null){
                dataStoreRepository.saveCredentials(email, password)
            }
        }
    }
}