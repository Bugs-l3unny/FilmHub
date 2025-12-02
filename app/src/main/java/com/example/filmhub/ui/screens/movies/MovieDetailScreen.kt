package com.example.filmhub.ui.screens.movies

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.filmhub.data.model.MovieList
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.example.filmhub.data.model.Movie
import com.example.filmhub.data.model.Review
import com.example.filmhub.data.remote.ApiConstants
import com.example.filmhub.viewmodel.MovieViewModel

import androidx.compose.ui.platform.LocalContext
import com.example.filmhub.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onNavigateBack: () -> Unit,
    viewModel: MovieViewModel = viewModel()
) {
    val movieDetailState by viewModel.movieDetailState.collectAsState()
    val listViewModel: com.example.filmhub.viewmodel.ListViewModel = viewModel()
    val listState by listViewModel.listState.collectAsState()
    val adminViewModel: com.example.filmhub.viewmodel.AdminViewModel = viewModel()
    val adminState by adminViewModel.adminState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var showRatingDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showEditReviewDialog by remember { mutableStateOf(false) }
    var reviewToEdit by remember { mutableStateOf<Review?>(null) }
    var showAddToListDialog by remember { mutableStateOf(false) }
    var showTrailerDialog by remember { mutableStateOf(false) }
    var selectedTrailer by remember { mutableStateOf<com.example.filmhub.data.model.VideoTrailer?>(null) }

    LaunchedEffect(movie.id) {
        viewModel.loadMovieDetails(movie, currentUser?.uid)
        viewModel.startReviewsListener(movie.id)
        currentUser?.uid?.let { userId ->
            listViewModel.checkMovieStatus(userId, movie.id)
            listViewModel.startRealtime(userId)
        }
        // HU42-EP22: Cargar trailers
        adminViewModel.loadMovieTrailers(movie.id)
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun requireAuth(action: () -> Unit) {
        if (currentUser == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Inicia sesión para continuar")
            }
        } else {
            action()
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text((movieDetailState.movie?.title ?: movie.title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón de compartir
                    IconButton(
                        onClick = {
                            ShareUtils.shareMovie(context, (movieDetailState.movie?.title ?: movie.title), movie.id)
                        }
                    ) {
                        Icon(Icons.Filled.Share, "Compartir")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Backdrop o poster grande
            AsyncImage(
                model = (
                    movieDetailState.movie?.let { m ->
                        ApiConstants.getBackdropUrl(m.backdropPath) ?: ApiConstants.getPosterUrl(m.posterPath)
                    }
                    ?: (ApiConstants.getBackdropUrl(movie.backdropPath) ?: ApiConstants.getPosterUrl(movie.posterPath))
                ),
                contentDescription = (movieDetailState.movie?.title ?: movie.title),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

                Column(modifier = Modifier.padding(16.dp)) {
                // Título y año
                Text(
                    text = (movieDetailState.movie?.title ?: movie.title),
                    style = MaterialTheme.typography.headlineMedium
                )

                val rel = movieDetailState.movie?.releaseDate ?: movie.releaseDate
                if (rel.isNotEmpty()) {
                    Text(
                        text = rel.take(4),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (movieDetailState.trailers.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.playTrailer(context, movieDetailState.trailers.firstOrNull()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, "Ver trailer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver trailer")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

            // Botones de acción rápida
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // HU37-EP19: Botón de pendientes
                OutlinedButton(
                    onClick = {
                        requireAuth {
                            val userId = currentUser!!.uid
                            if (listState.isInWatchlist) {
                                listViewModel.removeFromWatchlist(userId, movie.id)
                            } else {
                                listViewModel.addToWatchlist(userId, movie.id)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                        Icon(
                            imageVector = if (listState.isInWatchlist)
                                Icons.Filled.BookmarkAdded
                            else
                                Icons.Filled.BookmarkAdd,
                            contentDescription = "Pendientes"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (listState.isInWatchlist) "En lista" else "Pendiente")
                    }

                // HU45-EP13: Botón de favoritos
                OutlinedButton(
                    onClick = {
                        requireAuth {
                            val userId = currentUser!!.uid
                            if (listState.isInFavorites) {
                                listViewModel.removeFromFavorites(userId, movie.id)
                            } else {
                                listViewModel.addToFavorites(userId, movie.id)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                        Icon(
                            imageVector = if (listState.isInFavorites)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (listState.isInFavorites)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Favorito")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // HU34-EP18: Botón para añadir a lista personalizada
                OutlinedButton(
                    onClick = {
                        requireAuth { showAddToListDialog = true }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PlaylistAdd, "Añadir a lista")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir a lista personalizada")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // HU32-EP15: Calificación promedio de TMDb
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Calificación TMDb",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.1f", (movieDetailState.movie?.voteAverage ?: movie.voteAverage)),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / 10",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${(movieDetailState.movie?.voteCount ?: movie.voteCount)} votos)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // HU32-EP15 y HU33-EP15: Estadísticas de usuarios de FilmHub
                movieDetailState.stats?.let { stats ->
                    if (stats.totalRatings > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Calificación de usuarios FilmHub",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "${String.format("%.1f", stats.averageRating)} / 5 ⭐ (${stats.totalRatings} calificaciones)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // HU29-EP14: Botón para calificar
                Button(
                    onClick = { requireAuth { showRatingDialog = true } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Star, "Calificar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (movieDetailState.userRating != null)
                            "Tu calificación: ${movieDetailState.userRating!!.rating} ⭐"
                        else
                            "Calificar película"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // HU27-EP13: Botón para escribir reseña
                OutlinedButton(
                    onClick = { requireAuth { showReviewDialog = true } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.RateReview, "Escribir reseña")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escribir reseña")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sinopsis
                Text(
                    text = "Sinopsis",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (movieDetailState.movie?.overview ?: movie.overview).ifEmpty { "No hay sinopsis disponible" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de reseñas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reseñas (${movieDetailState.reviews.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (movieDetailState.reviews.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.RateReview,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aún no hay reseñas",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "¡Sé el primero en escribir una!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    movieDetailState.reviews.forEach { review ->
                        ReviewCard(
                            review = review,
                            isOwnReview = review.userId == currentUser?.uid,
                            onEdit = {
                                reviewToEdit = review
                                showEditReviewDialog = true
                            },
                            onDelete = {
                                viewModel.deleteReview(review.id, movie.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // HU29-EP14 y HU30-EP14: Diálogo para calificar
    if (showRatingDialog) {
        RatingDialog(
            currentRating = movieDetailState.userRating?.rating ?: 0f,
            onDismiss = { showRatingDialog = false },
            onRate = { rating ->
                currentUser?.uid?.let { userId ->
                    viewModel.rateMovie(movie.id, userId, rating)
                }
                showRatingDialog = false
            }
        )
    }

    // HU27-EP13: Diálogo para escribir reseña
    if (showReviewDialog) {
        WriteReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, reviewText ->
                currentUser?.let { user ->
                    viewModel.createReview(
                        movieId = movie.id,
                        userId = user.uid,
                        userName = user.displayName ?: "Usuario",
                        userEmail = user.email ?: "",
                        rating = rating,
                        reviewText = reviewText
                    )
                }
                showReviewDialog = false
            }
        )
    }

    // HU28-EP13: Diálogo para editar reseña
    if (showEditReviewDialog && reviewToEdit != null) {
        EditReviewDialog(
            review = reviewToEdit!!,
            onDismiss = {
                showEditReviewDialog = false
                reviewToEdit = null
            },
            onSubmit = { rating, reviewText ->
                val updated = reviewToEdit!!.copy(
                    rating = rating,
                    reviewText = reviewText
                )
                viewModel.updateReview(updated)
                showEditReviewDialog = false
                reviewToEdit = null
            }
        )
    }

    // HU31-EP14: Mostrar mensaje de confirmación
    if (movieDetailState.successMessage != null) {
        LaunchedEffect(movieDetailState.successMessage) {
            snackbarHostState.showSnackbar(movieDetailState.successMessage!!)
            viewModel.resetMessages()
        }
    }

    // Mensajes de listas
    if (listState.successMessage != null) {
        LaunchedEffect(listState.successMessage) {
            snackbarHostState.showSnackbar(listState.successMessage!!)
            listViewModel.resetMessages()
        }
    }

    // HU35-EP18: Diálogo para añadir a lista
    if (showAddToListDialog) {
        AddToListDialog(
            lists = listState.userLists,
            onDismiss = { showAddToListDialog = false },
            onAddToList = { listId ->
                currentUser?.uid?.let { userId ->
                    listViewModel.addMovieToList(listId, movie.id, userId)
                }
                showAddToListDialog = false
            },
            onCreateNewList = {
                showAddToListDialog = false
                // Navegar a crear lista nueva
            }
        )
    }
}

@Composable
fun ReviewCard(
    review: Review,
    isOwnReview: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.rating.toInt())
                                    Icons.Filled.Star
                                else
                                    Icons.Filled.StarOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${review.rating}/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // HU28-EP13: Opciones de editar/eliminar para propias reseñas
                if (isOwnReview) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(review.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar reseña") },
            text = { Text("¿Estás seguro de que quieres eliminar esta reseña?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
fun RatingDialog(
    currentRating: Float,
    onDismiss: () -> Unit,
    onRate: (Float) -> Unit
) {
    var rating by remember { mutableStateOf(currentRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Califica esta película") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${rating.toInt()} / 5",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = (index + 1).toFloat() }
                        ) {
                            Icon(
                                imageVector = if (index < rating.toInt())
                                    Icons.Filled.Star
                                else
                                    Icons.Filled.StarOutline,
                                contentDescription = "${index + 1} estrellas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRate(rating) },
                enabled = rating > 0
            ) {
                Text("Calificar")
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
fun AddToListDialog(
    lists: List<MovieList>,
    onDismiss: () -> Unit,
    onAddToList: (String) -> Unit,
    onCreateNewList: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a lista") },
        text = {
            if (lists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes listas aún",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Crea tu primera lista",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(lists) { list ->
                        ListItem(
                            headlineContent = { Text(list.title) },
                            supportingContent = {
                                Text("${list.movieIds.size} películas")
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (list.isPublic)
                                        Icons.Filled.Public
                                    else
                                        Icons.Filled.Lock,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                onAddToList(list.id)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNewList) {
                Text("Nueva lista")
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
fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escribir reseña") },
        text = {
            Column {
                Text("Calificación")
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = (index + 1).toFloat() }
                        ) {
                            Icon(
                                imageVector = if (index < rating.toInt())
                                    Icons.Filled.Star
                                else
                                    Icons.Filled.StarOutline,
                                contentDescription = "${index + 1} estrellas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tu reseña")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Escribe tu opinión sobre la película...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, reviewText) },
                enabled = rating > 0 && reviewText.isNotBlank()
            ) {
                Text("Publicar")
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
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var reviewText by remember { mutableStateOf(review.reviewText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar reseña") },
        text = {
            Column {
                Text("Calificación")
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = (index + 1).toFloat() }
                        ) {
                            Icon(
                                imageVector = if (index < rating.toInt())
                                    Icons.Filled.Star
                                else
                                    Icons.Filled.StarOutline,
                                contentDescription = "${index + 1} estrellas",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tu reseña")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Escribe tu opinión sobre la película...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, reviewText) },
                enabled = rating > 0 && reviewText.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
