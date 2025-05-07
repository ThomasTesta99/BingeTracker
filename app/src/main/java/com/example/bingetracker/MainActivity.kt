package com.example.bingetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bingetracker.models.AuthModel
import com.example.bingetracker.navigation.AppNavigation
import com.example.bingetracker.ui.theme.BingeTrackerTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent{
            val navController = rememberNavController()
            val authModel: AuthModel = viewModel()
            BingeTrackerTheme {
                AppNavigation(navController, authModel)
            }
        }
    }
}