package com.example.bingetracker.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel

@Composable
fun AllBingesScreen(navController: NavController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val currentUserAuth by authModel.currentUserAuth.collectAsState()

    val bingeModel: BingeModel = viewModel()
    val bingeList by bingeModel.userBinges.collectAsState()

    LaunchedEffect(user?.uuid) {
        user?.uuid?.let { bingeModel.getUserBinges(it) }
    }

    LaunchedEffect(currentUserAuth) {
        if (currentUserAuth == null) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ){
        items(bingeList){ binge->
            Card(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("bingeDetail/${binge.id}")
                    }
            ){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        binge.entertainmentList.forEach {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                                contentDescription = it.title,
                                modifier = Modifier
                                    .height(100.dp)
                                    .padding(top = 10.dp, end = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val total = binge.entertainmentList.sumOf { item ->
                        if (item is TVShow) item.episodes!!.size else 1
                    }
                    val watchedItems = binge.entertainmentList.sumOf { item ->
                        when (item) {
                            is Movie -> if (item.watched) 1 else 0
                            is TVShow -> item.watchedEpisodes?.size ?: 0
                        }
                    }
                    val progress = if (total > 0) watchedItems.toFloat() / total else 0f

                    Text(
                        text = "${binge.name} \n ${(progress * 100).toInt()} %  watched",
                        textAlign = TextAlign.Center,
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                }
            }
        }
    }

}
