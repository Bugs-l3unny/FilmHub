package com.example.filmhub.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.filmhub.R
import kotlinx.coroutines.launch
import com.example.filmhub.ui.components.*
import com.example.filmhub.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isAdmin = profileState.user?.isAdmin ?: false
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeManager = remember { com.example.filmhub.data.ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.reloadUser()
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.filmhup_logo),
                            contentDescription = "FilmHub",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mi Perfil")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
                        }
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            val photoUrl = profileState.user?.photoUrl
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del usuario
            Text(
                text = profileState.user?.displayName ?: "Usuario",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email del usuario
            Text(
                text = profileState.user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Opciones del perfil
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Panel de administrador (solo si es admin)
                if (isAdmin) {
                    ProfileOptionCard(
                        icon = Icons.Filled.AdminPanelSettings,
                        title = "Panel de Administración",
                        subtitle = "Gestionar usuarios y contenido",
                        onClick = onNavigateToAdmin
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // HU10-EP04: Cambiar nombre
                ProfileOptionCard(
                    icon = Icons.Filled.Edit,
                    title = "Editar nombre",
                    subtitle = "Cambiar tu nombre de perfil",
                    onClick = { showEditNameDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        viewModel.updateProfilePhoto(uri)
                    }
                }
                ProfileOptionCard(
                    icon = Icons.Filled.PhotoCamera,
                    title = "Cambiar foto",
                    subtitle = "Actualizar tu foto de perfil",
                    onClick = { imagePicker.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // HU12-EP04: Cambiar contraseña
                ProfileOptionCard(
                    icon = Icons.Filled.Lock,
                    title = "Cambiar contraseña",
                    subtitle = "Actualizar tu contraseña",
                    onClick = { showChangePasswordDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tema claro/oscuro
                Card(
                    onClick = { scope.launch { themeManager.toggleTheme(!isDarkTheme) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = "Tema oscuro",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tema oscuro",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isDarkTheme) "Activado" else "Desactivado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked -> scope.launch { themeManager.toggleTheme(checked) } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // HU54-EP30: Centro de ayuda
                ProfileOptionCard(
                    icon = Icons.Filled.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Preguntas frecuentes y soporte",
                    onClick = onNavigateToHelp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Estado de cuenta
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Estado de cuenta",
                            tint = if (profileState.user?.isActive == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado de cuenta",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (profileState.user?.isActive == true) "Activo" else "Desactivado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                // Cerrar sesión
                ProfileOptionCard(
                    icon = Icons.Filled.ExitToApp,
                    title = "Cerrar sesión",
                    subtitle = "Salir de tu cuenta",
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }

    // Diálogo para editar nombre (HU10-EP04)
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = profileState.user?.displayName ?: "",
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateDisplayName(newName)
                showEditNameDialog = false
            },
            isLoading = profileState.isLoading
        )
    }

    // Diálogo para cambiar contraseña (HU12-EP04)
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = {
                showChangePasswordDialog = false
                viewModel.resetMessages()
            },
            onConfirm = { current, new, confirm ->
                viewModel.updatePassword(current, new, confirm)
            },
            isLoading = profileState.isLoading
        )
    }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar mensajes de éxito o error
    if (profileState.successMessage != null) {
        LaunchedEffect(profileState.successMessage) {
            snackbarHostState.showSnackbar(profileState.successMessage!!)
            viewModel.resetMessages()
        }
    }

    if (profileState.errorMessage != null && !showChangePasswordDialog) {
        LaunchedEffect(profileState.errorMessage) {
            snackbarHostState.showSnackbar(profileState.errorMessage!!)
            viewModel.resetMessages()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nombre") },
        text = {
            CustomTextField(
                value = newName,
                onValueChange = { newName = it },
                label = "Nuevo nombre",
                enabled = !isLoading
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && !isLoading
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                CustomPasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Contraseña actual",
                    imeAction = ImeAction.Next,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nueva contraseña",
                    imeAction = ImeAction.Next,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar nueva contraseña",
                    imeAction = ImeAction.Done,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(currentPassword, newPassword, confirmPassword)
                },
                enabled = currentPassword.isNotBlank() &&
                        newPassword.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Cambiar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
