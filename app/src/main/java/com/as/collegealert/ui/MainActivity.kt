package com.`as`.collegealert.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.work.*
import com.`as`.collegealert.EventNotificationWorker
import com.`as`.collegealert.data.EventsDatabase
import com.`as`.collegealert.R
import com.`as`.collegealert.databinding.ActivityMainBinding
import com.xwray.groupie.GroupieAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: GroupieAdapter
    lateinit var eventsDatabase: EventsDatabase
    lateinit var pool: ExecutorService

    val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        loadEvents()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduleAllEventNotifications()
        } else {
            Log.e("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        eventsDatabase = EventsDatabase.getInstance(this)
        binding.addEventFAB.setOnClickListener(this)
        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        adapter = GroupieAdapter()
        binding.eventsRV.adapter = adapter

        loadEvents()
        searchEvents()
        checkNotificationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        pool.shutdown()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleAllEventNotifications()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleAllEventNotifications()
        }
    }

    private fun scheduleAllEventNotifications() {
        pool.submit {
            val events = eventsDatabase.eventsDao().getEvents()
            for (event in events) {
                val eventTimestamp = parseEventTime(event.eventDate, event.eventTime)
                scheduleEventNotificationWorker(eventTimestamp, event.eventDescription)
            }
        }
    }

    private fun scheduleEventNotificationWorker(eventTimeInMillis: Long, eventDescription: String) {
        val currentTime = System.currentTimeMillis()
        val delay = eventTimeInMillis - currentTime

        if (delay > 0) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val data = Data.Builder()
                .putString("eventDescription", eventDescription)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setConstraints(constraints)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)
            Log.d("MainActivity", "Notification scheduled for $eventDescription at $eventTimeInMillis")
        }
    }

    private fun parseEventTime(eventDate: String, eventTime: String): Long {
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val dateTime = "$eventDate $eventTime"
        return format.parse(dateTime)?.time ?: 0L
    }

    private fun searchEvents() {
        binding.searchEvents.doAfterTextChanged { eventName ->
            pool.submit {
                val events = eventsDatabase.eventsDao().getEvents()
                if (events.isNotEmpty()) {
                    val filteredEvents = events.filter { it.eventDescription.startsWith(eventName.toString(), true) }
                        .map { EventItem(it) }
                    runOnUiThread {
                        adapter.clear()
                        adapter.addAll(filteredEvents)
                    }
                }
            }
        }
    }

    private fun loadEvents() {
        pool.submit {
            val events = eventsDatabase.eventsDao().getEvents()
            if (events.isNotEmpty()) {
                val items = events.map { EventItem(it) }
                runOnUiThread {
                    adapter.clear()
                    adapter.addAll(items)
                }
            }
        }
    }

    fun deleteEventById(eventId: Int) {
        pool.submit {
            val eventsDatabase = EventsDatabase.getInstance(this)
            eventsDatabase.eventsDao().deleteEventById(eventId)
            val events = eventsDatabase.eventsDao().getEvents()

            runOnUiThread {
                val items = events.map { event -> EventItem(event) }
                adapter.clear()
                adapter.addAll(items)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.addEventFAB -> {
                val intent = Intent(this, AddItemActivity::class.java)
                launcher.launch(intent)
                finish()
            }
        }
    }
}
