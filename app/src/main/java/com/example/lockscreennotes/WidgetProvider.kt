package com.example.lockscreennotes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        const val PREFS_NAME: String = "CurrentNote"
        const val KEY_TEXT: String = "text"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val widget = ComponentName(context, WidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(widget)
            updateAllWidgets(context, manager, ids)
        }

        private fun updateAllWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val text: String = prefs.getString(KEY_TEXT, "Click to create a note")!!

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget)
                views.setTextViewText(R.id.widget_text, text.trim())

                val intent = Intent(context, ImportantActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
                val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_text, pi)

                manager.updateAppWidget(id, views)
            }
        }
    }
}