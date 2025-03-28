package com.example.bingetracker.data

import com.google.gson.annotations.SerializedName

data class StoredEntertainmentItem(
    val id: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val overview: String = "",
    val type: EntertainmentType = EntertainmentType.MOVIE,
    val releaseDate: String? = null,
    val totalEpisodes: Int? = null,
    val watchedEpisodes: List<Int>? = emptyList()
)

// Base Sealed Class
sealed class EntertainmentItem {
    abstract val id: Int
    abstract val title: String
    abstract val posterPath: String?
    abstract val type: EntertainmentType
    abstract val overview: String
}

enum class EntertainmentType {
    MOVIE,
    TV_SHOW
}

// Movie data class now extends EntertainmentItem
data class Movie(
    override val id: Int,
    override val title: String,
    @SerializedName("poster_path") override val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    override val overview: String
) : EntertainmentItem() {
    override val type: EntertainmentType = EntertainmentType.MOVIE
}

// TV Show data class now extends EntertainmentItem
data class TVShow(
    override val id: Int,
    @SerializedName("original_name") override val title: String,
    @SerializedName("poster_path") override val posterPath: String?,
    @SerializedName("first_air_date") val releaseDate: String?,
    override val overview: String,
    val totalEpisodes: Int?,
    val watchedEpisodes: List<Int>? = emptyList()
) : EntertainmentItem() {
    override val type: EntertainmentType = EntertainmentType.TV_SHOW
}

// Responses for Movie and TVShow
data class MovieResponse(
    val results: List<Movie>
)

data class TVShowResponse(
    val results: List<TVShow>
)
