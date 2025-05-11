package com.example.bingetracker.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Get current filter and sort states
    val currentFilter by entertainmentModel.currentFilter.collectAsState()
    val currentSort by entertainmentModel.currentSort.collectAsState()

    // Get filtered content
    val filteredMovies by entertainmentModel.filteredMovies.collectAsState()
    val filteredTVShows by entertainmentModel.filteredTVShows.collectAsState()

    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Same gradient as AuthScreen
    val gradientColors = listOf(
        Color(0xFF0A0F3D),  // Darker blue at the top
        Color(0xFF141E61),  // Mid blue
        Color(0xFF0A1172)   // Slightly lighter blue with a hint of purple
    )

    // Apply gradient background to entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        // Add subtle diagonal texture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.05f),  // Very subtle black
                            Color(0xFF000000).copy(alpha = 0.0f)    // Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when {
                user == null && currentUserAuth != null -> {
                    Text(
                        "Loading...",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White  // Make text visible on dark background
                    )
                }
                user != null -> {
                    SearchBar(searchQuery) { query ->
                        searchQuery = query
                        entertainmentModel.searchForEntertainment(query)
                    }

                    // Add the HomeFilterBar
                    if (searchQuery.isBlank()) {
                        HomeFilterBar(
                            currentFilter = currentFilter,
                            currentSort = currentSort,
                            onFilterChanged = { entertainmentModel.updateFilter(it) },
                            onSortChanged = { entertainmentModel.updateSort(it) }
                        )
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
                        color = Color.White  // Make title white for visibility
                    )

                    if (searchQuery.isNotBlank()) {
                        val movieResults by entertainmentModel.searchMovieResults.collectAsState()
                        val tvResults by entertainmentModel.searchTVResults.collectAsState()
                        Entertainment(authModel, entertainmentModel, movieResults, tvResults)
                    } else {
                        Entertainment(authModel, entertainmentModel, filteredMovies, filteredTVShows)
                    }
                }
                else -> {
                    Text(
                        text = "Not logged in",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White  // Make text visible on dark background
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    // Super compact search bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = { onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(19.dp)
                )
                .background(
                    Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(19.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = Color.Black
            ),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Search movies or TV shows...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}