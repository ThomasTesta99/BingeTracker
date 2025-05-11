package com.example.bingetracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
fun TopBar(user: User, authModel: AuthModel){
    val userName = user.name.substringBefore(' ')

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()  // Add status bar padding
            .background(
                Color.Black.copy(alpha = 0.7f)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Logo/Branding section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎬",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "Binge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        }

        // User section
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hi, $userName",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            Button(
                onClick = { authModel.logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAA00FF).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .height(28.dp)
                    .padding(0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 4.dp,
                    bottom = 4.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Logout",
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}