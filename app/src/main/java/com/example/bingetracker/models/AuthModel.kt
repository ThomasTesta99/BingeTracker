package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sign

class AuthModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState : StateFlow<AuthState> = _authState

    fun signUp(email: String, password: String){
        _authState.value = AuthState.Loading

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Log.e("AuthModel", "Email or password is empty")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Success
                    signIn(email, password)
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error unknown")
                }
            }
    }

    fun signIn(email: String, password: String){
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Success
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error unknown")
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