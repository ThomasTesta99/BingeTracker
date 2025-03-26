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

    private val _searchMovieResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchMovieResults : StateFlow<List<Movie>> = _searchMovieResults

    private val _searchTVResults = MutableStateFlow<List<TVShow>>(emptyList())
    val searchTVResults : StateFlow<List<TVShow>> = _searchTVResults

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

    private suspend fun searchEntertainment(query: String){
        if(query.isBlank()){
            _searchMovieResults.value = emptyList()
            _searchTVResults.value = emptyList()
        }else{
            try {
                val movieResponse = RetrofitClient.api.searchMovies(apiKey, query)
                val tvResponse = RetrofitClient.api.searchTVShows(apiKey, query)

                val movies = movieResponse.results ?: emptyList()
                val tvShows = tvResponse.results ?: emptyList()

                val filteredMovies = movies
                    .filter {
                        it.releaseDate != null && it.title.contains(query, ignoreCase = true) // Match title with query (case insensitive)
                    }
                    .sortedByDescending { it.releaseDate } // Sort by release date for movies
                    .take(10)

                val filteredTVShows = tvShows
                    .filter {
                        it.airDate != null && it.title.contains(query, ignoreCase = true) // Match title with query (case insensitive)
                    }
                    .sortedByDescending { it.airDate } // Sort by air date for TV shows
                    .take(10)

                _searchMovieResults.value = filteredMovies
                _searchTVResults.value = filteredTVShows

            }catch (e: Exception){
                Log.e("Entertainment Model","${e.message}")
            }
        }
    }

    fun searchForEntertainment(query: String){
        viewModelScope.launch {
            searchEntertainment(query)
        }
    }
}