package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.api.RetrofitClient
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EntertainmentModel : ViewModel() {
    private val _movieList = MutableStateFlow<List<Movie>>(emptyList())
    val movieList : StateFlow<List<Movie>> = _movieList

    private val _tvShowList = MutableStateFlow<List<TVShow>>(emptyList())
    val tvShowList : StateFlow<List<TVShow>> = _tvShowList

    val apiKey = BuildConfig.TMDB_API_KEY

    init {
        viewModelScope.launch {
            getPopularMovies()
            getPopularTvShows()
        }
    }

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
            Log.d("Entertainment Model", "${response}")
            _tvShowList.value = response.results
        } catch (e: Exception) {
            Log.e("Entertainment Model", "${e.message}")
        }
    }
}