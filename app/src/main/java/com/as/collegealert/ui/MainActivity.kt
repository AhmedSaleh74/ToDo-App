package com.`as`.collegealert.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.`as`.collegealert.data.EventsDatabase
import com.`as`.collegealert.R
import com.`as`.collegealert.databinding.ActivityMainBinding
import com.xwray.groupie.GroupieAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(),OnClickListener{
    lateinit var binding : ActivityMainBinding
    lateinit var adapter : GroupieAdapter
    lateinit var eventsDatabase: EventsDatabase
    lateinit var pool: ExecutorService
    val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        loadEvents()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        pool.shutdown()
    }
    private fun searchEvents(){
        binding.searchEvents.doAfterTextChanged { eventName ->
            pool.submit{
                val event = eventsDatabase.eventsDao().getEvents()
                if (event.isNotEmpty()){
                    val eventName = event.filter { it.eventDescription.startsWith(eventName.toString().lowercase())}.map { EventItem(it) }
                    runOnUiThread {
                        adapter.clear()
                        adapter.addAll(eventName)
                    }
                }
            }
        }
    }
    private fun loadEvents() {
        pool.submit {
            val events = eventsDatabase.eventsDao().getEvents()
            if (events.isNotEmpty()) {
                val items = events.map { event -> EventItem(event) }
                runOnUiThread {
                    adapter.clear()
                    adapter.addAll(items)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show()
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
    fun addEvent(){
        val intent = Intent(this, AddItemActivity::class.java)
        launcher.launch(intent)
        finish()
    }
    override fun onClick(v: View) {
        when(v.id){
            R.id.addEventFAB -> addEvent()
        }
    }


}