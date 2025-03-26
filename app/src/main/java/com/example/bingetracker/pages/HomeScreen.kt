package com.example.bingetracker.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.data.User
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.EntertainmentModel
import java.util.Properties

@Composable
fun HomeScreen(navController: NavHostController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()

    val entertainmentModel : EntertainmentModel = viewModel()

    // Navigate to auth screen when logged out
    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true } // Clears backstack
            }
        }
    }

    Scaffold(
        topBar = { SearchBar() },
        bottomBar = {BottomNavBar()}
    ){ padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                user == null && currentUserAuth != null -> {
                    Text("Loading...")
                }
                user != null -> {
                    Column {
                        Spacer(modifier = Modifier.height(50.dp))
                        Text(
                            text = "Welcome, ${user?.name}",
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { authModel.logout() }) {
                            Text("Logout")
                        }
                    }

                    SearchBar()
                    PopularEntertainment(entertainmentModel)
                    BottomNavBar()

                }
                else -> {
                    Text(text = "Not logged in")
                }
            }
        }
    }
}

@Composable
fun SearchBar(){
    var searchBar by remember { mutableStateOf("") }
    TextField(
        value = searchBar,
        onValueChange = {searchBar = it},
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        singleLine = true
    )
}

@Composable
fun BottomNavBar(){
    BottomAppBar {
        Text(
            text = "Navigation Bar",
            modifier = Modifier.padding(8.dp)
        )
    }
}


