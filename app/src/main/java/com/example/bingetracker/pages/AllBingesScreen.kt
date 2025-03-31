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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel

@Composable
fun AllBingesScreen(navController: NavController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()
    val bingeModel: BingeModel = viewModel()
    val bingeList by bingeModel.userBinges.collectAsState()

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

    Column(modifier = Modifier.fillMaxSize()) {
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

                        val total = binge.entertainmentList.sumOf {
                            if (it is TVShow) it.episodes?.size ?: 0 else 1
                        }
                        val watchedItems = binge.entertainmentList.sumOf {
                            when (it) {
                                is Movie -> if (it.watched) 1 else 0
                                is TVShow -> it.watchedEpisodes?.size ?: 0
                            }
                        }
                        val progress = if (total > 0) watchedItems.toFloat() / total else 0f

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
                                modifier = Modifier.padding(4.dp)
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
                    Text("Delete Selected (${selectedBinges.value.size})")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        deletingBinges = false
                        selectedBinges.value = emptySet()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
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
