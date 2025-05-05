package com.example.bingetracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BingeFireStore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<StoredEntertainmentItem> = emptyList(),
    val lastUpdated: Timestamp? = null
)

data class Binge(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<EntertainmentItem> = emptyList(),
    val lastUpdated: Timestamp? = null,
    val progress : Float = 0f
)
