package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.api.RetrofitClient
import com.example.bingetracker.data.Binge
import com.example.bingetracker.data.BingeFilter
import com.example.bingetracker.data.BingeFireStore
import com.example.bingetracker.data.BingeSort
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
    private val apiKey = BuildConfig.TMDB_API_KEY

    private val _userBinges = MutableStateFlow<List<Binge>>(emptyList())
    val userBinges: StateFlow<List<Binge>> = _userBinges

    private val _bingeId = MutableStateFlow<String?>(null)
    val bingeId: StateFlow<String?> = _bingeId

    // filter and sort state flows
    private val _currentFilter = MutableStateFlow(BingeFilter.ALL)
    val currentFilter: StateFlow<BingeFilter> = _currentFilter

    private val _currentSort = MutableStateFlow(BingeSort.ALPHABETICAL)
    val currentSort: StateFlow<BingeSort> = _currentSort

    private val _filteredBinges = MutableStateFlow<List<Binge>>(emptyList())
    val filteredBinges: StateFlow<List<Binge>> = _filteredBinges

    private suspend fun deleteUserBinge(bingeId: String, userId: String){
        try {
            db.collection("binges").document(bingeId).delete().await()
            getUserBinges(userId)
        }catch (e: Exception){
            Log.e("Binge Model", "${e.message}")
        }
    }

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
                applyFilterAndSort()
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
            }.await()

            val userId = _userBinges.value.firstOrNull { it.id == bingeId }?.userId
            userId?.let {
                getBinges(it)
                applyFilterAndSort()
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
            applyFilterAndSort() //filter and sort getting binges
        }
    }

    fun deleteBinge(bingeId: String, userId: String){
        viewModelScope.launch {
            deleteUserBinge(bingeId, userId)
        }
    }

    fun updateFilter(filter: BingeFilter) {
        _currentFilter.value = filter
        applyFilterAndSort()
    }

    fun updateSort(sort: BingeSort) {
        _currentSort.value = sort
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        viewModelScope.launch {
            val filtered = when (_currentFilter.value) {
                BingeFilter.ALL -> _userBinges.value
                BingeFilter.MOVIES_ONLY -> _userBinges.value.filter { binge -> binge.entertainmentList.any { it is Movie } && binge.entertainmentList.none { it is TVShow }
                }
                BingeFilter.TV_SHOWS_ONLY -> _userBinges.value.filter { binge -> binge.entertainmentList.any { it is TVShow } && binge.entertainmentList.none { it is Movie }
                }
            }

            val sorted = when (_currentSort.value) {
                BingeSort.ALPHABETICAL -> filtered.sortedBy { it.name }
                BingeSort.PROGRESS -> filtered.sortedByDescending { binge ->
                    val total = binge.entertainmentList.sumOf {
                        when (it) {
                            is Movie -> 1
                            is TVShow -> it.episodes?.size ?: 0
                        }
                    }
                    val watched = binge.entertainmentList.sumOf {
                        when (it) {
                            is Movie -> if (it.watched) 1 else 0
                            is TVShow -> it.watchedEpisodes.size
                        }
                    }
                    if (total > 0) watched.toFloat() / total else 0f
                }
                BingeSort.RECENTLY_UPDATED -> filtered
            }

            _filteredBinges.value = sorted
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

