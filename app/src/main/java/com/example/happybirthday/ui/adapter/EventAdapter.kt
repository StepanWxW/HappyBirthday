package com.example.happybirthday.ui.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.happybirthday.R
import com.example.happybirthday.databinding.ItemDayBinding
import com.example.happybirthday.model.MyEvent
import java.time.LocalDateTime

class EventAdapter(private val events: List<MyEvent>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

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

        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(event: MyEvent) {
            binding.day.text = convertDate(event)
            binding.name.text = "${event.lastName} ${event.firstName} ${event.patronymic}"
            val long0: Long = 0
            if(event.telephone != long0) {
                binding.phoneText.visibility = View.VISIBLE
                binding.phone.visibility = View.VISIBLE
                binding.phone.text = event.telephone.toString()
            } else {
                binding.phoneText.visibility = View.GONE
                binding.phone.visibility = View.GONE
            }
            val now = LocalDateTime.now()
            if((now.month.value - 1) == event.month.toInt() && now.dayOfMonth == event.day.toInt()){
                binding.item.setBackgroundResource(R.drawable.rounded_rectangle_title_active)
            } else {
                binding.item.setBackgroundResource(R.drawable.rounded_rectangle_title_not_active)
            }
        }
    }
    fun convertDate(event: MyEvent): String{
        val months = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        return "${event.day} ${months[event.month.toInt()]} ${event.year} года."
    }
}