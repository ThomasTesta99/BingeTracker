package com.example.bingetracker.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.EntertainmentModel

@Composable
fun HomeScreen(navController: NavHostController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()

    val entertainmentModel: EntertainmentModel = viewModel()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true } // Clears backstack
            }
        }
    }

    Scaffold()
     { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when {
                user == null && currentUserAuth != null -> {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                }
                user != null -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    SearchBar(searchQuery) { query ->
                        searchQuery = query
                        entertainmentModel.searchForEntertainment(query)
                    }

                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "Search results for \"$searchQuery\""
                        } else {
                            "Popular Movies and TV Shows"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                    )

                    if (searchQuery.isNotBlank()) {
                        val movieResults by entertainmentModel.searchMovieResults.collectAsState()
                        val tvResults by entertainmentModel.searchTVResults.collectAsState()
                        Entertainment(entertainmentModel, movieResults, tvResults)
                    } else {
                        val popularMovies by entertainmentModel.movieList.collectAsState()
                        val popularTVShows by entertainmentModel.tvShowList.collectAsState()
                        Entertainment(entertainmentModel, popularMovies, popularTVShows)
                    }
                }
                else -> {
                    Text(text = "Not logged in", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = { onQueryChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        singleLine = true,
        placeholder = { Text("Search movies or TV shows...") }
    )
}
