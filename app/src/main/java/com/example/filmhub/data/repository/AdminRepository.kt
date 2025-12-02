package com.example.filmhub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.filmhub.data.model.*
import com.example.filmhub.data.remote.RetrofitClient
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val api = RetrofitClient.tmdbApi

    // HU49-EP24: Obtener todos los usuarios
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU50-EP24: Asignar rol de admin a usuario
    suspend fun setUserRole(userId: String, isAdmin: Boolean): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("isAdmin", isAdmin)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU51-EP24: Desactivar cuenta de usuario
    suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "deactivatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reactivar usuario
    suspend fun reactivateUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("isActive", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU52-EP25: Obtener reseñas reportadas
    suspend fun getReportedReviews(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("reportedItemType", "review")
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val reports = snapshot.documents.mapNotNull {
                it.toObject(Report::class.java)
            }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU52-EP25: Aprobar reporte
    suspend fun approveReport(reportId: String, adminId: String, resolution: String): Result<Unit> {
        return try {
            firestore.collection("reports")
                .document(reportId)
                .update(
                    mapOf(
                        "status" to "resolved",
                        "resolvedAt" to System.currentTimeMillis(),
                        "resolvedBy" to adminId,
                        "resolution" to resolution
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU52-EP25: Rechazar reporte
    suspend fun rejectReport(reportId: String, adminId: String, reason: String): Result<Unit> {
        return try {
            firestore.collection("reports")
                .document(reportId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "resolvedAt" to System.currentTimeMillis(),
                        "resolvedBy" to adminId,
                        "resolution" to reason
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU53-EP25: Eliminar reseña que incumple normas
    suspend fun deleteReviewByAdmin(reviewId: String, movieId: Int, reason: String): Result<Unit> {
        return try {
            // Eliminar reseña
            firestore.collection("reviews")
                .document(reviewId)
                .delete()
                .await()

            // Registrar la acción
            val action = mapOf(
                "action" to "delete_review",
                "reviewId" to reviewId,
                "movieId" to movieId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("admin_actions")
                .add(action)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear reporte
    suspend fun createReport(report: Report): Result<String> {
        return try {
            val docRef = firestore.collection("reports").document()
            val reportWithId = report.copy(id = docRef.id)
            docRef.set(reportWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU42-EP22: Obtener trailers de película
    suspend fun getMovieTrailers(movieId: Int): Result<List<VideoTrailer>> {
        return try {
            val response = api.getMovieVideos(movieId)
            if (response.isSuccessful && response.body() != null) {
                val trailers = response.body()!!.results.filter {
                    it.type == "Trailer" && it.site == "YouTube"
                }
                Result.success(trailers)
            } else {
                Result.failure(Exception("Error al cargar trailers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU54-EP30: Obtener FAQs
    suspend fun getFAQs(): Result<List<FAQ>> {
        return try {
            val snapshot = firestore.collection("faqs")
                .whereEqualTo("isActive", true)
                .orderBy("order")
                .get()
                .await()

            val faqs = snapshot.documents.mapNotNull {
                it.toObject(FAQ::class.java)
            }
            Result.success(faqs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU55-EP30: Crear ticket de soporte
    suspend fun createSupportTicket(ticket: SupportTicket): Result<String> {
        return try {
            val docRef = firestore.collection("support_tickets").document()
            val ticketWithId = ticket.copy(id = docRef.id)
            docRef.set(ticketWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener tickets del usuario
    suspend fun getUserTickets(userId: String): Result<List<SupportTicket>> {
        return try {
            val snapshot = firestore.collection("support_tickets")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tickets = snapshot.documents.mapNotNull {
                it.toObject(SupportTicket::class.java)
            }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin: Obtener todos los tickets
    suspend fun getAllTickets(): Result<List<SupportTicket>> {
        return try {
            val snapshot = firestore.collection("support_tickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tickets = snapshot.documents.mapNotNull {
                it.toObject(SupportTicket::class.java)
            }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}