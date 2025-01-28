package com.`as`.collegealert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.`as`.collegealert.ui.MainActivity
import com.`as`.collegealert.data.EventsDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class EventNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // إنشاء قناة الإشعارات
        createNotificationChannel()

        // الحصول على قاعدة البيانات
        val eventsDatabase = EventsDatabase.getInstance(applicationContext)
        val events = eventsDatabase.eventsDao().getEvents()

        // الوقت الحالي
        val currentTime = System.currentTimeMillis()

        for (event in events) {
            // تحويل تاريخ ووقت الحدث إلى timestamp
            val eventTimestamp = parseEventTime(event.eventDate, event.eventTime)

            // حساب التأخير (الوقت المتبقي حتى الحدث)
            val delay = eventTimestamp - currentTime

            // إذا كان التأخير موجبًا (الحدث لم يحدث بعد)، يتم جدولة الإشعار
            if (delay > 0) {
                sendNotification(event.eventDescription, delay)
            }
        }

        return Result.success()
    }

    private fun sendNotification(eventDescription: String, delay: Long) {
        // إنشاء نية (Intent) لفتح التطبيق عند النقر على الإشعار
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // إنشاء الإشعار
        val notification = NotificationCompat.Builder(applicationContext, "event_channel")
            .setSmallIcon(R.drawable.ic_collegealert) // أيقونة صغيرة (تأكد من وجودها في مجلد drawable)
            .setContentTitle("Event Reminder") // عنوان الإشعار
            .setContentText("Don't forget: $eventDescription") // نص الإشعار
            .setPriority(NotificationCompat.PRIORITY_HIGH) // أولوية الإشعار
            .setAutoCancel(true) // إغلاق الإشعار عند النقر عليه
            .setContentIntent(pendingIntent) // النية المعلقة
            .build()

        // إرسال الإشعار
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        try {
            notificationManager.notify(eventDescription.hashCode(), notification) // استخدام hashCode كمعرف فريد
            Log.d("EventNotificationWorker", "Notification sent for event: $eventDescription")
        } catch (e: SecurityException) {
            Log.e("EventNotificationWorker", "Failed to send notification: ${e.message}")
        }
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

    private fun parseEventTime(eventDate: String, eventTime: String): Long {
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val dateTime = "$eventDate $eventTime"
        return format.parse(dateTime)?.time ?: 0L
    }
}