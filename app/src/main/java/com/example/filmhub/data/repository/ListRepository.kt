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

class ListRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val api = RetrofitClient.tmdbApi

    // HU34-EP18: Crear lista personalizada
    suspend fun createList(list: MovieList): Result<String> {
        return try {
            val docRef = firestore.collection("movie_lists").document()
            val listWithId = list.copy(id = docRef.id)
            docRef.set(listWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Listener en tiempo real: watchlist por usuario
    fun watchlistListener(userId: String): Flow<List<Int>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("watchlist")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents
                    ?.mapNotNull { it.toObject(WatchlistItem::class.java) }
                    ?.sortedByDescending { it.addedAt }
                    ?.map { it.movieId }
                    ?: emptyList()
                trySend(ids).isSuccess
            }
        awaitClose { registration.remove() }
    }

    // Listener en tiempo real: favoritos por usuario
    fun favoritesListener(userId: String): Flow<List<Int>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents
                    ?.mapNotNull { it.toObject(FavoriteMovie::class.java) }
                    ?.sortedByDescending { it.addedAt }
                    ?.map { it.movieId }
                    ?: emptyList()
                trySend(ids).isSuccess
            }
        awaitClose { registration.remove() }
    }

    // Listener en tiempo real: listas del usuario
    fun userListsListener(userId: String): Flow<List<MovieList>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("movie_lists")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val lists = snapshot?.documents
                    ?.mapNotNull { it.toObject(MovieList::class.java) }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()
                trySend(lists).isSuccess
            }
        awaitClose { registration.remove() }
    }

    // Listener en tiempo real: listas públicas
    fun publicListsListener(): Flow<List<MovieList>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("movie_lists")
            .whereEqualTo("isPublic", true)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                val lists = snapshot?.documents
                    ?.mapNotNull { it.toObject(MovieList::class.java) }
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()
                trySend(lists).isSuccess
            }
        awaitClose { registration.remove() }
    }

    // HU36-EP18: Actualizar título y descripción
    suspend fun updateList(list: MovieList): Result<Unit> {
        return try {
            val updatedList = list.copy(updatedAt = System.currentTimeMillis())
            firestore.collection("movie_lists")
                .document(list.id)
                .set(updatedList)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar lista
    suspend fun deleteList(listId: String): Result<Unit> {
        return try {
            firestore.collection("movie_lists")
                .document(listId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU35-EP18: Añadir película a lista
    suspend fun addMovieToList(listId: String, movieId: Int): Result<Unit> {
        return try {
            val docRef = firestore.collection("movie_lists").document(listId)
            val doc = docRef.get().await()
            val list = doc.toObject(MovieList::class.java)

            if (list != null) {
                val updatedMovieIds = list.movieIds.toMutableList()
                if (!updatedMovieIds.contains(movieId)) {
                    updatedMovieIds.add(movieId)
                    val updatedList = list.copy(
                        movieIds = updatedMovieIds,
                        updatedAt = System.currentTimeMillis()
                    )
                    docRef.set(updatedList).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU35-EP18: Quitar película de lista
    suspend fun removeMovieFromList(listId: String, movieId: Int): Result<Unit> {
        return try {
            val docRef = firestore.collection("movie_lists").document(listId)
            val doc = docRef.get().await()
            val list = doc.toObject(MovieList::class.java)

            if (list != null) {
                val updatedMovieIds = list.movieIds.toMutableList()
                updatedMovieIds.remove(movieId)
                val updatedList = list.copy(
                    movieIds = updatedMovieIds,
                    updatedAt = System.currentTimeMillis()
                )
                docRef.set(updatedList).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener listas del usuario
    suspend fun getUserLists(userId: String): Result<List<MovieList>> {
        return try {
            val snapshot = firestore.collection("movie_lists")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val lists = snapshot.documents.mapNotNull { it.toObject(MovieList::class.java) }
                .sortedByDescending { it.updatedAt }
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU38-EP20: Obtener listas públicas
    suspend fun getPublicLists(): Result<List<MovieList>> {
        return try {
            val snapshot = firestore.collection("movie_lists")
                .whereEqualTo("isPublic", true)
                .limit(50)
                .get()
                .await()

            val lists = snapshot.documents.mapNotNull { it.toObject(MovieList::class.java) }
                .sortedByDescending { it.updatedAt }
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU37-EP19: Añadir a lista de pendientes
    suspend fun addToWatchlist(userId: String, movieId: Int): Result<String> {
        return try {
            // Verificar si ya existe
            val existing = firestore.collection("watchlist")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            if (existing.isEmpty) {
                val docRef = firestore.collection("watchlist").document()
                val item = WatchlistItem(
                    id = docRef.id,
                    userId = userId,
                    movieId = movieId
                )
                docRef.set(item).await()
                Result.success(docRef.id)
            } else {
                Result.success(existing.documents[0].id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Quitar de lista de pendientes
    suspend fun removeFromWatchlist(userId: String, movieId: Int): Result<Unit> {
        return try {
            val snapshot = firestore.collection("watchlist")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener lista de pendientes
    suspend fun getWatchlist(userId: String): Result<List<Int>> {
        return try {
            val snapshot = firestore.collection("watchlist")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val movieIds = snapshot.documents.mapNotNull { it.toObject(WatchlistItem::class.java) }
                .sortedByDescending { it.addedAt }
                .map { it.movieId }
            Result.success(movieIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si está en watchlist
    suspend fun isInWatchlist(userId: String, movieId: Int): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("watchlist")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU45-EP13: Marcar como favorita
    suspend fun addToFavorites(userId: String, movieId: Int): Result<String> {
        return try {
            val existing = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            if (existing.isEmpty) {
                val docRef = firestore.collection("favorites").document()
                val item = FavoriteMovie(
                    id = docRef.id,
                    userId = userId,
                    movieId = movieId
                )
                docRef.set(item).await()
                Result.success(docRef.id)
            } else {
                Result.success(existing.documents[0].id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Quitar de favoritos
    suspend fun removeFromFavorites(userId: String, movieId: Int): Result<Unit> {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener favoritos
    suspend fun getFavorites(userId: String): Result<List<Int>> {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val movieIds = snapshot.documents.mapNotNull { it.toObject(FavoriteMovie::class.java) }
                .sortedByDescending { it.addedAt }
                .map { it.movieId }
            Result.success(movieIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si está en favoritos
    suspend fun isInFavorites(userId: String, movieId: Int): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU41-EP21: Obtener próximos estrenos
    suspend fun getUpcomingMovies(page: Int = 1): Result<MovieResponse> {
        return try {
            val response = api.getUpcomingMovies(page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar próximos estrenos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
