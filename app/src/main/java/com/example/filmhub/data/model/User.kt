package com.example.filmhub.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    @get:PropertyName("isAdmin") @set:PropertyName("isAdmin") var isAdmin: Boolean = false,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val deactivatedAt: Long? = null
)
