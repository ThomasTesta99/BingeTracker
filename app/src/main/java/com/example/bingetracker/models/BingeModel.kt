package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.data.Binge
import com.example.bingetracker.data.BingeFireStore
import com.example.bingetracker.data.EntertainmentItem
import com.example.bingetracker.data.EntertainmentType
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
            Log.d("BINGE", "DOCUMENT: ${document.id}")
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
                                        overview = stored.overview
                                    )
                                    EntertainmentType.TV_SHOW -> TVShow(
                                        id = stored.id,
                                        title = stored.title,
                                        posterPath = stored.posterPath,
                                        releaseDate = stored.releaseDate,
                                        overview = stored.overview,
                                        totalEpisodes = stored.totalEpisodes,
                                        watchedEpisodes = stored.watchedEpisodes ?: emptyList()
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
        try {
            val bingeRef = db.collection("binges").document(bingeId)
            val binge = bingeRef.get().await().toObject(BingeFireStore::class.java)
            binge?.let {
                val updatedList = it.entertainmentList.toMutableList()
                if (!updatedList.any { item -> item.id == entertainment.id }) {
                    updatedList.add(entertainment.toStored())
                    bingeRef.update("entertainmentList", updatedList).await()
                }
            }
        } catch (e: Exception) {
            Log.e("BingeModel", "Error adding entertainment to binge: ${e.message}")
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
            releaseDate = releaseDate
        )
        is TVShow -> StoredEntertainmentItem(
            id = id,
            title = title,
            posterPath = posterPath,
            overview = overview,
            type = type,
            releaseDate = releaseDate,
            totalEpisodes = totalEpisodes,
            watchedEpisodes = watchedEpisodes
        )
    }
}

