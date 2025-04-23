package com.example.bingetracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.time.LocalDateTime

data class BingeFireStore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<StoredEntertainmentItem> = emptyList(),
    val lastUpdated: Timestamp
)

data class Binge(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<EntertainmentItem> = emptyList(),
    val lastUpdated: Timestamp
)
