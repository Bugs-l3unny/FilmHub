package com.example.filmhub

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.filmhub.data.ThemeManager
import com.example.filmhub.data.repository.AuthRepository
import com.example.filmhub.navigation.NavigationGraph
import com.example.filmhub.navigation.Screen
import com.example.filmhub.notifications.MoviesNotifyWorker
import com.example.filmhub.ui.theme.FilmHubTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var themeManager: ThemeManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeManager = ThemeManager(this)
        authRepository = AuthRepository()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)

            FilmHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val startDestination = Screen.MovieList.route
                    NavigationGraph(
                        navController = navController,
                        startDestination = startDestination
                    )

                    val startRoute = intent?.getStringExtra("startRoute")
                    LaunchedEffect(startRoute) {
                        if (startRoute != null) {
                            navController.navigate(startRoute)
                        }
                    }
                }
            }
        }

        MoviesNotifyWorker.schedule(this)

        MoviesNotifyWorker.triggerOnce(this)
    }
}
