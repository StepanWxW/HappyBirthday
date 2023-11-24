package com.example.happybirthday.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happybirthday.adapter.EventAdapter
import com.example.happybirthday.databinding.FragmentHomeBinding
import com.example.happybirthday.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var auth: FirebaseAuth? = null
    private lateinit var firestore: FirebaseFirestore
    private var selectedDate: MutableMap<String, Any>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val currentUser = auth?.currentUser
        if (currentUser != null) {
            binding.textReg.visibility = View.GONE
            binding.imageUser.visibility = View.GONE
            lifecycleScope.launch {
                val userEvents = getUserEvents()
                if(userEvents.isEmpty()){
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.imageAdd.visibility = View.VISIBLE
                }
                val layoutManager = LinearLayoutManager(requireContext())
                binding.recycler.layoutManager = layoutManager
                val adapter = EventAdapter(userEvents)
                binding.recycler.adapter = adapter
            }
        } else {
            binding.recycler.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserEvents(): List<Event> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            val eventsCollection = firestore.collection("users").document(userId).collection("events")

            try {
                // Получаем все документы из коллекции "events" пользователя
                val querySnapshot = eventsCollection.get().await()

                // Преобразуем результат запроса в список событий
                val events = querySnapshot.documents.map { document ->
                    val eventId = document.id
                    val eventName = document.getString("name") ?: ""
                    val day = document.getLong("day") ?: 0
                    val month = document.getLong("month") ?: 0
                    val year = document.getLong("year") ?: 0
                    val eventDate = LocalDate.of(year.toInt(), month.toInt(), day.toInt())

                    Event(eventId, eventName, eventDate)
                }
                val currentDate = LocalDate.now()
                val newEvents = events.sortedWith(compareBy(
                    { ChronoUnit.MONTHS.between(currentDate.withDayOfMonth(1), it.eventDate.withDayOfMonth(1)) % 12 },
                    { it.eventDate.dayOfMonth }
                ))

                val (futureEvents, pastEvents) = newEvents.partition {
                    ChronoUnit.DAYS.between(currentDate, it.eventDate) > 0
                }

// Фильтруем события из pastEvents, которые относятся к сегодняшнему дню
                val todayPastEvents = pastEvents.filter {
                    it.eventDate.dayOfMonth == currentDate.dayOfMonth &&
                            it.eventDate.month == currentDate.month
                }

// Создаем объединенный список
                val combinedEvents = todayPastEvents + futureEvents + pastEvents.minus(todayPastEvents)

                return combinedEvents
            } catch (e: Exception) {
                // Обработка ошибок
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        return emptyList()
    }
//        // В случае ошибки или отсутствия пользователя возвращаем пустой список
//        return emptyList()
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}