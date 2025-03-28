package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.api.RetrofitClient
import com.example.bingetracker.data.Binge
import com.example.bingetracker.data.BingeFireStore
import com.example.bingetracker.data.EntertainmentItem
import com.example.bingetracker.data.EntertainmentType
import com.example.bingetracker.data.Episode
import com.example.bingetracker.data.EpisodeWatched
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.StoredEntertainmentItem
import com.example.bingetracker.data.TVShow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BingeModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    val apiKey = BuildConfig.TMDB_API_KEY
    private val _userBinges = MutableStateFlow<List<Binge>>(emptyList())
    val userBinges: StateFlow<List<Binge>> = _userBinges

    private val _bingeId = MutableStateFlow<String?>(null)
    val bingeId: StateFlow<String?> = _bingeId

    private suspend fun createNewBinge(userId: String, name: String, item: EntertainmentItem) {
        try {
            val entertainmentList = listOf(item.toStored())
            val newBinge = BingeFireStore(
                userId = userId,
                name = name,
                entertainmentList = entertainmentList
            )

            val document = db.collection("binges").add(newBinge).await()
            Log.d("BINGEMODEL", "createNewBinge: before add entertainment")
            addEntertainment(document.id, item)
            _bingeId.value = document.id
        } catch (e: Exception) {
            Log.e("BingeModel", "Error creating new binge: ${e.message}")
        }
    }

    private suspend fun getBinges(userId: String) {
        try {
            val binges = db.collection("binges")
                .whereEqualTo("userId", userId)
                .get().await()
                .documents
                .mapNotNull { document ->
                    val binge = document.toObject(BingeFireStore::class.java)
                    binge?.let {
                        Binge(
                            id = it.id,
                            userId = it.userId,
                            name = it.name,
                            entertainmentList = it.entertainmentList.map { stored ->
                                when (stored.type) {
                                    EntertainmentType.MOVIE -> Movie(
                                        id = stored.id,
                                        title = stored.title,
                                        posterPath = stored.posterPath,
                                        releaseDate = stored.releaseDate,
                                        overview = stored.overview,
                                        watched = stored.watched
                                    )
                                    EntertainmentType.TV_SHOW -> TVShow(
                                        id = stored.id,
                                        title = stored.title,
                                        posterPath = stored.posterPath,
                                        releaseDate = stored.releaseDate,
                                        overview = stored.overview,
                                        totalEpisodes = stored.totalEpisodes,
                                        watchedEpisodes = stored.watchedEpisodes ?: emptyList(),
                                        episodes = stored.episodes ?: emptyList(),
                                    )
                                }
                            }
                        )
                    }
                }
            Log.d("BINGE MODEL", "getBinges: $binges")
            _userBinges.value = binges
        } catch (e: Exception) {
            Log.e("BingeModel", "Error fetching user binges: ${e.message}")
        }
    }

//    private suspend fun addEntertainment(bingeId: String, entertainment: EntertainmentItem) {
//        val bingeRef = db.collection("binges").document(bingeId)
//        val binge = bingeRef.get().await().toObject(BingeFireStore::class.java)
//        Log.d("EPISODE DEBUG", "$bingeRef + $binge")
//
//        binge?.let {
//            val updatedList = it.entertainmentList.toMutableList()
//            Log.d("EPISODE DEBUG", "Existing IDs: ${updatedList.map { i -> i.id }}")
//
//            val index = updatedList.indexOfFirst { item -> item.id == entertainment.id }
//            if (index != -1 && entertainment is TVShow) {
//                val episodes = fetchEpisodesForTVShow(entertainment.id)
//                val updatedItem = entertainment.toStored().copy(
//                    episodes = episodes,
//                    totalEpisodes = episodes.size
//                )
//                updatedList[index] = updatedItem
//                Log.d("BINGEMODEL", "Updated $entertainment with ${episodes.size} episodes")
//            }
//
//            bingeRef.update("entertainmentList", updatedList).await()
//        }
//    }

    private suspend fun addEntertainment(bingeId: String, entertainment: EntertainmentItem) {
        val bingeRef = db.collection("binges").document(bingeId)
        val binge = bingeRef.get().await().toObject(BingeFireStore::class.java)

        Log.d("EPISODE DEBUG", "$bingeRef + $binge")
        Log.d("BINGEMODEL", "$entertainment")
        binge?.let {
            val updatedList = it.entertainmentList.toMutableList()

            val exists = updatedList.any { item -> item.id == entertainment.id }
            if (!exists) {
                val storedItem = when (entertainment) {
                    is TVShow -> {
                        val episodes = fetchEpisodesForTVShow(entertainment.id)
                        entertainment.toStored().copy(
                            episodes = episodes,
                            totalEpisodes = episodes.size,
                            type = entertainment.type
                        )
                    }
                    is Movie -> {
                        entertainment.toStored().copy(
                            type = entertainment.type
                        )
                    }
                }

                updatedList.add(storedItem)
                Log.d("BINGEMODEL", "Added new item: $storedItem")
                bingeRef.update("entertainmentList", updatedList).await()
            } else {
                Log.d("BINGEMODEL", "Item already exists in binge.")
            }
        }
    }


    private suspend fun fetchEpisodesForTVShow(tvShowId: Int): List<Episode> {
        val episodes = mutableListOf<Episode>()
        try {
            val response = RetrofitClient.api.getTvShowDetails(tvShowId, apiKey)

            val validSeasons = response.seasons.filter { it.seasonNumber > 0 }

            for (season in validSeasons) {
                val seasonResponse = RetrofitClient.api.getTvSeason(tvShowId, season.seasonNumber, apiKey)
                episodes.addAll(seasonResponse.episodes)
            }

        } catch (e: Exception) {
            Log.e("EntertainmentModel", "Error fetching episodes: ${e.message}")
        }
        return episodes
    }

    fun toggleMovieWatched(bingeId: String, movieId: Int, isWatched: Boolean) {
        viewModelScope.launch {
            val bingeRef = db.collection("binges").document(bingeId)
            db.runTransaction { transaction ->
                val binge = transaction.get(bingeRef).toObject(BingeFireStore::class.java) ?: return@runTransaction
                val updatedList = binge.entertainmentList.map { item ->
                    if (item.id == movieId && item.type == EntertainmentType.MOVIE) {
                        item.copy(watched = isWatched)
                    } else item
                }
                transaction.update(bingeRef, "entertainmentList", updatedList)
            }.await() // <-- Await transaction completion explicitly here

            // ✅ Fetch updated binge list again after transaction
            val userId = _userBinges.value.firstOrNull { it.id == bingeId }?.userId
            userId?.let {
                getBinges(it)
            }
        }
    }


    fun toggleEpisodeWatched(
        bingeId: String,
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        isWatched: Boolean
    ) {
        viewModelScope.launch {
            val bingeRef = db.collection("binges").document(bingeId)
            db.runTransaction { transaction ->
                val binge = transaction.get(bingeRef).toObject(BingeFireStore::class.java) ?: return@runTransaction
                val updatedList = binge.entertainmentList.map { item ->
                    if (item.id == tvShowId && item.type == EntertainmentType.TV_SHOW) {
                        val updatedEpisodes = item.watchedEpisodes.toMutableList()
                        val episodeWatched = EpisodeWatched(seasonNumber, episodeNumber)
                        if (isWatched) {
                            if (!updatedEpisodes.contains(episodeWatched)) updatedEpisodes.add(episodeWatched)
                        } else {
                            updatedEpisodes.remove(episodeWatched)
                        }
                        item.copy(watchedEpisodes = updatedEpisodes)
                    } else item
                }
                transaction.update(bingeRef, "entertainmentList", updatedList)
            }.await() // Explicitly await completion

            // ✅ Fetch updated data from Firestore afterward:
            val userId = _userBinges.value.firstOrNull { it.id == bingeId }?.userId
            userId?.let {
                getBinges(it)
            }
        }
    }

    fun createBinge(userId: String, name: String, item: EntertainmentItem) {
        viewModelScope.launch {
            createNewBinge(userId, name, item)
        }
    }

    fun addEntertainmentToBinge(bingeId: String, entertainment: EntertainmentItem) {
        viewModelScope.launch {
            addEntertainment(bingeId, entertainment)
        }
    }

    fun getUserBinges(userId: String) {
        viewModelScope.launch {
            getBinges(userId)
        }
    }
}

fun EntertainmentItem.toStored(): StoredEntertainmentItem {
    return when (this) {
        is Movie -> StoredEntertainmentItem(
            id = id,
            title = title,
            posterPath = posterPath,
            overview = overview,
            type = type,
            releaseDate = releaseDate,
            watched = watched
        )
        is TVShow -> StoredEntertainmentItem(
            id = id,
            title = title,
            posterPath = posterPath,
            overview = overview,
            type = type,
            releaseDate = releaseDate,
            totalEpisodes = totalEpisodes,
            watchedEpisodes = watchedEpisodes,
            episodes = episodes
        )
    }
}

