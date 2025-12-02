package com.example.filmhub.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.filmhub.MainActivity
import com.example.filmhub.R

class FilmHubWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_filmhub)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Quick actions
            val toMyLists = Intent(context, MainActivity::class.java).apply {
                putExtra("startRoute", com.example.filmhub.navigation.Screen.MyLists.route)
            }
            val toWatchlist = Intent(context, MainActivity::class.java).apply {
                putExtra("startRoute", com.example.filmhub.navigation.Screen.Watchlist.route)
            }
            val toHelp = Intent(context, MainActivity::class.java).apply {
                putExtra("startRoute", com.example.filmhub.navigation.Screen.HelpCenter.route)
            }

            val pFavorites = PendingIntent.getActivity(context, 1, toMyLists, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pWatchlist = PendingIntent.getActivity(context, 2, toWatchlist, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pHelp = PendingIntent.getActivity(context, 3, toHelp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            views.setOnClickPendingIntent(R.id.action_favorites, pFavorites)
            views.setOnClickPendingIntent(R.id.action_watchlist, pWatchlist)
            views.setOnClickPendingIntent(R.id.action_help, pHelp)

            // Collection: popular/tendencias
            val svcIntent = Intent(context, FilmHubWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = android.net.Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, svcIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)
            val templateIntent = Intent(context, MainActivity::class.java)
            val templatePending = PendingIntent.getActivity(
                context, 100, templateIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, templatePending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }
    }
}
