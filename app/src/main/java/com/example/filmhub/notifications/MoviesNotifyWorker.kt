package com.example.filmhub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.filmhub.MainActivity
import com.example.filmhub.R
import com.example.filmhub.data.remote.RetrofitClient
import java.util.concurrent.TimeUnit

class MoviesNotifyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        createChannel()
        try {
            val upcoming = RetrofitClient.tmdbApi.getUpcomingMovies()
            val movie = if (upcoming.isSuccessful) {
                upcoming.body()?.results?.firstOrNull()
            } else null
            val fallback = if (movie == null) {
                val popular = RetrofitClient.tmdbApi.getPopularMovies()
                if (popular.isSuccessful) popular.body()?.results?.firstOrNull() else null
            } else null
            val m = movie ?: fallback
            if (m != null) {
                val title = m.title ?: "Nueva película"
                val text = if (!m.releaseDate.isNullOrEmpty()) "Estreno: ${m.releaseDate}" else "Popular ⭐ ${String.format("%.1f", m.voteAverage ?: 0.0)}"
                notifyMovie(m.id, title, text)
            } else {
                notifyMovie(999999, "Películas destacadas", "Activa las notificaciones para ver estrenos y populares")
            }
        } catch (_: Exception) {}
        return Result.success()
    }

    private fun notifyMovie(id: Int, title: String, text: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("startRoute", "movie_detail/$id")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        NotificationManagerCompat.from(applicationContext).notify(id, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Estrenos y Populares", NotificationManager.IMPORTANCE_HIGH)
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "filmhub_movies"
        fun schedule(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val work = PeriodicWorkRequestBuilder<MoviesNotifyWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "filmhub_movies_periodic",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
        }

        fun triggerOnce(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val once = androidx.work.OneTimeWorkRequestBuilder<MoviesNotifyWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueue(once)
        }
    }
}
