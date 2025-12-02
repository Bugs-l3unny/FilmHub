package com.example.filmhub.data.model

// Reporte de reseña/contenido
data class Report(
    val id: String = "",
    val reportedItemId: String = "",
    val reportedItemType: String = "", // "review", "user", etc.
    val reportedUserId: String = "",
    val reporterUserId: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = "pending", // pending, reviewed, resolved, rejected
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolution: String? = null
)

// HU54-EP30: Ticket de soporte
data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val subject: String = "",
    val description: String = "",
    val category: String = "", // technical, account, content, other
    val status: String = "open", // open, in_progress, resolved, closed
    val priority: String = "normal", // low, normal, high, urgent
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val responses: List<TicketResponse> = emptyList()
)

data class TicketResponse(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val isAdmin: Boolean = false,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// HU54-EP30: Pregunta frecuente
data class FAQ(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val category: String = "",
    val order: Int = 0,
    val isActive: Boolean = true
)

// HU42-EP22: Video de trailer
data class VideoTrailer(
    val key: String = "",
    val name: String = "",
    val site: String = "YouTube",
    val type: String = "Trailer",
    val official: Boolean = false
)

data class VideosResponse(
    val results: List<VideoTrailer> = emptyList()
)