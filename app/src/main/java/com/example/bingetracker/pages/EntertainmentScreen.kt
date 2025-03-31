package com.example.bingetracker.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bingetracker.data.EntertainmentItem
import com.example.bingetracker.data.EntertainmentType
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel
import com.example.bingetracker.models.EntertainmentModel
import kotlin.math.log


@Composable
fun Entertainment(authModel: AuthModel, entertainmentModel: EntertainmentModel, movieList: List<Movie>, tvShowList: List<TVShow>) {
    val bingeModel : BingeModel = viewModel()
    var selectedItem by remember { mutableStateOf<EntertainmentItem?>(null) }

    val user = authModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        entertainmentModel.getPopularMovies()
        entertainmentModel.getPopularTvShows()
    }

    Scaffold{ padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(movieList){ movie ->
                    ItemCard(movie){selectedItem = movie}
                }
                items(tvShowList){tvShow ->
                    ItemCard(tvShow) { selectedItem = tvShow }
                }
            }
        }

        selectedItem?.let{
            ItemPopup(item = it, onDismiss = {selectedItem = null}, bingeModel, user.value!!.uuid)
        }
    }
}

@Composable
fun ItemCard(item : EntertainmentItem, onClick: () -> Unit){
    val title = item.title
    val posterPath = item.posterPath

    if(!posterPath.isNullOrEmpty()){
        Column(modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500$posterPath",
                contentDescription = title,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ItemPopup(item: EntertainmentItem, onDismiss: () -> Unit, bingeModel: BingeModel, userId: String) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var bingeName by remember { mutableStateOf("") }


    var showAddDialog by remember { mutableStateOf(false) }
    val bingeList by bingeModel.userBinges.collectAsState()

    LaunchedEffect(userId) {
        bingeModel.getUserBinges(userId)
    }

    val title = item.title
    val posterPath = item.posterPath
    val overview = item.overview

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!posterPath.isNullOrEmpty()) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500$posterPath",
                            contentDescription = title,
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = overview ?: "No description available.",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Binge")
                    }
                    Button(
                        onClick = { showAddDialog = true},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Existing Binge")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Custom Dialog layout
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Create a Binge",
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Enter a name for your binge:")
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = bingeName,
                        onValueChange = { bingeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Binge Name") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (bingeName.isNotEmpty()) {
                                    val entertainmentItem = when (item) {
                                        is Movie -> Movie(
                                            id = item.id,
                                            title = item.title,
                                            posterPath = item.posterPath,
                                            releaseDate = item.releaseDate,
                                            overview = item.overview
                                        )

                                        is TVShow -> TVShow(
                                            id = item.id,
                                            title = item.title,
                                            posterPath = item.posterPath,
                                            releaseDate = item.releaseDate,
                                            overview = item.overview,
                                            totalEpisodes = item.totalEpisodes,
                                            watchedEpisodes = item.watchedEpisodes ?: emptyList()
                                        )

                                        else -> null
                                    }

                                    entertainmentItem?.let {
                                        bingeModel.createBinge(userId, bingeName, it)
                                    }
                                    showCreateDialog = false
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Create")
                        }

                        Button(
                            onClick = { showCreateDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showAddDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select a Binge",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (bingeList.isEmpty()) {
                        Text("No binges found.")
                    } else {
                        Log.d("ENTERTAINMENT SCREEN", "ItemPopup: ${item.type}")
                        bingeList.forEach { binge ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val entertainmentItem = when (item) {
                                            is Movie -> Movie(
                                                id = item.id,
                                                title = item.title,
                                                posterPath = item.posterPath,
                                                releaseDate = item.releaseDate,
                                                overview = item.overview
                                            )
                                            is TVShow -> TVShow(
                                                id = item.id,
                                                title = item.title,
                                                posterPath = item.posterPath,
                                                releaseDate = item.releaseDate,
                                                overview = item.overview,
                                                totalEpisodes = item.totalEpisodes,
                                                watchedEpisodes = item.watchedEpisodes ?: emptyList()
                                            )

                                            else -> null
                                        }

                                        entertainmentItem?.let {
                                            bingeModel.addEntertainmentToBinge(binge.id, it)
                                        }
                                        showAddDialog = false
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Text(
                                    text = binge.name,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showAddDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
