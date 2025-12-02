package com.example.filmhub.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("original_title")
    val originalTitle: String = "",
    val overview: String = "",
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("backdrop_path")
    val backdropPath: String? = null,
    @SerializedName("release_date")
    val releaseDate: String = "",
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,
    @SerializedName("vote_count")
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerializedName("genre_ids")
    val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    @SerializedName("original_language")
    val originalLanguage: String = ""
) : Parcelable

data class MovieResponse(
    val page: Int = 0,
    val results: List<Movie> = emptyList(),
    @SerializedName("total_pages")
    val totalPages: Int = 0,
    @SerializedName("total_results")
    val totalResults: Int = 0
)

data class Genre(
    val id: Int = 0,
    val name: String = ""
)

data class GenreResponse(
    val genres: List<Genre> = emptyList()
)

// Modelo para reseñas
data class Review(
    val id: String = "",
    val movieId: Int = 0,
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val rating: Float = 0f,
    val reviewText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Modelo para calificaciones
data class Rating(
    val id: String = "",
    val movieId: Int = 0,
    val userId: String = "",
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

// Modelo para estadísticas de película
data class MovieStats(
    val movieId: Int = 0,
    val averageRating: Float = 0f,
    val totalRatings: Int = 0,
    val totalReviews: Int = 0
)