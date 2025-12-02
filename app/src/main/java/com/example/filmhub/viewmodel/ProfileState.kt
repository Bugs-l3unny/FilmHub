package com.example.filmhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmhub.data.model.User
import com.example.filmhub.data.repository.AuthRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: User? = null
)

class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    private var userListener: ListenerRegistration? = null

    init {
        loadUserData()
        startUserListener()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)

            val currentUser = repository.currentUser
            if (currentUser != null) {
                val result = repository.getUserData(currentUser.uid)
                result.onSuccess { user ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        user = user
                    )
                }.onFailure {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar datos del usuario"
                    )
                }
            }
        }
    }

    // HU10-EP04: Cambiar nombre de perfil
    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            if (newName.isBlank()) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = "El nombre no puede estar vacío"
                )
                return@launch
            }

            val result = repository.updateDisplayName(newName)
            result.onSuccess {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Nombre actualizado correctamente",
                    user = _profileState.value.user?.copy(displayName = newName)
                )
            }.onFailure { exception ->
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Error al actualizar nombre"
                )
            }
        }
    }

    // HU12-EP04: Cambiar contraseña desde el perfil
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            // Validaciones
            when {
                currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "Todos los campos son obligatorios"
                    )
                    return@launch
                }
                newPassword.length < 6 -> {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "La nueva contraseña debe tener al menos 6 caracteres"
                    )
                    return@launch
                }
                newPassword != confirmPassword -> {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "Las contraseñas no coinciden"
                    )
                    return@launch
                }
            }

            // Primero re-autenticar con la contraseña actual
            val user = repository.currentUser
            if (user?.email != null) {
                val signInResult = repository.signInWithEmail(user.email!!, currentPassword)

                signInResult.onSuccess {
                    // Si la autenticación es exitosa, cambiar la contraseña
                    val updateResult = repository.updatePassword(newPassword)
                    updateResult.onSuccess {
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = "Contraseña actualizada correctamente"
                        )
                    }.onFailure { exception ->
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al actualizar contraseña"
                        )
                    }
                }.onFailure {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "La contraseña actual es incorrecta"
                    )
                }
            }
        }
    }

    // HU11-EP04: Actualizar foto de perfil
    fun updateProfilePhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.updatePhoto(uri)
            result.onSuccess { url ->
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Foto de perfil actualizada",
                    user = _profileState.value.user?.copy(photoUrl = url)
                )
            }.onFailure { e ->
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al actualizar foto"
                )
            }
        }
    }

    fun reloadUser() {
        loadUserData()
    }

    private fun startUserListener() {
        val current = repository.currentUser
        if (current != null) {
            userListener?.remove()
            userListener = repository.observeUser(current.uid) { user ->
                if (user != null) {
                    _profileState.value = _profileState.value.copy(user = user)
                }
            }
        }
    }

    override fun onCleared() {
        userListener?.remove()
        super.onCleared()
    }

    fun logout() {
        repository.signOut()
    }

    fun resetMessages() {
        _profileState.value = _profileState.value.copy(
            errorMessage = null,
            successMessage = null,
            isSuccess = false
        )
    }
}
