package com.example.filmhub.data.remote

object ApiConstants {
    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val IMAGE_SIZE_POSTER = "w500"
    const val IMAGE_SIZE_BACKDROP = "w780"

    // Reemplaza con tu API Key de TMDb
    const val API_KEY = "022889fab5e12a42a70b3f84a8ae5465" // <-- ¡Aquí va tu clave!

    // Construir URL de imagen
    fun getPosterUrl(posterPath: String?): String {
        return if (posterPath != null) {
            "$IMAGE_BASE_URL$IMAGE_SIZE_POSTER$posterPath"
        } else {
            ""
        }
    }

    fun getBackdropUrl(backdropPath: String?): String {
        return if (backdropPath != null) {
            "$IMAGE_BASE_URL$IMAGE_SIZE_BACKDROP$backdropPath"
        } else {
            ""
        }
    }
}