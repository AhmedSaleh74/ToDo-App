package com.`as`.collegealert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.`as`.collegealert.ui.MainActivity

class EventNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // إنشاء قناة الإشعارات
        createNotificationChannel()
        // انشاء الاشعار
        val eventDescription: String
        if (inputData.getString("eventDescription") != null) {
            eventDescription = inputData.getString("eventDescription")!!
        } else {
            eventDescription = "Upcoming Event"
        }
        sendNotification(eventDescription)
        return Result.success()
    }

    private fun sendNotification(eventDescription: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "event_channel")
            .setSmallIcon(R.drawable.ic_collegealert)
            .setContentTitle("Event Reminder")
            .setContentText("Don't forget: $eventDescription")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        try {
            notificationManager.notify(eventDescription.hashCode(), notification)
        } catch (e: SecurityException) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            if (notificationManager.getNotificationChannel("event_channel") == null) {
                val channel = NotificationChannel(
                    "event_channel",
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}