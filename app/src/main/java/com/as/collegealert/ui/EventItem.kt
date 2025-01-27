package com.`as`.collegealert.ui

import android.view.View
import com.`as`.collegealert.data.Event
import com.`as`.collegealert.R
import com.`as`.collegealert.databinding.EventItemBinding
import com.xwray.groupie.viewbinding.BindableItem

class EventItem(private val event: Event): BindableItem<EventItemBinding>() {
    override fun bind(viewBinding: EventItemBinding, position: Int) {
        viewBinding.taskDescriptionText.text = event.eventDescription
        viewBinding.taskDateText.text = event.eventDate
        viewBinding.taskTimeText.text = event.eventTime
        viewBinding.deleteEventIV.setOnClickListener{
            (viewBinding.root.context as MainActivity).deleteEventById(event.id)
        }
    }

    override fun getLayout(): Int = R.layout.event_item

    override fun initializeViewBinding(view: View): EventItemBinding = EventItemBinding.bind(view)
}