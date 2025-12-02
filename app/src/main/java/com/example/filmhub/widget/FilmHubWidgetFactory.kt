package com.example.filmhub.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.filmhub.R
import com.example.filmhub.data.remote.ApiConstants
import com.example.filmhub.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import java.net.URL

class FilmHubWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    data class Item(
        val id: Int,
        val title: String,
        val rating: Double,
        val posterBitmap: Bitmap?
    )

    private val items = mutableListOf<Item>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        items.clear()
        runBlocking {
            try {
                val resp = RetrofitClient.tmdbApi.getPopularMovies()
                if (resp.isSuccessful) {
                    val results = resp.body()?.results ?: emptyList()
                    results.take(8).forEach { m ->
                        val url = ApiConstants.getPosterUrl(m.posterPath)
                        val bmp = if (url.isNotEmpty()) loadBitmap(url) else null
                        items.add(Item(m.id, m.title ?: "", m.voteAverage ?: 0.0, bmp))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_item_movie)
        rv.setTextViewText(R.id.item_title, item.title)
        rv.setTextViewText(R.id.item_rating, "⭐ ${String.format("%.1f", item.rating)}")
        item.posterBitmap?.let { rv.setImageViewBitmap(R.id.item_poster, it) }

        val fill = android.content.Intent()
        fill.putExtra("startRoute", "movie_detail/${item.id}")
        rv.setOnClickFillInIntent(R.id.widget_item_root, fill)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun hasStableIds(): Boolean = true

    private fun loadBitmap(url: String): Bitmap? {
        return try {
            val conn = URL(url).openConnection()
            conn.connect()
            conn.getInputStream().use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }
    }
}

