package com.`as`.collegealert.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.`as`.collegealert.pojo.Date
import com.`as`.collegealert.data.Event
import com.`as`.collegealert.data.EventsDatabase
import com.`as`.collegealert.R
import com.`as`.collegealert.pojo.Time
import com.`as`.collegealert.databinding.ActivityAddItemBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddItemActivity : AppCompatActivity(), OnClickListener {
    lateinit var binding: ActivityAddItemBinding
    lateinit var date: Date
    lateinit var time: Time
    lateinit var pool: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        date = Date(this)
        time = Time(this)
        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        binding.createTaskButton.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        binding.selectedDateText.setOnClickListener(this)
        binding.selectedTimeText.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        pool.shutdown()
    }

    fun backButton() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun createEvent() {
        val eventDescription = binding.taskInput.text.toString().lowercase()
        val eventDate = date.selectedDate
        val eventTime = time.selectedTime

        if (eventDescription.isEmpty() || eventTime.isEmpty() || eventDate.isEmpty()) {
            Toast.makeText(this, "Please Complete Fields", Toast.LENGTH_SHORT).show()
            return
        }

        pool.submit {
            val event = Event(eventDescription, eventDate, eventTime)
            val eventsDatabase = EventsDatabase.getInstance(this)
                eventsDatabase.eventsDao().insertEvent(event)
                runOnUiThread {
                    Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
    }

    override fun onClick(v: View) {
        when (v.id){
            R.id.backButton -> backButton()
            R.id.createTaskButton -> createEvent()
            R.id.selectedDateText -> date.showDatePickerDialog()
            R.id.selectedTimeText -> time.showTimePickerDialog()
        }
    }
}