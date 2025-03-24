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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bingetracker.R
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.AuthState

@Composable
fun AuthScreen(
    authViewModel: AuthModel,
    onAuthSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    // Use LaunchedEffect to navigate only once when authentication succeeds
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isSignUp) {
            SignUpScreen(authViewModel, onSwitchToSignIn = { isSignUp = false })
        } else {
            SignInScreen(authViewModel, onSwitchToSignUp = { isSignUp = true })
        }
    }
}

@Composable
fun SignUpScreen(
    authModel: AuthModel,
    onSwitchToSignIn: () -> Unit
) {
    AuthForm(
        title = stringResource(R.string.sign_up),
        buttonText = stringResource(R.string.sign_up),
        authModel = authModel,
        onSubmit = { name, email, password -> authModel.signUp(name, email, password) },
        switchText = stringResource(R.string.sign_up_switch_text),
        onSwitch = onSwitchToSignIn,
        isSignUp = true
    )
}

@Composable
fun SignInScreen(
    authModel: AuthModel,
    onSwitchToSignUp: () -> Unit
) {
    AuthForm(
        title = stringResource(R.string.sign_in),
        buttonText = stringResource(R.string.sign_in),
        authModel = authModel,
        onSubmit = { _, email, password -> authModel.signIn(email, password) },
        switchText = stringResource(R.string.sign_in_switch_text),
        onSwitch = onSwitchToSignUp
    )
}

@Composable
fun AuthForm(
    title: String,
    buttonText: String,
    authModel: AuthModel,
    onSubmit: (String, String, String) -> Unit,
    switchText: String,
    onSwitch: () -> Unit,
    isSignUp: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authModel.authState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isSignUp) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onSubmit(name, email, password) }) {
            Text(buttonText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitch) {
            Text(switchText)
        }
    }
}
