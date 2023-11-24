package com.example.happybirthday.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.happybirthday.databinding.ItemDayBinding
import com.example.happybirthday.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDayBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int {
        return events.size
    }

    inner class EventViewHolder(private val binding: ItemDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(event: Event) {
            binding.day.text = formatDate(event.eventDate)
            binding.name.text = event.eventName
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(eventDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return eventDate.format(formatter)
    }
}