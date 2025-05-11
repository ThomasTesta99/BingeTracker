package com.example.bingetracker.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.AuthState
import com.example.bingetracker.models.BingeModel
import com.example.bingetracker.models.BingeState
import com.example.bingetracker.models.calculateProgress

@Composable
fun AllBingesScreen(navController: NavController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()
    val bingeModel: BingeModel = viewModel()
    val bingeState by bingeModel.bingeState.collectAsState()
    val bingeList by bingeModel.filteredBinges.collectAsState()

    // Get current filter and sort
    val currentFilter by bingeModel.currentFilter.collectAsState()
    val currentSort by bingeModel.currentSort.collectAsState()

    val selectedBinges = remember { mutableStateOf(setOf<String>()) }
    var deletingBinges by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(user?.uuid) {
        user?.uuid?.let { bingeModel.getUserBinges(it) }
    }

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

        when (bingeState) {
            is BingeState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White  // Make loading indicator visible
            )
            is BingeState.Error -> Text(
                text = (bingeState as BingeState.Error).message,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Add the filter/sort bar here, before the LazyVerticalGrid
                    FilterSortBar(
                        currentFilter = currentFilter,
                        currentSort = currentSort,
                        onFilterChanged = { bingeModel.updateFilter(it) },
                        onSortChanged = { bingeModel.updateSort(it) }
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bingeList) { binge ->
                            val isSelected = selectedBinges.value.contains(binge.id)

                            Card(
                                modifier = Modifier
                                    .clickable {
                                        if (deletingBinges) {
                                            selectedBinges.value = if (isSelected) {
                                                selectedBinges.value - binge.id
                                            } else {
                                                selectedBinges.value + binge.id
                                            }
                                        } else {
                                            navController.navigate("bingeDetail/${binge.id}")
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)  // Semi-transparent white
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.wrapContentWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        binge.entertainmentList.forEach {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                                                contentDescription = it.title,
                                                modifier = Modifier
                                                    .height(100.dp)
                                                    .padding(top = 10.dp, end = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val progress = calculateProgress(binge.entertainmentList)

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (deletingBinges) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    selectedBinges.value = if (isSelected) {
                                                        selectedBinges.value - binge.id
                                                    } else {
                                                        selectedBinges.value + binge.id
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkmarkColor = Color.White,
                                                    checkedColor = Color(0xFFAA00FF)  // Purple accent
                                                )
                                            )
                                        }
                                        Text(
                                            text = "${binge.name} \n ${(progress * 100).toInt()}% watched",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(4.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = Color(0xFFAA00FF),  // Purple progress bar
                                        trackColor = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    if (deletingBinges) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    showConfirmDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red  // Red for delete
                                )
                            ) {
                                Text(
                                    "Delete Selected (${selectedBinges.value.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    deletingBinges = false
                                    selectedBinges.value = emptySet()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White  // White background
                                )
                            ) {
                                Text(
                                    "Cancel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                deletingBinges = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White  // White background
                            )
                        ) {
                            Text(
                                "Delete Binges",
                                color = Color.Black
                            )
                        }
                    }

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Confirm Deletion", color = Color.Black) },
                            text = {
                                Text(
                                    "Are you sure you want to delete ${selectedBinges.value.size} binge(s)?",
                                    color = Color.Black
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    selectedBinges.value.forEach { bingeId ->
                                        user?.uuid?.let { bingeModel.deleteBinge(bingeId, it) }
                                    }
                                    showConfirmDialog = false
                                    selectedBinges.value = emptySet()
                                    deletingBinges = false
                                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Yes", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text("Cancel", color = Color.Gray)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}