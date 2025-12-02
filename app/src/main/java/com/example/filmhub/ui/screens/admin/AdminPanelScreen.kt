package com.example.filmhub.ui.screens.admin

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
import com.example.filmhub.data.model.User
import com.example.filmhub.viewmodel.AdminViewModel
import com.example.filmhub.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val adminState by viewModel.adminState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Usuarios", "Reportes", "Tickets")

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
        viewModel.loadReports()
        viewModel.loadAllTickets()
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
                        Text("Panel de Administración")
                    }
                },
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
            // Estadísticas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Usuarios", adminState.users.size.toString())
                    StatCard("Reportes", adminState.reports.size.toString())
                    StatCard("Tickets", adminState.tickets.filter { it.status == "open" }.size.toString())
                }
            }

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
                0 -> UsersContent(
                    users = adminState.users,
                    isLoading = adminState.isLoading,
                    currentUserId = currentUser?.uid ?: "",
                    onToggleRole = { userId, isAdmin ->
                        viewModel.toggleUserRole(userId, isAdmin)
                    },
                    onDeactivateUser = { userId ->
                        viewModel.deactivateUser(userId)
                    },
                    onReactivateUser = { userId ->
                        viewModel.reactivateUser(userId)
                    }
                )
                1 -> ReportsContent(
                    reports = adminState.reports,
                    isLoading = adminState.isLoading,
                    onApprove = { reportId ->
                        currentUser?.uid?.let { adminId ->
                            viewModel.approveReport(reportId, adminId)
                        }
                    },
                    onReject = { reportId ->
                        currentUser?.uid?.let { adminId ->
                            viewModel.rejectReport(reportId, adminId)
                        }
                    }
                )
                2 -> TicketsContent(
                    tickets = adminState.tickets,
                    isLoading = adminState.isLoading
                )
            }
        }
    }

    // Mensajes
    if (adminState.successMessage != null) {
        LaunchedEffect(adminState.successMessage) {
            snackbarHostState.showSnackbar(adminState.successMessage!!)
            viewModel.resetMessages()
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UsersContent(
    users: List<User>,
    isLoading: Boolean,
    currentUserId: String,
    onToggleRole: (String, Boolean) -> Unit,
    onDeactivateUser: (String) -> Unit,
    onReactivateUser: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (users.isEmpty()) {
            Text(
                text = "No hay usuarios",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        isCurrentUser = user.uid == currentUserId,
                        onToggleRole = { onToggleRole(user.uid, user.isAdmin) },
                        onDeactivate = { onDeactivateUser(user.uid) },
                        onReactivate = { onReactivateUser(user.uid) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    isCurrentUser: Boolean,
    onToggleRole: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (user.isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (user.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName.ifEmpty { "Usuario" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "Tú",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user.isAdmin) {
                        Text(
                            text = "Administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (!isCurrentUser) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, "Opciones")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (user.isAdmin) "Quitar admin" else "Hacer admin") },
                        onClick = {
                            showMenu = false
                            onToggleRole()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.AdminPanelSettings, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Desactivar cuenta") },
                        onClick = {
                            showMenu = false
                            onDeactivate()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Block, null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportsContent(
    reports: List<com.example.filmhub.data.model.Report>,
    isLoading: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (reports.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay reportes pendientes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportCard(
                        report = report,
                        onApprove = { onApprove(report.id) },
                        onReject = { onReject(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: com.example.filmhub.data.model.Report,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Report,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reporte de ${report.reportedItemType}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Motivo: ${report.reason}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (report.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Rechazar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Aprobar y eliminar")
                }
            }
        }
    }
}

@Composable
fun TicketsContent(
    tickets: List<com.example.filmhub.data.model.SupportTicket>,
    isLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (tickets.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ContactSupport,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay tickets",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tickets) { ticket ->
                    TicketCard(ticket = ticket)
                }
            }
        }
    }
}

@Composable
fun TicketCard(
    ticket: com.example.filmhub.data.model.SupportTicket
) {
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
                Text(
                    text = ticket.subject,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (ticket.status) {
                        "open" -> MaterialTheme.colorScheme.errorContainer
                        "in_progress" -> MaterialTheme.colorScheme.primaryContainer
                        "resolved" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (ticket.status) {
                            "open" -> "Abierto"
                            "in_progress" -> "En proceso"
                            "resolved" -> "Resuelto"
                            else -> "Cerrado"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usuario: ${ticket.userName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Categoría: ${ticket.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}
