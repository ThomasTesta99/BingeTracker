package com.example.bingetracker.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bingetracker.data.Binge
import com.example.bingetracker.data.EntertainmentItem
import com.example.bingetracker.data.EpisodeWatched
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel
import com.example.bingetracker.models.BingeState
import com.example.bingetracker.models.calculateProgress

@Composable
fun BingeDetailScreen(
    bingeId: String,
    bingeModel: BingeModel,
    authModel: AuthModel,
    navController: NavController
) {
    val user by authModel.currentUser.collectAsState()
    val bingeList by bingeModel.userBinges.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    val bingeState by bingeModel.bingeState.collectAsState()

    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    LaunchedEffect(user?.uuid) {
        user?.uuid?.let { bingeModel.getUserBinges(it) }
    }

    val bingeValue = remember { mutableStateOf<Binge?>(null) }

    LaunchedEffect(bingeList) {
        bingeValue.value = bingeList.find { it.id == bingeId }
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
            is BingeState.Error -> Text(
                text = (bingeState as BingeState.Error).message,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> {
                bingeValue.value?.let { binge ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = binge.name,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.White  // Make title white for visibility
                            )
                        }

                        items(binge.entertainmentList) { item ->
                            EntertainmentItemDetail(item)
                        }

                        item {
                            HorizontalProgressBar(binge)
                        }

                        item {
                            ChecklistItems(binge = binge, bingeModel = bingeModel) { updated ->
                                bingeValue.value = updated
                            }
                        }
                        item {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red  // Red for delete button
                                )
                            ) {
                                Text("Delete this binge", color = Color.White)
                            }
                            if (showConfirmDialog) {
                                val context = LocalContext.current
                                AlertDialog(
                                    onDismissRequest = { showConfirmDialog = false },
                                    title = { Text("Delete this binge", color = Color.Black) },
                                    text = {
                                        Text("Are you sure you want to delete current binge?", color = Color.Black)
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showConfirmDialog = false
                                            user?.let { bingeModel.deleteBinge(bingeId, it.uuid) }
                                            navController.popBackStack()
                                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT)
                                                .show()
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
    }
}

@Composable
fun ChecklistItems(
    binge: Binge,
    bingeModel: BingeModel,
    onBingeUpdate: (Binge) -> Unit
) {
    // Container with semi-transparent white background for better visibility
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            binge.entertainmentList.forEachIndexed { index, item ->
                when (item) {
                    is Movie -> {
                        val isWatchedState = remember { mutableStateOf(item.watched) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isWatchedState.value,
                                onCheckedChange = { checked ->
                                    isWatchedState.value = checked
                                    bingeModel.toggleMovieWatched(binge.id, item.id, checked)

                                    val updatedList = binge.entertainmentList.toMutableList()
                                    updatedList[index] = item.copy(watched = checked)
                                    onBingeUpdate(binge.copy(entertainmentList = updatedList))
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.White,
                                    checkedColor = Color(0xFFAA00FF)  // Purple accent
                                )
                            )
                            Text(text = item.title, color = Color.Black)
                        }
                    }

                    is TVShow -> {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            color = Color.Black
                        )
                        item.episodes?.forEach { episode ->
                            val watchedState = remember {
                                mutableStateOf(
                                    item.watchedEpisodes.any {
                                        it.seasonNumber == episode.seasonNumber &&
                                                it.episodeNumber == episode.episodeNumber
                                    }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = watchedState.value,
                                    onCheckedChange = { checked ->
                                        watchedState.value = checked
                                        bingeModel.toggleEpisodeWatched(
                                            binge.id, item.id,
                                            episode.seasonNumber, episode.episodeNumber,
                                            checked
                                        )

                                        val updatedList = binge.entertainmentList.toMutableList()
                                        val updatedEpisodes = item.watchedEpisodes.toMutableList()
                                        val watchedEp = EpisodeWatched(
                                            seasonNumber = episode.seasonNumber,
                                            episodeNumber = episode.episodeNumber
                                        )

                                        if (checked) updatedEpisodes.add(watchedEp)
                                        else updatedEpisodes.remove(watchedEp)

                                        updatedList[index] = item.copy(watchedEpisodes = updatedEpisodes)
                                        onBingeUpdate(binge.copy(entertainmentList = updatedList))
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkmarkColor = Color.White,
                                        checkedColor = Color(0xFFAA00FF)  // Purple accent
                                    )
                                )
                                Text(
                                    text = "S${episode.seasonNumber} E${episode.episodeNumber}: ${episode.title}",
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EntertainmentItemDetail(item: EntertainmentItem) {
    // Card with semi-transparent white background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
                contentDescription = item.title,
                modifier = Modifier.height(100.dp)
            )
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Black
                )
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun HorizontalProgressBar(binge: Binge) {
    val progress = calculateProgress(binge.entertainmentList)
    val progressPercent = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Watched: $progressPercent%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.White  // Make progress text white
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFFAA00FF))  // Purple progress bar
            )
        }
    }
}