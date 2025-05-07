package com.example.bingetracker.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // val bingeList by bingeModel.userBinges.collectAsState()
    // Use filteredBinges instead of userBinges
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

    when (bingeState) {
        is BingeState.Loading -> CircularProgressIndicator()
        is BingeState.Error -> Text(
            text = (bingeState as BingeState.Error).message,
            color = Color.Red
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
                                }
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
                                            }
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
                                        .height(6.dp)
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete Selected (${selectedBinges.value.size})", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                deletingBinges = false
                                selectedBinges.value = emptySet()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            deletingBinges = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Delete Binges")
                    }
                }


                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text("Confirm Deletion") },
                        text = {
                            Text("Are you sure you want to delete ${selectedBinges.value.size} binge(s)?")
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
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}
