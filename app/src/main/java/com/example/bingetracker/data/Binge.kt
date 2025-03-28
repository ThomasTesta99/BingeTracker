package com.example.bingetracker.data

import com.google.firebase.firestore.DocumentId

data class BingeFireStore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<StoredEntertainmentItem> = emptyList()
)

data class Binge(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val entertainmentList: List<EntertainmentItem> = emptyList()
)
