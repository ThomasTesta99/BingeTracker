package com.example.bingetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bingetracker.pages.HomeScreen
import com.example.bingetracker.pages.auth.AuthScreen

@Composable
fun AppNavigation(navController: NavHostController){
    NavHost(navController, startDestination = "auth"){
        composable("auth") { AuthScreen {navController.navigate("home")} }
        composable("home") { HomeScreen() }
    }
}
