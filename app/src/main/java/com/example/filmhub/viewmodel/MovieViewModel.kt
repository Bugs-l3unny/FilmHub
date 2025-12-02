package com.example.filmhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmhub.data.model.*
import com.example.filmhub.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieListState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedYear: Int? = null,
    val selectedGenres: List<Int> = emptyList()
)

data class MovieDetailState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    val reviews: List<Review> = emptyList(),
    val stats: MovieStats? = null,
    val userRating: Rating? = null,
    val trailers: List<VideoTrailer> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MovieViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _movieListState = MutableStateFlow(MovieListState())
    val movieListState: StateFlow<MovieListState> = _movieListState.asStateFlow()

    private val _movieDetailState = MutableStateFlow(MovieDetailState())
    val movieDetailState: StateFlow<MovieDetailState> = _movieDetailState.asStateFlow()

    init {
        loadPopularMovies()
        loadGenres()
    }

    // HU13-EP05: Cargar películas populares
    fun loadPopularMovies() {
        viewModelScope.launch {
            _movieListState.value = _movieListState.value.copy(isLoading = true)

            val result = repository.getPopularMovies()
            result.onSuccess { response ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    movies = response.results,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar géneros
    private fun loadGenres() {
        viewModelScope.launch {
            val result = repository.getGenres()
            result.onSuccess { genres ->
                _movieListState.value = _movieListState.value.copy(genres = genres)
            }
        }
    }

    // HU23-EP10: Buscar películas
    fun searchMovies(query: String) {
        viewModelScope.launch {
            _movieListState.value = _movieListState.value.copy(
                isLoading = true,
                searchQuery = query
            )

            if (query.isBlank()) {
                loadPopularMovies()
                return@launch
            }

            val result = repository.searchMovies(query)
            result.onSuccess { response ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    movies = response.results,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU24-EP11 y HU25-EP12: Filtrar películas
    fun filterMovies(year: Int? = null, genreIds: List<Int>? = null) {
        viewModelScope.launch {
            _movieListState.value = _movieListState.value.copy(
                isLoading = true,
                selectedYear = year,
                selectedGenres = genreIds ?: emptyList()
            )

            val result = repository.discoverMovies(year = year, genreIds = genreIds)
            result.onSuccess { response ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    movies = response.results,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _movieListState.value = _movieListState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Limpiar filtros
    fun clearFilters() {
        _movieListState.value = _movieListState.value.copy(
            selectedYear = null,
            selectedGenres = emptyList(),
            searchQuery = ""
        )
        loadPopularMovies()
    }

    // Cargar detalles de película y sus reseñas
    fun loadMovieDetails(movie: Movie, userId: String?) {
        viewModelScope.launch {
            _movieDetailState.value = _movieDetailState.value.copy(
                isLoading = true,
                movie = movie
            )

            // Cargar detalles completos desde TMDb
            val movieResult = repository.getMovie(movie.id)
            val fullMovie = movieResult.getOrNull() ?: movie

            // Cargar reseñas
            val reviewsResult = repository.getMovieReviews(movie.id)
            val reviews = reviewsResult.getOrNull() ?: emptyList()

            // Cargar estadísticas
            val statsResult = repository.getMovieStats(movie.id)
            val stats = statsResult.getOrNull()

            // Cargar calificación del usuario
            val userRating = if (!userId.isNullOrBlank()) {
                repository.getUserRating(movie.id, userId).getOrNull()
            } else null

            // Cargar trailers
            val trailers = repository.getMovieTrailers(movie.id).getOrNull() ?: emptyList()

            _movieDetailState.value = _movieDetailState.value.copy(
                isLoading = false,
                movie = fullMovie,
                reviews = reviews,
                stats = stats,
                userRating = userRating,
                trailers = trailers
            )
        }
    }

    fun startReviewsListener(movieId: Int) {
        viewModelScope.launch {
            repository.reviewsListener(movieId).collect { reviews ->
                _movieDetailState.value = _movieDetailState.value.copy(reviews = reviews)
            }
        }
    }

    fun playTrailer(context: android.content.Context, trailer: VideoTrailer?) {
        if (trailer == null) return
        val appIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("vnd.youtube:" + trailer.key))
        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=" + trailer.key))
        try {
            context.startActivity(appIntent)
        } catch (_: Exception) {
            context.startActivity(webIntent)
        }
    }

    // HU29-EP14: Calificar película
    fun rateMovie(movieId: Int, userId: String, rating: Float) {
        viewModelScope.launch {
            val newRating = Rating(
                movieId = movieId,
                userId = userId,
                rating = rating
            )

            val result = repository.rateMovie(newRating)
            result.onSuccess {
                // HU31-EP14: Mensaje de confirmación
                _movieDetailState.value = _movieDetailState.value.copy(
                    successMessage = "Calificación guardada",
                    userRating = newRating
                )
                // Recargar estadísticas (HU33-EP15)
                loadMovieStats(movieId)
            }.onFailure { exception ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU27-EP13: Crear reseña
    fun createReview(movieId: Int, userId: String, userName: String, userEmail: String, rating: Float, reviewText: String) {
        viewModelScope.launch {
            val review = Review(
                movieId = movieId,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                rating = rating,
                reviewText = reviewText
            )

            val result = repository.createReview(review)
            result.onSuccess {
                _movieDetailState.value = _movieDetailState.value.copy(
                    successMessage = "Reseña publicada"
                )
                // Recargar reseñas
                loadMovieReviews(movieId)
            }.onFailure { exception ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU28-EP13: Editar reseña
    fun updateReview(review: Review) {
        viewModelScope.launch {
            val result = repository.updateReview(review)
            result.onSuccess {
                _movieDetailState.value = _movieDetailState.value.copy(
                    successMessage = "Reseña actualizada"
                )
                loadMovieReviews(review.movieId)
            }.onFailure { exception ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU28-EP13: Eliminar reseña
    fun deleteReview(reviewId: String, movieId: Int) {
        viewModelScope.launch {
            val result = repository.deleteReview(reviewId, movieId)
            result.onSuccess {
                _movieDetailState.value = _movieDetailState.value.copy(
                    successMessage = "Reseña eliminada"
                )
                loadMovieReviews(movieId)
            }.onFailure { exception ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    private fun loadMovieReviews(movieId: Int) {
        viewModelScope.launch {
            val result = repository.getMovieReviews(movieId)
            result.onSuccess { reviews ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    reviews = reviews
                )
            }
        }
    }

    private fun loadMovieStats(movieId: Int) {
        viewModelScope.launch {
            val result = repository.getMovieStats(movieId)
            result.onSuccess { stats ->
                _movieDetailState.value = _movieDetailState.value.copy(
                    stats = stats
                )
            }
        }
    }

    fun resetMessages() {
        _movieDetailState.value = _movieDetailState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
