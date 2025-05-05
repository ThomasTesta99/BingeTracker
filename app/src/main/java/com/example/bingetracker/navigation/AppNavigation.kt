package com.example.bingetracker.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bingetracker.data.User
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel
import com.example.bingetracker.pages.AllBingesScreen
import com.example.bingetracker.pages.BingeDetailScreen
import com.example.bingetracker.pages.HomeScreen
import com.example.bingetracker.pages.auth.AuthScreen

@Composable
fun AppNavigation(navController: NavHostController, authModel: AuthModel){
    val user by authModel.currentUser.collectAsState()

    Scaffold (
        topBar = {
            user?.let { TopBar(it, authModel) }
        },
        bottomBar = {
            user?.let { BottomNavBar(navController) }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {  // Wrap NavHost in Box with padding
            NavHost(navController, startDestination = "auth") {
                composable("auth") {
                    AuthScreen(authModel) {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
                composable("home") { HomeScreen(navController, authModel) }
                composable("binges") { AllBingesScreen(navController, authModel) }
                composable("bingeDetail/{bingeId}") { backStackEntry ->
                    val bingeId = backStackEntry.arguments?.getString("bingeId") ?: return@composable
                    val bingeModel: BingeModel = viewModel()
                    BingeDetailScreen(bingeId = bingeId, bingeModel = bingeModel, authModel, navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    BottomAppBar {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = false,
                onClick = { navController.navigate("home") }
            )

            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Binges") },
                label = { Text("Binges") },
                selected = false,
                onClick = { navController.navigate("binges") }
            )
        }
    }
}

@Composable
fun TopBar(user : User, authModel: AuthModel){
    val userName = user.name.substringBefore(' ')
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
                .padding(16.dp)
        )

        Button(
            onClick = { authModel.logout() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Logout")
        }
    }
}
