package com.example.filmhub.utils

import android.content.Context
import android.content.Intent
import com.example.filmhub.data.model.MovieList

object ShareUtils {

    // HU39-EP20: Compartir lista en redes sociales
    fun shareList(context: Context, list: MovieList) {
        val shareText = buildString {
            append("🎬 ${list.title}\n\n")
            if (list.description.isNotEmpty()) {
                append("${list.description}\n\n")
            }
            append("📽️ ${list.movieIds.size} películas\n\n")
            append("¡Mira mi lista en FilmHub!")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Compartir lista")
        context.startActivity(shareIntent)
    }

    // Compartir película
    fun shareMovie(context: Context, movieTitle: String, movieId: Int) {
        val shareText = buildString {
            append("🎬 $movieTitle\n\n")
            append("¡Te recomiendo esta película!\n\n")
            append("Encuéntrala en FilmHub")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Compartir película")
        context.startActivity(shareIntent)
    }

    // HU44-EP22: Compartir trailer
    fun shareTrailer(context: Context, movieTitle: String, trailerUrl: String) {
        val shareText = buildString {
            append("🎬 Mira el trailer de $movieTitle\n\n")
            append(trailerUrl)
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Compartir trailer")
        context.startActivity(shareIntent)
    }
}