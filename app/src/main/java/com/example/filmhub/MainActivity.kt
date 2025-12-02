package com.example.filmhub

import android.os.Bundle
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
import com.example.filmhub.ui.theme.FilmHubTheme

class MainActivity : ComponentActivity() {

    private lateinit var themeManager: ThemeManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeManager = ThemeManager(this)
        authRepository = AuthRepository()

        setContent {
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)

            FilmHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Soporte invitado: inicia en la lista incluso sin sesión; las acciones protegidas pedirán login
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
    }
}
