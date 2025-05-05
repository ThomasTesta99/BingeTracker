package com.example.bingetracker.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bingetracker.BuildConfig
import com.example.bingetracker.api.RetrofitClient
import com.example.bingetracker.data.Episode
import com.example.bingetracker.data.HomeContentFilter
import com.example.bingetracker.data.HomeContentSort
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

    // Add filter and sort state
    private val _currentFilter = MutableStateFlow(HomeContentFilter.ALL)
    val currentFilter: StateFlow<HomeContentFilter> = _currentFilter

    private val _currentSort = MutableStateFlow(HomeContentSort.POPULARITY)
    val currentSort: StateFlow<HomeContentSort> = _currentSort

    // Filtered content for the UI
    private val _filteredMovies = MutableStateFlow<List<Movie>>(emptyList())
    val filteredMovies: StateFlow<List<Movie>> = _filteredMovies

    private val _filteredTVShows = MutableStateFlow<List<TVShow>>(emptyList())
    val filteredTVShows: StateFlow<List<TVShow>> = _filteredTVShows

    private val _entertainmentState = MutableStateFlow<EntertainmentState>(EntertainmentState.Idle)
    val entertainmentState : StateFlow<EntertainmentState> = _entertainmentState

    val apiKey = BuildConfig.TMDB_API_KEY

    init {
        viewModelScope.launch {
            getPopularMovies()
            getPopularTvShows()
        }
    }

    suspend fun getPopularMovies(){
        try {
            _entertainmentState.value = EntertainmentState.Loading
            val response = RetrofitClient.api.getPopularMovies(apiKey)
            _movieList.value = response.results
            applyFilter() // apply filter after fetching
            _entertainmentState.value = EntertainmentState.Success
        } catch (e: Exception) {
            Log.e("Entertainment Model", "${e.message}")
            _entertainmentState.value = EntertainmentState.Error("${e.message}")
        }
    }

    suspend fun getPopularTvShows(){
        try {
            _entertainmentState.value = EntertainmentState.Loading
            val response = RetrofitClient.api.getPopularTVShows(apiKey)
            _tvShowList.value = response.results
            applyFilter() // Apply filter after fetching
            _entertainmentState.value = EntertainmentState.Success
        } catch (e: Exception) {
            Log.e("Entertainment Model", "${e.message}")
            _entertainmentState.value = EntertainmentState.Error("${e.message}")
        }
    }

    private suspend fun searchEntertainment(query: String){
        if(query.isBlank()){
            _searchMovieResults.value = emptyList()
            _searchTVResults.value = emptyList()
        }else{
            try {
                _entertainmentState.value = EntertainmentState.Loading
                val movieResponse = RetrofitClient.api.searchMovies(apiKey, query)
                val tvResponse = RetrofitClient.api.searchTVShows(apiKey, query)
                _searchMovieResults.value = movieResponse.results
                _searchTVResults.value = tvResponse.results
                _entertainmentState.value = EntertainmentState.Success
            }catch (e: Exception){
                Log.e("Entertainment Model","${e.message}")
                _entertainmentState.value = EntertainmentState.Error("${e.message}")
            }
        }
    }

    fun searchForEntertainment(query: String){
        viewModelScope.launch {
            searchEntertainment(query)
        }
    }

    fun updateFilter(filter: HomeContentFilter) {
        _currentFilter.value = filter
        applyFilter()
    }

    fun updateSort(sort: HomeContentSort){
        _currentSort.value = sort
        applyFilter()
    }

    private fun applyFilter() {
        viewModelScope.launch {
            // Apply content type filter
            when (_currentFilter.value) {
                HomeContentFilter.ALL -> {
                    _filteredMovies.value = _movieList.value
                    _filteredTVShows.value = _tvShowList.value
                }
                HomeContentFilter.MOVIES -> {
                    _filteredMovies.value = _movieList.value
                    _filteredTVShows.value = emptyList()
                }
                HomeContentFilter.TV_SHOWS -> {
                    _filteredMovies.value = emptyList()
                    _filteredTVShows.value = _tvShowList.value
                }
            }

            // Apply sorting
            when (_currentSort.value) {
                HomeContentSort.POPULARITY -> {
                    // Already sorted by popularity from API
                }
                HomeContentSort.NEWEST -> {
                    _filteredMovies.value = _filteredMovies.value.sortedByDescending { it.releaseDate }
                    _filteredTVShows.value = _filteredTVShows.value.sortedByDescending { it.releaseDate }
                }
                HomeContentSort.RATING -> {
                    _filteredMovies.value = _filteredMovies.value.sortedBy{it.rating}
                    _filteredTVShows.value = _filteredTVShows.value.sortedBy{it.rating}
                }
            }
        }
    }
}

sealed class EntertainmentState{
    object Idle : EntertainmentState()
    object Loading : EntertainmentState()
    object Success : EntertainmentState()
    data class Error(val message : String) : EntertainmentState()
}