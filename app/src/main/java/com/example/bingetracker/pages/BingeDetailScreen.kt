package com.example.bingetracker.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    // ✅ Mutable local binge state
    val bingeState = remember { mutableStateOf<Binge?>(null) }

    LaunchedEffect(bingeList) {
        bingeState.value = bingeList.find { it.id == bingeId }
    }

    bingeState.value?.let { binge ->
        Column(modifier = Modifier.padding(16.dp)) {
            // Binge Name
            Text(
                text = binge.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Entertainment items details
            binge.entertainmentList.forEach { item ->
                EntertainmentItemDetail(item)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalProgressBar(binge)
                Spacer(modifier = Modifier.height(16.dp))
                EntertainmentChecklist(binge = binge, bingeModel = bingeModel) { updatedBinge ->
                    bingeState.value = updatedBinge // ✅ immediate local UI update
                }
            }
        }
    } ?: run {
        Text("Loading binge details...", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun EntertainmentItemDetail(item: EntertainmentItem) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${item.posterPath}",
            contentDescription = item.title,
            modifier = Modifier.height(200.dp)
        )
        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = item.overview,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EntertainmentChecklist(
    binge: Binge,
    bingeModel: BingeModel,
    onBingeUpdate: (Binge) -> Unit
) {
    LazyColumn {
        binge.entertainmentList.forEachIndexed { index, item ->
            when (item) {
                is Movie -> {
                    item {
                        val isWatchedState = remember { mutableStateOf(item.watched) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isWatchedState.value,
                                onCheckedChange = { checked ->
                                    isWatchedState.value = checked
                                    bingeModel.toggleMovieWatched(binge.id, item.id, checked)

                                    // Immediate UI update
                                    val updatedEntertainment = binge.entertainmentList.toMutableList()
                                    updatedEntertainment[index] = item.copy(watched = checked)
                                    onBingeUpdate(binge.copy(entertainmentList = updatedEntertainment))
                                }
                            )
                            Text(text = item.title)
                        }
                    }
                }
                is TVShow -> {
                    item {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(item.episodes ?: emptyList()) { episode ->
                        val episodeWatchedState = remember {
                            mutableStateOf(
                                item.watchedEpisodes.any {
                                    it.seasonNumber == episode.seasonNumber &&
                                            it.episodeNumber == episode.episodeNumber
                                }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = episodeWatchedState.value,
                                onCheckedChange = { checked ->
                                    episodeWatchedState.value = checked
                                    bingeModel.toggleEpisodeWatched(
                                        binge.id, item.id, episode.seasonNumber, episode.episodeNumber, checked
                                    )

                                    // Immediate UI update
                                    val updatedEntertainment = binge.entertainmentList.toMutableList()
                                    val updatedEpisodes = item.watchedEpisodes.toMutableList()

                                    val episodeWatched = EpisodeWatched(
                                        seasonNumber = episode.seasonNumber,
                                        episodeNumber = episode.episodeNumber
                                    )

                                    if (checked) updatedEpisodes.add(episodeWatched)
                                    else updatedEpisodes.remove(episodeWatched)

                                    updatedEntertainment[index] = item.copy(watchedEpisodes = updatedEpisodes)
                                    onBingeUpdate(binge.copy(entertainmentList = updatedEntertainment))
                                }
                            )
                            Text(text = "S${episode.seasonNumber} E${episode.episodeNumber}: ${episode.title}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalProgressBar(binge: Binge) {
    val totalItems = binge.entertainmentList.sumOf {
        when (it) {
            is Movie -> 1
            is TVShow -> it.episodes?.size ?: 0
        }
    }
    val watchedItems = binge.entertainmentList.sumOf {
        when (it) {
            is Movie -> if (it.watched) 1 else 0
            is TVShow -> it.watchedEpisodes?.size ?: 0
        }
    }

    val progress = if (totalItems > 0) watchedItems.toFloat() / totalItems else 0f
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
            modifier = Modifier.padding(bottom = 8.dp)
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
                    .background(Color.Green)
            )
        }
    }
}




