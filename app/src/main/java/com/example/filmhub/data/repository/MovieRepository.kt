package com.example.filmhub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.filmhub.data.model.*
import com.example.filmhub.data.remote.RetrofitClient
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.ListenerRegistration

class MovieRepository {

    private val api = RetrofitClient.tmdbApi
    private val firestore = FirebaseFirestore.getInstance()

    // HU13-EP05: Obtener películas populares
    suspend fun getPopularMovies(page: Int = 1): Result<MovieResponse> {
        return try {
            val response = api.getPopularMovies(page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar películas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener películas mejor calificadas
    suspend fun getTopRatedMovies(page: Int = 1): Result<MovieResponse> {
        return try {
            val response = api.getTopRatedMovies(page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar películas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU23-EP10: Buscar películas
    suspend fun searchMovies(query: String, page: Int = 1): Result<MovieResponse> {
        return try {
            val response = api.searchMovies(query = query, page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en la búsqueda"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU24-EP11 y HU25-EP12: Filtrar películas
    suspend fun discoverMovies(
        year: Int? = null,
        genreIds: List<Int>? = null,
        page: Int = 1
    ): Result<MovieResponse> {
        return try {
            val genreString = genreIds?.joinToString(",")
            val response = api.discoverMovies(
                year = year,
                genreIds = genreString,
                page = page
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al filtrar películas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener géneros
    suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val response = api.getGenres()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.genres)
            } else {
                Result.failure(Exception("Error al cargar géneros"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener detalles de película por ID
    suspend fun getMovie(movieId: Int): Result<Movie> {
        return try {
            val response = api.getMovieDetails(movieId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar detalles de película"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU27-EP13: Crear reseña
    suspend fun createReview(review: Review): Result<String> {
        return try {
            val docRef = firestore.collection("reviews").document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId).await()
            updateMovieStats(review.movieId)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU28-EP13: Editar o eliminar reseña
    suspend fun updateReview(review: Review): Result<Unit> {
        return try {
            val updatedReview = review.copy(updatedAt = System.currentTimeMillis())
            firestore.collection("reviews")
                .document(review.id)
                .set(updatedReview)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String, movieId: Int): Result<Unit> {
        return try {
            firestore.collection("reviews")
                .document(reviewId)
                .delete()
                .await()
            updateMovieStats(movieId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener reseñas de una película (sin requerir índice compuesto)
    suspend fun getMovieReviews(movieId: Int): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            val reviews = snapshot.documents
                .mapNotNull { it.toObject(Review::class.java) }
                .sortedByDescending { it.createdAt }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Listener en tiempo real: reseñas por película (sin requerir índice compuesto)
    fun reviewsListener(movieId: Int): Flow<List<Review>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("reviews")
            .whereEqualTo("movieId", movieId)
            .addSnapshotListener { snapshot, _ ->
                val reviews = snapshot?.documents
                    ?.mapNotNull { it.toObject(Review::class.java) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(reviews).isSuccess
            }
        awaitClose { registration.remove() }
    }

    // HU29-EP14: Calificar película
    suspend fun rateMovie(rating: Rating): Result<String> {
        return try {
            // Buscar si ya existe una calificación del usuario para esta película
            val existingRating = firestore.collection("ratings")
                .whereEqualTo("movieId", rating.movieId)
                .whereEqualTo("userId", rating.userId)
                .get()
                .await()

            if (!existingRating.isEmpty) {
                // HU30-EP14: Modificar calificación existente
                val docId = existingRating.documents[0].id
                firestore.collection("ratings")
                    .document(docId)
                    .set(rating.copy(id = docId))
                    .await()
                updateMovieStats(rating.movieId)
                Result.success(docId)
            } else {
                // Crear nueva calificación
                val docRef = firestore.collection("ratings").document()
                val ratingWithId = rating.copy(id = docRef.id)
                docRef.set(ratingWithId).await()
                updateMovieStats(rating.movieId)
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU32-EP15 y HU33-EP15: Obtener promedio de calificaciones
    suspend fun getMovieStats(movieId: Int): Result<MovieStats> {
        return try {
            val ratingsSnapshot = firestore.collection("ratings")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            val ratings = ratingsSnapshot.documents.mapNotNull {
                it.toObject(Rating::class.java)
            }

            val reviewsSnapshot = firestore.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            val averageRating = if (ratings.isNotEmpty()) {
                ratings.map { it.rating }.average().toFloat()
            } else {
                0f
            }

            val stats = MovieStats(
                movieId = movieId,
                averageRating = averageRating,
                totalRatings = ratings.size,
                totalReviews = reviewsSnapshot.size()
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar estadísticas de película en tiempo real (HU33-EP15)
    private suspend fun updateMovieStats(movieId: Int) {
        try {
            val stats = getMovieStats(movieId).getOrNull()
            if (stats != null) {
                firestore.collection("movie_stats")
                    .document(movieId.toString())
                    .set(stats)
                    .await()
            }
        } catch (e: Exception) {
            // Log error pero no fallar
        }
    }

    // Obtener calificación del usuario
    suspend fun getUserRating(movieId: Int, userId: String): Result<Rating?> {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val rating = snapshot.documents.firstOrNull()?.toObject(Rating::class.java)
            Result.success(rating)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU42-EP22: Obtener videos/trailers de una película (YouTube Trailer)
    suspend fun getMovieTrailers(movieId: Int): Result<List<VideoTrailer>> {
        return try {
            val response = api.getMovieVideos(movieId)
            if (response.isSuccessful && response.body() != null) {
                val trailers = response.body()!!.results
                    .filter { it.site.equals("YouTube", true) && it.type.equals("Trailer", true) }
                Result.success(trailers)
            } else {
                Result.failure(Exception("Error al cargar trailers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
