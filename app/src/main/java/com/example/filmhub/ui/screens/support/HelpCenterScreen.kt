package com.example.filmhub.ui.screens.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.filmhub.data.model.FAQ
import com.example.filmhub.viewmodel.AdminViewModel
import com.example.filmhub.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: com.example.filmhub.viewmodel.AdminViewModel = viewModel()
) {
    val adminState by viewModel.adminState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Preguntas Frecuentes", "Contactar Soporte")

    // Cargar FAQs por defecto
    val defaultFAQs = remember {
        listOf(
            FAQ(
                id = "1",
                question = "¿Cómo creo una cuenta?",
                answer = "Toca el botón 'Registrarse' en la pantalla de inicio. Ingresa tu correo electrónico y crea una contraseña. Recibirás un correo de verificación para activar tu cuenta.",
                category = "Cuenta"
            ),
            FAQ(
                id = "2",
                question = "¿Cómo califico una película?",
                answer = "Abre los detalles de cualquier película y toca el botón 'Calificar película'. Selecciona de 1 a 5 estrellas según tu opinión.",
                category = "Películas"
            ),
            FAQ(
                id = "3",
                question = "¿Cómo creo una lista personalizada?",
                answer = "Ve al menú principal → 'Mis Listas' → Toca el botón + → Ingresa un título y descripción → Elige si será pública o privada.",
                category = "Listas"
            ),
            FAQ(
                id = "4",
                question = "¿Puedo cambiar mi contraseña?",
                answer = "Sí, ve a tu perfil → 'Cambiar contraseña' → Ingresa tu contraseña actual y la nueva contraseña dos veces.",
                category = "Cuenta"
            ),
            FAQ(
                id = "5",
                question = "¿Qué significa 'Pendientes'?",
                answer = "Es una lista especial donde guardas películas que quieres ver más tarde. Puedes añadir películas tocando el botón de marcador en los detalles.",
                category = "Listas"
            ),
            FAQ(
                id = "6",
                question = "¿Cómo reporto contenido inapropiado?",
                answer = "En cualquier reseña que consideres inapropiada, toca el menú (⋮) y selecciona 'Reportar'. Describe el motivo y envía el reporte.",
                category = "Seguridad"
            ),
            FAQ(
                id = "7",
                question = "¿Puedo compartir mis listas?",
                answer = "Sí, en 'Mis Listas' toca el menú de una lista → 'Compartir' → Elige la red social o app donde quieres compartir.",
                category = "Listas"
            ),
            FAQ(
                id = "8",
                question = "¿Cómo busco películas específicas?",
                answer = "Toca el ícono de búsqueda en la pantalla principal e ingresa el título de la película. También puedes usar filtros por año y género.",
                category = "Películas"
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadFAQs()
    }

    Scaffold(
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
                        Text("Centro de Ayuda")
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
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> FAQContent(faqs = if (adminState.faqs.isEmpty()) defaultFAQs else adminState.faqs)
                1 -> ContactSupportContent(onNavigateToSupport = onNavigateToSupport)
            }
        }
    }
}

@Composable
fun FAQContent(faqs: List<FAQ>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Preguntas Frecuentes",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Encuentra respuestas a las preguntas más comunes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        items(faqs) { faq ->
            FAQCard(faq = faq)
        }
    }
}

@Composable
fun FAQCard(faq: FAQ) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.QuestionAnswer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = faq.question,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ContactSupportContent(
    onNavigateToSupport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                    Icon(
                    	imageVector = Icons.Filled.ContactSupport,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¿No encontraste lo que buscabas?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nuestro equipo de soporte está aquí para ayudarte",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        SupportOptionCard(
            icon = Icons.Filled.Email,
            title = "Enviar un mensaje",
            description = "Crea un ticket de soporte y responderemos pronto",
            onClick = onNavigateToSupport
        )

        SupportOptionCard(
            icon = Icons.Filled.BugReport,
            title = "Reportar un problema",
            description = "Informa sobre errores técnicos o bugs",
            onClick = onNavigateToSupport
        )

        SupportOptionCard(
            icon = Icons.Filled.Feedback,
            title = "Enviar feedback",
            description = "Comparte tus ideas para mejorar FilmHub",
            onClick = onNavigateToSupport
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tiempo de respuesta",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Problemas técnicos: 24-48 horas\n• Consultas generales: 2-3 días\n• Reportes: 1-2 días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SupportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
