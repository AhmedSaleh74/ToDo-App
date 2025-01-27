package com.`as`.collegealert.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [Event::class], version = 1)
abstract class EventsDatabase : RoomDatabase() {
    abstract fun eventsDao(): EventsDAO
    companion object {
        @Volatile
        private var instance: EventsDatabase? = null

        fun getInstance(context: Context): EventsDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventsDatabase::class.java,
                    "events_database"
                ).build()
            }
        }
    }
}
