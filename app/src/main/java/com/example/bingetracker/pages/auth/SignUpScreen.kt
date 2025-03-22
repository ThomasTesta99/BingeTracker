package com.example.bingetracker.pages.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
fun SignUpScreen(
    authViewModel: AuthModel = AuthModel(),
    onSignUpSuccess: () -> Unit
){
    var email by remember {mutableStateOf("")}
    var password by remember { mutableStateOf("") }
    val signUpState by authViewModel.signUpState.collectAsState()

    Column (modifier = Modifier.padding(16.dp)){
        TextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("Email")}
        )
        Spacer(modifier = Modifier.padding(16.dp))
        TextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Button(onClick = {authViewModel.signUp(email, password)}) {
            Text("Sign Up")
        }

        when (signUpState){
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success -> onSignUpSuccess()
            is AuthState.Error -> Text(
                text = (signUpState as AuthState.Error).message,
                color = Color.Red
            )
            else -> {}
        }
    }

}