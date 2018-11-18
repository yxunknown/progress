package com.hercats.dev.timers.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.hercats.dev.timers.R
import com.hercats.dev.timers.entity.*
import com.hercats.dev.timers.service.UpdateWidgetService
import com.tencent.mmkv.MMKV
import org.jetbrains.anko.intentFor
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class ProgressWidget : AppWidgetProvider() {
    private val mmkv = MMKV.defaultMMKV()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        val str = mmkv.decodeString("widget_progress", "")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                str
            )
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        // start update service
        val startUpdateServiceIntent = context.intentFor<UpdateWidgetService>()
        context.startService(startUpdateServiceIntent)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int, progressStr: String
        ) {
            // get progress
            val progress = if (progressStr.isBlank()) {
                getThisYearProgress()
            } else {
                parseJsonToProgress(progressStr)
            }
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.progress_widget)
            views.setTextViewText(R.id.tv_progress_name, progress.name)
            val progressNumber = getProgress(progress)
            views.setInt(R.id.pb_progress, "setProgress", progressNumber.toInt())
            views.setTextViewText(R.id.tv_progress, "$progressNumber%")
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

