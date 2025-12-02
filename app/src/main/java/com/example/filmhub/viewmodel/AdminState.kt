package com.example.filmhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmhub.data.model.*
import com.example.filmhub.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val reports: List<Report> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val trailers: List<VideoTrailer> = emptyList(),
    val faqs: List<FAQ> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    // HU49-EP24: Cargar todos los usuarios
    fun loadAllUsers() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getAllUsers()
            result.onSuccess { users ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    users = users
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU50-EP24: Cambiar rol de usuario
    fun toggleUserRole(userId: String, currentIsAdmin: Boolean) {
        viewModelScope.launch {
            val result = repository.setUserRole(userId, !currentIsAdmin)
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Rol actualizado"
                )
                loadAllUsers()
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU51-EP24: Desactivar usuario
    fun deactivateUser(userId: String) {
        viewModelScope.launch {
            val result = repository.deactivateUser(userId)
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Usuario desactivado"
                )
                loadAllUsers()
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Reactivar usuario
    fun reactivateUser(userId: String) {
        viewModelScope.launch {
            val result = repository.reactivateUser(userId)
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Usuario reactivado"
                )
                loadAllUsers()
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU52-EP25: Cargar reportes
    fun loadReports() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getReportedReviews()
            result.onSuccess { reports ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    reports = reports
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU52-EP25: Aprobar reporte
    fun approveReport(reportId: String, adminId: String) {
        viewModelScope.launch {
            val result = repository.approveReport(reportId, adminId, "Contenido inapropiado eliminado")
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Reporte aprobado"
                )
                loadReports()
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU52-EP25: Rechazar reporte
    fun rejectReport(reportId: String, adminId: String) {
        viewModelScope.launch {
            val result = repository.rejectReport(reportId, adminId, "No viola las normas")
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Reporte rechazado"
                )
                loadReports()
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU53-EP25: Eliminar reseña
    fun deleteReview(reviewId: String, movieId: Int) {
        viewModelScope.launch {
            val result = repository.deleteReviewByAdmin(reviewId, movieId, "Violación de normas")
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Reseña eliminada"
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU42-EP22: Cargar trailers
    fun loadMovieTrailers(movieId: Int) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getMovieTrailers(movieId)
            result.onSuccess { trailers ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    trailers = trailers
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU54-EP30: Cargar FAQs
    fun loadFAQs() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getFAQs()
            result.onSuccess { faqs ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    faqs = faqs
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // HU55-EP30: Crear ticket de soporte
    fun createSupportTicket(
        userId: String,
        userEmail: String,
        userName: String,
        subject: String,
        description: String,
        category: String
    ) {
        viewModelScope.launch {
            val ticket = SupportTicket(
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                subject = subject,
                description = description,
                category = category
            )

            val result = repository.createSupportTicket(ticket)
            result.onSuccess {
                _adminState.value = _adminState.value.copy(
                    successMessage = "Ticket creado. Te responderemos pronto."
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    errorMessage = exception.message
                )
            }
        }
    }

    // Cargar tickets del usuario
    fun loadUserTickets(userId: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getUserTickets(userId)
            result.onSuccess { tickets ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    tickets = tickets
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    // Admin: Cargar todos los tickets
    fun loadAllTickets() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true)

            val result = repository.getAllTickets()
            result.onSuccess { tickets ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    tickets = tickets
                )
            }.onFailure { exception ->
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }

    fun resetMessages() {
        _adminState.value = _adminState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}