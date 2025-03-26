package com.example.bingetracker.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.EntertainmentModel


@Composable
fun Entertainment(entertainmentModel: EntertainmentModel, movieList: List<Movie>, tvShowList: List<TVShow>) {

    var selectedItem by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        entertainmentModel.getPopularMovies()
        entertainmentModel.getPopularTvShows()
    }

    Scaffold{ padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(movieList){ movie ->
                    ItemCard(movie){selectedItem = movie}
                }
                items(tvShowList){tvShow ->
                    ItemCard(tvShow) { selectedItem = tvShow }
                }
            }
        }

        selectedItem?.let{
            ItemPopup(item = it, onDismiss = {selectedItem = null})
        }
    }
}

@Composable
fun ItemCard(item : Any, onClick: () -> Unit){
    val title = if(item is Movie) item.title else (item as TVShow).title
    val posterPath = if(item is Movie) item.posterPath else (item as TVShow).posterPath

    if(!posterPath.isNullOrEmpty()){
        Column(modifier = Modifier.padding(8.dp).clickable { onClick() }) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500$posterPath",
                contentDescription = title,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ItemPopup(item: Any, onDismiss: () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val title = if (item is Movie) item.title else (item as TVShow).title
                val posterPath = if (item is Movie) item.posterPath else (item as TVShow).posterPath
                val overview = if (item is Movie) item.overview else (item as TVShow).overview

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!posterPath.isNullOrEmpty()) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500$posterPath",
                            contentDescription = title,
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = overview,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Binge")
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Existing Binge")
                    }
                }
            }
        }
    }





}


