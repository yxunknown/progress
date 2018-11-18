package com.hercats.dev.timers.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.SystemClock
import android.widget.RemoteViews
import com.hercats.dev.timers.R
import com.hercats.dev.timers.entity.getProgress
import com.hercats.dev.timers.entity.getThisYearProgress
import com.hercats.dev.timers.entity.parseJsonToProgress
import com.hercats.dev.timers.widget.ProgressWidget
import com.tencent.mmkv.MMKV
import org.jetbrains.anko.intentFor
import java.util.*

// try to protect the service from system kill, use AlarmManager to restart this service
const val SERVICE_SELF_START_DURATION = 5 * 60 * 1000 // FIVE MINUTES
// the interval to update the widget
const val WIDGET_UPDATE_DURATION = 1000L
// handler msg flag
const val UPDATE_WIDGET_FLAG = 8102  // 2018 YEAR


class UpdateWidgetService: Service() {

    private val mmkv = MMKV.defaultMMKV()

    private val timer = Timer()
    private val task = UpdateTask()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // self start to prevent from system killing
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = intentFor<UpdateWidgetService>()
        val pendingIntent = PendingIntent.getService(baseContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + SERVICE_SELF_START_DURATION,
            pendingIntent)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        timer.schedule(task, 0L, WIDGET_UPDATE_DURATION)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        task.cancel()
    }

    fun updateWidget() {
        val progressStr = mmkv.decodeString("widget_progress", "")
        val progress = if (progressStr.isBlank()) {
            getThisYearProgress()
        } else {
            parseJsonToProgress(progressStr)
        }
        val componentName = ComponentName(this, ProgressWidget::class.java)
        // Construct the RemoteViews object
        val views = RemoteViews(packageName, R.layout.progress_widget)
        views.setTextViewText(R.id.tv_progress_name, progress.name)
        val progressNumber = getProgress(progress)
        views.setInt(R.id.pb_progress, "setProgress", progressNumber.toInt())
        views.setTextViewText(R.id.tv_progress, "$progressNumber%")
        // Instruct the widget manager to update the widget
        // notify data changed
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        appWidgetManager.updateAppWidget(componentName, views)
        println("update widget done")
    }

    inner class UpdateTask: TimerTask() {
        override fun run() {
            this@UpdateWidgetService.updateWidget()
        }
    }

}
