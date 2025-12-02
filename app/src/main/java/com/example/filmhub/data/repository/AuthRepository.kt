package com.example.filmhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import com.example.filmhub.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // HU01-EP01: Registro con email
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // HU03-EP01: Enviar correo de verificación
                firebaseUser.sendEmailVerification().await()

                // Crear documento en Firestore
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = email.substringBefore("@"),
                    isAdmin = false
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()

                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Error al crear usuario"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU04-EP02: Iniciar sesión
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Error al iniciar sesión"))
        } catch (e: Exception) {
            // HU05-EP02: Mensaje de error si credenciales inválidas
            Result.failure(e)
        }
    }

    // HU07-EP03: Recuperación de contraseña
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU12-EP04: Cambiar contraseña
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HU10-EP04: Actualizar nombre de perfil
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            currentUser?.updateProfile(profileUpdates)?.await()

            // Actualizar en Firestore
            currentUser?.uid?.let { uid ->
                firestore.collection("users")
                    .document(uid)
                    .update("displayName", displayName)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener datos del usuario desde Firestore
    suspend fun getUserData(uid: String): Result<User> {
        return try {
            // Migrar campos legacy si existen
            migrateLegacyUserFields(uid)

            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Usuario no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar foto de perfil: sube a Storage y guarda URL en Auth/Firestore
    suspend fun updatePhoto(uri: Uri): Result<String> {
        return try {
            val uid = currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val ref = storage.reference.child("profile_photos/$uid.jpg")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(downloadUrl))
                .build()
            currentUser?.updateProfile(profileUpdates)?.await()

            firestore.collection("users")
                .document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Observador en tiempo real del documento de usuario
    fun observeUser(uid: String, onChange: (User?) -> Unit): ListenerRegistration {
        return firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                onChange(user)
            }
    }

    // Migración: renombrar campos legacy 'admin' -> 'isAdmin' y 'active' -> 'isActive'
    private suspend fun migrateLegacyUserFields(uid: String) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            val data = doc.data
            if (data != null) {
                val updates = mutableMapOf<String, Any>()
                var needUpdate = false

                if (data.containsKey("admin")) {
                    val admin = (data["admin"] as? Boolean) ?: false
                    updates["isAdmin"] = admin
                    updates["admin"] = FieldValue.delete()
                    needUpdate = true
                }
                if (data.containsKey("active")) {
                    val active = (data["active"] as? Boolean) ?: true
                    updates["isActive"] = active
                    updates["active"] = FieldValue.delete()
                    needUpdate = true
                }

                if (needUpdate) {
                    firestore.collection("users")
                        .document(uid)
                        .update(updates)
                        .await()
                }
            }
        } catch (_: Exception) {
            // Ignorar migración si falla para no bloquear lectura
        }
    }

    // Cerrar sesión
    fun signOut() {
        auth.signOut()
    }

    // Verificar si el usuario está autenticado
    fun isUserAuthenticated(): Boolean {
        return currentUser != null
    }
}
