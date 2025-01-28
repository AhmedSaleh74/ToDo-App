package com.`as`.collegealert.pojo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.`as`.collegealert.R
import com.`as`.collegealert.data.EventsDAO
import com.`as`.collegealert.data.EventsDatabase
import com.`as`.collegealert.ui.MainActivity

class EventNotification(conext:Context,params:WorkerParameters):Worker(conext,params){
    override fun doWork(): Result {
        Log.d("workManager" , "Completed")
        //create channel
        createChannel()
        val eventsDatabase = EventsDatabase.getInstance(applicationContext).eventsDao()
        val event = eventsDatabase
        val intent = Intent(applicationContext,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext,1,intent,PendingIntent.FLAG_IMMUTABLE)
        //create notification
        val notif = NotificationCompat.Builder(applicationContext,"default")
            .setSmallIcon(R.drawable.ic_time)
            .setContentTitle("")
            .setContentText("")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(1,notif)
        } catch (e: SecurityException) {}

        return Result.success()
    }
    fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default","default",NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager  = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}