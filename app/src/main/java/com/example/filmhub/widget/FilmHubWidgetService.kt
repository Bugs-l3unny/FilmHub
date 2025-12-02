package com.example.filmhub.widget

import android.content.Intent
import android.widget.RemoteViewsService

class FilmHubWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return FilmHubWidgetFactory(applicationContext)
    }
}

