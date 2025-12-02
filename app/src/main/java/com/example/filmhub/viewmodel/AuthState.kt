package com.example.filmhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmhub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // HU01-EP01 y HU02-EP01: Registro con email y contraseña
    fun register(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            // Validaciones
            when {
                email.isBlank() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "El correo no puede estar vacío"
                    )
                    return@launch
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Ingresa un correo válido"
                    )
                    return@launch
                }
                password.isBlank() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "La contraseña no puede estar vacía"
                    )
                    return@launch
                }
                password.length < 6 -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    )
                    return@launch
                }
                password != confirmPassword -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Las contraseñas no coinciden"
                    )
                    return@launch
                }
            }

            // HU03-EP01: Registro y envío de correo de verificación
            val result = repository.registerWithEmail(email, password)

            result.onSuccess {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    // HU04-EP02: Iniciar sesión
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            // Validaciones
            when {
                email.isBlank() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "El correo no puede estar vacío"
                    )
                    return@launch
                }
                password.isBlank() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "La contraseña no puede estar vacía"
                    )
                    return@launch
                }
            }

            val result = repository.signInWithEmail(email, password)

            result.onSuccess {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            }.onFailure { exception ->
                // HU05-EP02: Mensaje de error si credenciales son inválidas
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    // HU07-EP03: Recuperación de contraseña
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            if (email.isBlank()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "El correo no puede estar vacío"
                )
                return@launch
            }

            // HU08-EP03: Enviar enlace de recuperación
            val result = repository.sendPasswordResetEmail(email)

            result.onSuccess {
                // HU09-EP03: Confirmación después de enviar correo
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = getErrorMessage(exception)
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState()
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("email") == true -> "Correo inválido"
            exception.message?.contains("password") == true -> "Contraseña incorrecta"
            exception.message?.contains("network") == true -> "Error de conexión"
            exception.message?.contains("user-not-found") == true -> "Usuario no encontrado"
            exception.message?.contains("wrong-password") == true -> "Contraseña incorrecta"
            exception.message?.contains("email-already-in-use") == true -> "El correo ya está en uso"
            else -> exception.message ?: "Error desconocido"
        }
    }
}