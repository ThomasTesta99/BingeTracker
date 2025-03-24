package com.example.bingetracker.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.pages.HomeScreen
import com.example.bingetracker.pages.auth.AuthScreen

@Composable
fun AppNavigation(navController: NavHostController, authModel: AuthModel){

    NavHost(navController, startDestination = "auth"){
        composable("auth") {
            AuthScreen(authModel) {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true } // Removes "auth" from back stack
                }
            }
        }
        composable("home") { HomeScreen(navController, authModel) }
    }
}
