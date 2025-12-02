package com.example.filmhub.ui.screens.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.filmhub.data.model.Movie
import com.example.filmhub.data.remote.ApiConstants
import com.example.filmhub.viewmodel.ListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovieDetail: (Movie) -> Unit,
    viewModel: ListViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "Favoritos")

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            viewModel.startRealtime(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Películas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0)
                                    Icons.Filled.BookmarkAdded
                                else
                                    Icons.Filled.Favorite,
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> WatchlistContent(
                    movies = listState.watchlistMovies,
                    isLoading = listState.isLoading,
                    onMovieClick = onNavigateToMovieDetail,
                    onRemove = { movie ->
                        currentUser?.uid?.let { userId ->
                            viewModel.removeFromWatchlist(userId, movie.id)
                        }
                    }
                )
                1 -> FavoritesContent(
                    movies = listState.favoriteMovies,
                    isLoading = listState.isLoading,
                    onMovieClick = onNavigateToMovieDetail,
                    onRemove = { movie ->
                        currentUser?.uid?.let { userId ->
                            viewModel.removeFromFavorites(userId, movie.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WatchlistContent(
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onRemove: (Movie) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (movies.isEmpty()) {
            EmptyWatchlistState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies) { movie ->
                    SavedMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onRemove = { onRemove(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesContent(
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onRemove: (Movie) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (movies.isEmpty()) {
            EmptyFavoritesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies) { movie ->
                    SavedMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        onRemove = { onRemove(movie) },
                        isFavorite = true
                    )
                }
            }
        }
    }
}

@Composable
fun SavedMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    isFavorite: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = ApiConstants.getPosterUrl(movie.posterPath),
                contentDescription = movie.title,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (movie.releaseDate.isNotEmpty()) {
                        Text(
                            text = movie.releaseDate.take(4),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", movie.voteAverage),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.BookmarkRemove,
                            contentDescription = "Eliminar",
                            tint = if (isFavorite)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar") },
            text = {
                Text(
                    if (isFavorite)
                        "¿Quitar de favoritos?"
                    else
                        "¿Quitar de pendientes?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EmptyWatchlistState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.BookmarkAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu lista de pendientes está vacía",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Añade películas que quieras ver más tarde",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes favoritos aún",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Marca tus películas favoritas para acceder a ellas fácilmente",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
