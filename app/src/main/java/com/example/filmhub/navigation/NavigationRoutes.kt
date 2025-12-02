package com.example.filmhub.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object MovieList : Screen("movie_list")
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: Int) = "movie_detail/$movieId"
    }
    object Search : Screen("search")
    object Filter : Screen("filter")
    object MyLists : Screen("my_lists")
    object Watchlist : Screen("watchlist")
    object UpcomingMovies : Screen("upcoming_movies")
    object AdminPanel : Screen("admin_panel")
    object HelpCenter : Screen("help_center")
    object SupportTicket : Screen("support_ticket")
}