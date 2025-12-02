package com.example.filmhub.ui.screens.movies

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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.filmhub.R
import com.example.filmhub.data.model.Movie
import com.example.filmhub.data.remote.ApiConstants
import com.example.filmhub.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToMovieDetail: (Movie) -> Unit,
    onNavigateToMyLists: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToUpcoming: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: MovieViewModel = viewModel()
) {
    val movieListState by viewModel.movieListState.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                // HU23-EP10: Barra de búsqueda
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { viewModel.searchMovies(it) },
                    onClose = {
                        showSearchBar = false
                        searchQuery = ""
                        viewModel.clearFilters()
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.filmhup_logo),
                                contentDescription = "FilmHub",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FilmHub")
                        }
                    },
                    actions = {
                        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            TextButton(onClick = onNavigateToLogin) {
                                Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        // HU23-EP10: Botón de búsqueda
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Filled.Search, "Buscar")
                        }
                        // HU24-EP11 y HU25-EP12: Botón de filtros
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Filled.FilterList, "Filtros")
                        }
                        if (currentUser != null) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Filled.MoreVert, "Menú")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mis Listas") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToMyLists()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.PlaylistPlay, "Mis Listas")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Favoritos y Pendientes") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToWatchlist()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Favorite, "Favoritos")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Próximos Estrenos") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToUpcoming()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.CalendarMonth, "Estrenos")
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Mi Perfil") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToProfile()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Person, "Perfil")
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (movieListState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (movieListState.errorMessage != null) {
                ErrorMessage(
                    message = movieListState.errorMessage!!,
                    onRetry = { viewModel.loadPopularMovies() }
                )
            } else if (movieListState.movies.isEmpty()) {
                EmptyState()
            } else {
                // HU13-EP05 y HU14-EP05: Lista de películas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mostrar filtros activos
                    if (movieListState.selectedYear != null || movieListState.selectedGenres.isNotEmpty()) {
                        item {
                            ActiveFiltersChip(
                                year = movieListState.selectedYear,
                                genreCount = movieListState.selectedGenres.size,
                                onClear = { viewModel.clearFilters() }
                            )
                        }
                    }

                    items(movieListState.movies) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onNavigateToMovieDetail(movie) }
                        )
                    }
                }
            }
        }
    }

    // HU24-EP11 y HU25-EP12: Diálogo de filtros
    if (showFilterDialog) {
        FilterDialog(
            genres = movieListState.genres,
            selectedYear = movieListState.selectedYear,
            selectedGenres = movieListState.selectedGenres,
            onDismiss = { showFilterDialog = false },
            onApply = { year, genres ->
                viewModel.filterMovies(year, genres)
                showFilterDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar películas...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Filled.ArrowBack,
                    "Cerrar búsqueda",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = { onSearch(query) }) {
                Icon(
                    Icons.Filled.Search,
                    "Buscar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Poster de la película
            AsyncImage(
                model = ApiConstants.getPosterUrl(movie.posterPath),
                contentDescription = movie.title,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = movie.releaseDate.take(4),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Calificación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", movie.voteAverage),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${movie.voteCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersChip(
    year: Int?,
    genreCount: Int,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    Icons.Filled.FilterList,
                    "Filtros",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Filtros activos",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        buildString {
                            if (year != null) append("Año: $year")
                            if (genreCount > 0) {
                                if (year != null) append(" • ")
                                append("$genreCount género${if (genreCount > 1) "s" else ""}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            TextButton(onClick = onClear) {
                Text("Limpiar")
            }
        }
    }
}

@Composable
fun FilterDialog(
    genres: List<com.example.filmhub.data.model.Genre>,
    selectedYear: Int?,
    selectedGenres: List<Int>,
    onDismiss: () -> Unit,
    onApply: (Int?, List<Int>) -> Unit
) {
    var year by remember { mutableStateOf(selectedYear?.toString() ?: "") }
    var tempSelectedGenres by remember { mutableStateOf(selectedGenres.toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar películas") },
        text = {
            LazyColumn {
                item {
                    Text(
                        "Año de estreno",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4) year = it },
                        placeholder = { Text("Ej: 2024") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Géneros",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(genres) { genre ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (tempSelectedGenres.contains(genre.id)) {
                                    tempSelectedGenres.remove(genre.id)
                                } else {
                                    tempSelectedGenres.add(genre.id)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempSelectedGenres.contains(genre.id),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    tempSelectedGenres.add(genre.id)
                                } else {
                                    tempSelectedGenres.remove(genre.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(genre.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val yearInt = year.toIntOrNull()
                    onApply(yearInt, tempSelectedGenres)
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MovieFilter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No se encontraron películas",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Intenta con otra búsqueda o filtro",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
