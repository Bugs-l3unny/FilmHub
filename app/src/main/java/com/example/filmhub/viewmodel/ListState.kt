package com.example.filmhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmhub.data.model.Movie
import com.example.filmhub.data.model.MovieList
import com.example.filmhub.data.repository.ListRepository
import com.example.filmhub.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListState(
    val isLoading: Boolean = false,
    val userLists: List<MovieList> = emptyList(),
    val publicLists: List<MovieList> = emptyList(),
    val watchlistMovies: List<Movie> = emptyList(),
    val favoriteMovies: List<Movie> = emptyList(),
    val upcomingMovies: List<Movie> = emptyList(),
    val selectedList: MovieList? = null,
    val listMovies: List<Movie> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isInWatchlist: Boolean = false,
    val isInFavorites: Boolean = false
)

class ListViewModel(
    private val listRepository: ListRepository = ListRepository(),
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _listState = MutableStateFlow(ListState())
    val listState: StateFlow<ListState> = _listState.asStateFlow()

    fun startRealtime(userId: String) {
        // Watchlist
        viewModelScope.launch {
            listRepository.watchlistListener(userId).collect { ids ->
                val movies = ids.mapNotNull { movieRepository.getMovie(it).getOrNull() }
                _listState.value = _listState.value.copy(watchlistMovies = movies)
            }
        }
        // Favorites
        viewModelScope.launch {
            listRepository.favoritesListener(userId).collect { ids ->
                val movies = ids.mapNotNull { movieRepository.getMovie(it).getOrNull() }
                _listState.value = _listState.value.copy(favoriteMovies = movies)
            }
        }
        // User lists
        viewModelScope.launch {
            listRepository.userListsListener(userId).collect { lists ->
                _listState.value = _listState.value.copy(userLists = lists)
            }
        }
        // Public lists
        viewModelScope.launch {
            listRepository.publicListsListener().collect { lists ->
                _listState.value = _listState.value.copy(publicLists = lists)
            }
        }
    }

    // HU34-EP18: Crear lista
    fun createList(userId: String, title: String, description: String, isPublic: Boolean) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val list = MovieList(
                userId = userId,
                title = title,
                description = description,
                isPublic = isPublic
            )

            val result = listRepository.createList(list)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    successMessage = "Lista creada correctamente"
                )
                loadUserLists(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU36-EP18: Actualizar lista
    fun updateList(list: MovieList) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.updateList(list)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    successMessage = "Lista actualizada"
                )
                loadUserLists(list.userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Eliminar lista
    fun deleteList(listId: String, userId: String) {
        viewModelScope.launch {
            val result = listRepository.deleteList(listId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Lista eliminada"
                )
                loadUserLists(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU35-EP18: Añadir película a lista
    fun addMovieToList(listId: String, movieId: Int, userId: String) {
        viewModelScope.launch {
            val result = listRepository.addMovieToList(listId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Película añadida a la lista"
                )
                loadUserLists(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU35-EP18: Quitar película de lista
    fun removeMovieFromList(listId: String, movieId: Int, userId: String) {
        viewModelScope.launch {
            val result = listRepository.removeMovieFromList(listId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Película eliminada de la lista"
                )
                loadListDetails(listId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar listas del usuario
    fun loadUserLists(userId: String) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.getUserLists(userId)
            result.onSuccess { lists ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    userLists = lists
                )
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU38-EP20: Cargar listas públicas
    fun loadPublicLists() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.getPublicLists()
            result.onSuccess { lists ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    publicLists = lists
                )
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar detalles de una lista
    fun loadListDetails(listId: String) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            // Buscar la lista
            val allLists = _listState.value.userLists + _listState.value.publicLists
            val list = allLists.find { it.id == listId }

            if (list != null) {
                // Cargar películas de la lista
                val movies = mutableListOf<Movie>()
                list.movieIds.forEach { movieId ->
                    val m = movieRepository.getMovie(movieId).getOrNull()
                    movies.add(m ?: Movie(id = movieId, title = "Película"))
                }

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    selectedList = list,
                    listMovies = movies
                )
            }
        }
    }

    // HU37-EP19: Añadir a lista de pendientes
    fun addToWatchlist(userId: String, movieId: Int) {
        viewModelScope.launch {
            val result = listRepository.addToWatchlist(userId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Añadido a pendientes",
                    isInWatchlist = true
                )
                loadWatchlist(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Quitar de pendientes
    fun removeFromWatchlist(userId: String, movieId: Int) {
        viewModelScope.launch {
            val result = listRepository.removeFromWatchlist(userId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Eliminado de pendientes",
                    isInWatchlist = false
                )
                loadWatchlist(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar lista de pendientes
    fun loadWatchlist(userId: String) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.getWatchlist(userId)
            result.onSuccess { movieIds ->
                // Cargar detalles de cada película
                val movies = mutableListOf<Movie>()
                movieIds.forEach { id ->
                    val m = movieRepository.getMovie(id).getOrNull()
                    movies.add(m ?: Movie(id = id, title = "Película"))
                }

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    watchlistMovies = movies
                )
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU45-EP13: Añadir a favoritos
    fun addToFavorites(userId: String, movieId: Int) {
        viewModelScope.launch {
            val result = listRepository.addToFavorites(userId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Añadido a favoritos",
                    isInFavorites = true
                )
                loadFavorites(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Quitar de favoritos
    fun removeFromFavorites(userId: String, movieId: Int) {
        viewModelScope.launch {
            val result = listRepository.removeFromFavorites(userId, movieId)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    successMessage = "Eliminado de favoritos",
                    isInFavorites = false
                )
                loadFavorites(userId)
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar favoritos
    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.getFavorites(userId)
            result.onSuccess { movieIds ->
                val movies = mutableListOf<Movie>()
                movieIds.forEach { id ->
                    val m = movieRepository.getMovie(id).getOrNull()
                    movies.add(m ?: Movie(id = id, title = "Película"))
                }
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    favoriteMovies = movies
                )
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU41-EP21: Cargar próximos estrenos
    fun loadUpcomingMovies() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)

            val result = listRepository.getUpcomingMovies()
            result.onSuccess { response ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    upcomingMovies = response.results
                )
            }.onFailure { exception ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Verificar estado de película
    fun checkMovieStatus(userId: String, movieId: Int) {
        viewModelScope.launch {
            val watchlistResult = listRepository.isInWatchlist(userId, movieId)
            val favoritesResult = listRepository.isInFavorites(userId, movieId)

            _listState.value = _listState.value.copy(
                isInWatchlist = watchlistResult.getOrNull() ?: false,
                isInFavorites = favoritesResult.getOrNull() ?: false
            )
        }
    }

    fun resetMessages() {
        _listState.value = _listState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
