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
    val watchedEpisodes: List<EpisodeWatched> = emptyList(),
    val episodes: List<Episode>? = emptyList(),
    val watched: Boolean = false,
)

data class EpisodeWatched(
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0
)

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

data class Movie(
    override val id: Int,
    override val title: String,
    @SerializedName("poster_path") override val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    override val overview: String,
    val watched: Boolean = false,
    @SerializedName("vote_average") val rating : Float? = null
) : EntertainmentItem() {
    override val type: EntertainmentType = EntertainmentType.MOVIE
}

data class TVShow(
    override val id: Int,
    @SerializedName("original_name") override val title: String,
    @SerializedName("poster_path") override val posterPath: String?,
    @SerializedName("first_air_date") val releaseDate: String?,
    override val overview: String,
    val totalEpisodes: Int?,
    val watchedEpisodes: List<EpisodeWatched> = emptyList(),
    val episodes: List<Episode>? = emptyList(),
    @SerializedName("vote_average") val rating : Float? = null
) : EntertainmentItem() {
    override val type: EntertainmentType = EntertainmentType.TV_SHOW
}

data class MovieResponse(
    val results: List<Movie>
)

data class TVShowResponse(
    val results: List<TVShow>
)

data class TvDetailsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("seasons") val seasons: List<SeasonInfo>
)

data class SeasonInfo(
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int
)

data class TvSeasonResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episodes") val episodes: List<Episode>
)

data class Episode(
    @SerializedName("episode_number") val episodeNumber: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("name") val title: String = "",
)
