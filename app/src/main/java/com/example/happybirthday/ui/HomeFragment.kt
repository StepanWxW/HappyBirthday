package com.example.happybirthday.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happybirthday.R
import com.example.happybirthday.adapter.EventAdapter
import com.example.happybirthday.databinding.FragmentHomeBinding
import com.example.happybirthday.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val currentUser = auth?.currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val userEvents = getUserEvents()

                val layoutManager = LinearLayoutManager(requireContext())
                binding.recycler.layoutManager = layoutManager

                val adapter = EventAdapter(userEvents)
                binding.recycler.adapter = adapter
            }
        } else {
            findNavController().navigate(R.id.navigation_user)
        }

    }

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
                return querySnapshot.documents.map { document ->
                    val eventId = document.id
                    val eventName = document.getString("name") ?: ""
                    val day = document.getLong("day") ?: ""
                    val month = document.getLong("month") ?: ""
                    val year = document.getLong("year") ?: ""
                    val eventDate = "$day/$month/$year"

                    Event(eventId, eventName, eventDate)
                }
            } catch (e: Exception) {
                // Обработка ошибок
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // В случае ошибки или отсутствия пользователя возвращаем пустой список
        return emptyList()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}