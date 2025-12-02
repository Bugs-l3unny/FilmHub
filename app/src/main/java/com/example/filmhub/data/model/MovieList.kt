package com.example.filmhub.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

// HU34-EP18, HU35-EP18, HU36-EP18: Lista personalizada
@IgnoreExtraProperties
data class MovieList(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val movieIds: List<Int> = emptyList(),
    val isPublic: Boolean = true, // HU40-EP20
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// HU37-EP19: Película en lista de pendientes
data class WatchlistItem(
    val id: String = "",
    val userId: String = "",
    val movieId: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// HU45-EP13: Película favorita
data class FavoriteMovie(
    val id: String = "",
    val userId: String = "",
    val movieId: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// Para guardar detalles de películas en listas
data class ListMovieDetail(
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val releaseDate: String = "",
    val voteAverage: Double = 0.0
)

// HU41-EP21: Película de próximo estreno
data class UpcomingMovieItem(
    val movie: Movie,
    val releaseDate: String = ""
)
