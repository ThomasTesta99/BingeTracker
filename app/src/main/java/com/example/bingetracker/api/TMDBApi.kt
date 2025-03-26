package com.example.bingetracker.api

import com.example.bingetracker.data.MovieResponse
import com.example.bingetracker.data.TVShowResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("tv/popular")
    suspend fun getPopularTVShows(
        @Query("api_key") apiKey :String,
        @Query("page") page: Int = 1
    ): TVShowResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): MovieResponse

    @GET("search/tv")
    suspend fun searchTVShows(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TVShowResponse


}