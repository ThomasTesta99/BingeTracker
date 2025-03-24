package com.example.bingetracker.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.bingetracker.models.AuthModel

@Composable
fun HomeScreen(navController: NavHostController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()

    // Navigate to auth screen when logged out
    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true } // Clears backstack
            }
        }

    }

    Column {
        if (user != null) {
            Text(text = "Welcome, ${user?.name}")
            Button(onClick = { authModel.logout() }) {
                Text("Logout")
            }
        } else {
            Text(text = "Not logged in")
        }
    }
}
