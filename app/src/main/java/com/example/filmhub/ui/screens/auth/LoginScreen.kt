package com.example.filmhub.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filmhub.ui.components.*
import com.example.filmhub.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // HU04-EP02: Navegar al home cuando el login es exitoso
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onNavigateToHome()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FilmHub") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o título
            Text(
                text = "Bienvenido Cinéfil@",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de email
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = authState.errorMessage != null && email.isBlank(),
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            CustomPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                imeAction = ImeAction.Done,
                isError = authState.errorMessage != null && password.isBlank(),
                enabled = !authState.isLoading,
                onImeAction = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.signIn(email, password)
                    }
                }
            )

            // HU05-EP02: Mostrar mensaje de error si las credenciales son inválidas
            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enlace para recuperar contraseña
            TextButton(
                onClick = onNavigateToForgotPassword,
                enabled = !authState.isLoading
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de iniciar sesión
            CustomButton(
                text = "Iniciar sesión",
                onClick = {
                    viewModel.signIn(email, password)
                },
                isLoading = authState.isLoading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace para registro
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = !authState.isLoading
                ) {
                    Text("Regístrate")
                }
            }
        }
    }
}