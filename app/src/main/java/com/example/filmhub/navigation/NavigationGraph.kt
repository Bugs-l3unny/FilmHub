package com.example.filmhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.filmhub.data.repository.AuthRepository
import com.example.filmhub.ui.screens.auth.ForgotPasswordScreen
import com.example.filmhub.ui.screens.auth.LoginScreen
import com.example.filmhub.ui.screens.auth.RegisterScreen
import com.example.filmhub.ui.screens.movies.MovieListScreen
import com.example.filmhub.ui.screens.movies.MovieDetailScreen
import com.example.filmhub.ui.screens.lists.MyListsScreen
import com.example.filmhub.ui.screens.lists.WatchlistScreen
import com.example.filmhub.ui.screens.lists.UpcomingMoviesScreen
import com.example.filmhub.ui.screens.admin.AdminPanelScreen
import com.example.filmhub.ui.screens.support.HelpCenterScreen
import com.example.filmhub.ui.screens.support.SupportTicketScreen
import com.example.filmhub.ui.screens.profile.ProfileScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String
) {
    val authRepo = AuthRepository()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.MovieList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.MovieList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MovieList.route) {
            MovieListScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMovieDetail = { movie ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                    navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                },
                onNavigateToMyLists = {
                    if (authRepo.isUserAuthenticated()) {
                        navController.navigate(Screen.MyLists.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onNavigateToWatchlist = {
                    if (authRepo.isUserAuthenticated()) {
                        navController.navigate(Screen.Watchlist.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onNavigateToUpcoming = {
                    navController.navigate(Screen.UpcomingMovies.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.MovieDetail.route) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
            val movie = navController.previousBackStackEntry?.savedStateHandle?.get<com.example.filmhub.data.model.Movie>("movie")

            if (movie != null) {
                MovieDetailScreen(
                    movie = movie,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.MyLists.route) {
            MyListsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToListDetail = { list ->
                    // Por ahora solo volver
                }
            )
        }

        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMovieDetail = { movie ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                    navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                }
            )
        }

        composable(Screen.UpcomingMovies.route) {
            UpcomingMoviesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMovieDetail = { movie ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                    navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                }
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReports = {
                    // Ya está en la misma pantalla con tabs
                }
            )
        }

        composable(Screen.HelpCenter.route) {
            HelpCenterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSupport = {
                    if (authRepo.isUserAuthenticated()) {
                        navController.navigate(Screen.SupportTicket.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.SupportTicket.route) {
            SupportTicketScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route)
                },
                onNavigateToHelp = {
                    navController.navigate(Screen.HelpCenter.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
    }
}
