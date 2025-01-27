package com.`as`.collegealert.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events_table")
data class Event(val eventDescription:String, var eventDate:String, var eventTime:String){
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
}