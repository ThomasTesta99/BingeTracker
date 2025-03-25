package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUserAuth = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUserAuth: StateFlow<FirebaseUser?> = _currentUserAuth

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private suspend fun createUserToFirestore(userId: String, name: String, email: String) {
        val userDocument = db.collection("users").document(userId)
        try {
            val document = userDocument.get().await()
            if (!document.exists()) {
                val user = User(userId, name, email) // Assuming User data class
                userDocument.set(user).await()
                Log.d("AuthModel", "User document created successfully")
            } else {
                Log.d("AuthModel", "User document already exists")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to save user: ${e.localizedMessage}")
            Log.e("AuthModel", "Error creating user in Firestore", e)
        }
    }

    private suspend fun getUserFromDatabase(userId: String) {
        try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                _currentUser.value = document.toObject(User::class.java)
            } else {
                _authState.value = AuthState.Error("User not found")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to fetch user: ${e.localizedMessage}")
        }
    }

    private suspend fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Log.e("AuthModel", "Fields cannot be empty")
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid
            if (userId != null) {
                _currentUserAuth.value = auth.currentUser
                createUserToFirestore(userId, name, email)
                getUserFromDatabase(userId)
                _authState.value = AuthState.Success
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-up failed: ${e.localizedMessage}")
            Log.e("AuthModel", "Sign-up error", e)
        }
    }

    private suspend fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid
            if (userId != null) {
                _currentUserAuth.value = auth.currentUser
                getUserFromDatabase(userId)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Unexpected authentication error")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage}")
            Log.e("AuthModel", "Sign-in error", e)
        }
    }

    fun signInUser(email: String, password: String) {
        viewModelScope.launch { signIn(email, password) }
    }

    fun signUpUser(name: String, email: String, password: String) {
        viewModelScope.launch { signUp(name, email, password) }
    }

    fun logout() {
        auth.signOut()
        _currentUserAuth.value = null
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
