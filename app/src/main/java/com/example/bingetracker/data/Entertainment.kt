package com.example.bingetracker.data

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val results: List<Movie>
)

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,  // Maps JSON field "poster_path" to posterPath
    @SerializedName("release_date") val releaseDate: String?, // Maps "release_date" to releaseDate
    val overview: String
)

data class TVShowResponse(
    val results: List<TVShow>
)

data class TVShow(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,  // Maps JSON field "poster_path" to posterPath
    @SerializedName("first_air_date") val airDate: String?, // Maps "release_date" to releaseDate
    val overview: String
)
