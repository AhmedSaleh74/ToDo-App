package com.`as`.collegealert.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event)
    @Query("select * from events_table")
    fun getEvents():List<Event>
    @Query("DELETE FROM events_table WHERE id = :eventId")
    fun deleteEventById(eventId: Int)
    @Query("DELETE FROM events_table")
    fun deleteAllEvents()
}