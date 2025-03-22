package com.example.bingetracker.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState : StateFlow<AuthState> = _signUpState

    fun signUp(email: String, password: String){
        _signUpState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _signUpState.value = AuthState.Success
                }else{
                    _signUpState.value = AuthState.Error(task.exception?.message ?: "Error unknown")
                }
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}