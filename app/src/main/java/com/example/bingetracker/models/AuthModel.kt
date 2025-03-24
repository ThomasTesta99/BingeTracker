package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bingetracker.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sign

class AuthModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState : StateFlow<AuthState> = _authState

    private val _currentUserAuth = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUserAuth : StateFlow<FirebaseUser?> = _currentUserAuth

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser : StateFlow<User?> = _currentUser

    fun signUp(name: String, email: String, password: String){
        _authState.value = AuthState.Loading

        if (name.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Log.e("AuthModel", "Name or Email or password is empty")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        createUserToFirestore(userId, name, email)
                    }
                    signIn(email, password)
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error unknown")
                }
            }
    }

    private fun createUserToFirestore(userId: String, name: String, email: String){
        val user = hashMapOf(
            "uuid" to userId,
            "name" to name,
            "email" to email,
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener{ e ->
                _authState.value = AuthState.Error("Failed to save user: ${e.message}")
            }
    }

    private fun getUserFromDatabase(userId: String) {
        _authState.value = AuthState.Loading

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _currentUser.value = user
                    Log.d("AuthModel", "User data retrieved: $user")
                } else {
                    _authState.value = AuthState.Error("User not found")
                    Log.e("AuthModel", "User document does not exist")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Failed to fetch user: ${e.message}")
                Log.e("AuthModel", "Error retrieving user", e)
            }
    }

    fun signIn(email: String, password: String){
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val userId = auth.currentUser?.uid
                    _currentUserAuth.value = auth.currentUser
                    if (userId != null) {
                        getUserFromDatabase(userId)  // Fetch user details after sign-in
                    }
                    _authState.value = AuthState.Success
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error unknown")
                }
            }
    }

    fun logout(){
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