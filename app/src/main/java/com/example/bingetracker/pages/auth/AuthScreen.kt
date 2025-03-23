package com.example.bingetracker.pages.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.AuthState

@Composable
fun AuthScreen(
    authViewModel: AuthModel = AuthModel(),
    onAuthSuccess: () -> Unit
){
    var isSignUp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)){
        if(isSignUp){
            SignUpScreen(authViewModel, onAuthSuccess, onSwitchToSignIn = {isSignUp = false})
        }else{
            SignInScreen(authViewModel, onAuthSuccess, onSwitchToSignUp = {isSignUp = true})
        }
    }
}

@Composable
fun SignUpScreen(
    authModel: AuthModel,
    onSignUpSuccess: () -> Unit,
    onSwitchToSignIn: () -> Unit,
){
    AuthForm(
        title = "Sign Up",
        buttonText = "Sign Up",
        authModel = authModel,
        onSubmit = {email, password -> authModel.signUp(email, password)},
        onAuthSuccess = onSignUpSuccess,
        switchText = "Already have an account? Sign in",
        onSwitch = onSwitchToSignIn
    )
}

@Composable
fun SignInScreen(
    authModel: AuthModel,
    onSignInSuccess: () -> Unit,
    onSwitchToSignUp: () -> Unit,
){
    AuthForm(
        title = "Sign In",
        buttonText = "Sign In",
        authModel = authModel,
        onSubmit = {email, password -> authModel.signIn(email, password)},
        onAuthSuccess = onSignInSuccess,
        switchText = "Already have an account? Sign in",
        onSwitch = onSwitchToSignUp
    )
}

@Composable
fun AuthForm(
    title: String,
    buttonText: String,
    authModel: AuthModel,
    onSubmit: (String, String) -> Unit,
    onAuthSuccess: () -> Unit,
    switchText: String,
    onSwitch: () -> Unit
){
    var email by remember {mutableStateOf("")}
    var password by remember { mutableStateOf("") }
    val authState by authModel.authState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)){
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("password") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {onSubmit(email, password)}) {
            Text(buttonText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authState){
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success -> onAuthSuccess()
            is AuthState.Error -> Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitch){
            Text(switchText)
        }
    }
}