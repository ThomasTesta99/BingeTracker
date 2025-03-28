package com.example.bingetracker.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bingetracker.data.EntertainmentType
import com.example.bingetracker.data.Movie
import com.example.bingetracker.data.TVShow
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.models.BingeModel

@Composable
fun AllBingesScreen(navController: NavController, authModel: AuthModel) {
    val user by authModel.currentUser.collectAsState()
    val bingeModel: BingeModel = viewModel()

    if (user != null) {
        bingeModel.getUserBinges(user!!.uuid)
    }

    val bingeList by bingeModel.userBinges.collectAsState()

    LazyColumn {
        items(bingeList) { binge ->
            Column {
                Text(text = binge.name)

                Spacer(modifier = Modifier.height(8.dp))

                binge.entertainmentList.forEach { entertainment ->
                    Text(text = "- ${entertainment.title}")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
