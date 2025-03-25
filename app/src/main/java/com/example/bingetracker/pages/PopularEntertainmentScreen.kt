package com.example.bingetracker.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.bingetracker.models.EntertainmentModel


@Composable
fun PopularEntertainment(){
    val entertainmentModel = EntertainmentModel()

    val popularMovies = entertainmentModel.movieList.collectAsState()
    val popularTVShows = entertainmentModel.tvShowList.collectAsState()

    LaunchedEffect(Unit) {
        entertainmentModel.getPopularMovies()
        entertainmentModel.getPopularTvShows()
    }

    // Your UI Code
    Column {
        // Your search bar here

        // Current Binge Section (For now, just a placeholder)
        Text("Current Binges Section")

        // Popular Movies Section
//        Text("Popular Movies")
//        LazyColumn {
//            items(popularMovies.value){ movie ->
//                Text(movie.title)
//            }
//        }
//
//        Text("Popular TV Shows")
//        LazyColumn {
//            items(popularTVShows.value){ TVshow ->
//                Text(TVshow.title)
//            }
//        }
    }
}
