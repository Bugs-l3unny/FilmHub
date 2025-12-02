package com.example.filmhub.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.filmhub.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val adminState by viewModel.adminState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    val categories = listOf(
        "technical" to "Problema Técnico",
        "account" to "Mi Cuenta",
        "content" to "Contenido",
        "other" to "Otro"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactar Soporte") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Describe tu problema",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Sé lo más específico posible para que podamos ayudarte mejor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Categoría
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == selectedCategory }?.second ?: "Selecciona una categoría",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedCategory = key
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Asunto
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Asunto") },
                placeholder = { Text("Resumen breve del problema") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Subject, "Asunto")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción detallada") },
                placeholder = { Text("Describe tu problema en detalle...") },
                minLines = 6,
                maxLines = 10,
                leadingIcon = {
                    Icon(Icons.Filled.Description, "Descripción")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Consejos
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Consejos para una mejor respuesta:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Incluye capturas de pantalla si es posible\n" +
                                "• Menciona el modelo de tu dispositivo\n" +
                                "• Describe los pasos para reproducir el error\n" +
                                "• Indica si el problema es reciente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón enviar
            Button(
                onClick = {
                    currentUser?.let { user ->
                        viewModel.createSupportTicket(
                            userId = user.uid,
                            userEmail = user.email ?: "",
                            userName = user.displayName ?: "Usuario",
                            subject = subject,
                            description = description,
                            category = selectedCategory
                        )
                    }
                },
                enabled = subject.isNotBlank() &&
                        description.isNotBlank() &&
                        selectedCategory.isNotBlank() &&
                        !adminState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (adminState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Send, "Enviar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Ticket")
                }
            }
        }
    }

    // Diálogo de éxito
    if (adminState.successMessage != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Ticket Enviado") },
            text = {
                Column {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(adminState.successMessage!!)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Te responderemos en tu correo: ${currentUser?.email}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetMessages()
                        onNavigateBack()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Mensaje de error
    if (adminState.errorMessage != null) {
        LaunchedEffect(adminState.errorMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetMessages()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(adminState.errorMessage!!)
        }
    }
}