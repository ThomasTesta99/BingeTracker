package com.example.bingetracker.models

import android.util.Log
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.api.RetrofitClient
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EntertainmentModel {
    private val _movieList = MutableStateFlow<List<Movie>>(emptyList())
    val movieList : StateFlow<List<Movie>> = _movieList

    private val _tvShowList = MutableStateFlow<List<TVShow>>(emptyList())
    val tvShowList : StateFlow<List<TVShow>> = _tvShowList

    val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun getPopularMovies(){
        try {
            val response = RetrofitClient.api.getPopularMovies(apiKey)
            _movieList.value = response.results
        } catch (e: Exception) {
            Log.e("Entertainment Model", "${e.message}")
        }
    }

    suspend fun getPopularTvShows(){
        try {
            val response = RetrofitClient.api.getPopularTVShows(apiKey)
            _tvShowList.value = response.results
        } catch (e: Exception) {
            Log.e("Entertainment Model", "${e.message}")
        }
    }
}