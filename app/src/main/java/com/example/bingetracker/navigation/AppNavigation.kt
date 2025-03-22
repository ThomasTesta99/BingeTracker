package com.example.bingetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bingetracker.pages.auth.SignUpScreen

@Composable
fun AppNavigation(navController: NavHostController){
    NavHost(navController, startDestination = "signup"){
        composable("signup") { SignUpScreen {navController.navigate("home")}}
        composable("home") {  }
    }
}
