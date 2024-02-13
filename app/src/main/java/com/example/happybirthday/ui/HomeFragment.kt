package com.example.happybirthday.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.ui.adapter.EventAdapter
import com.example.happybirthday.databinding.FragmentHomeBinding
import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.model.MyStatus
import com.example.happybirthday.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var auth: FirebaseAuth? = null
    private var userEvents: MutableList<MyEvent>? = null
    private var adapter: EventAdapter? = null

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
//        auth = Firebase.auth
//        val currentUser = auth?.currentUser
//        if (currentUser != null) {
//            showEvents(currentUser)
//        }
        val auth = Firebase.auth
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null && binding != null) {
                showEvents(currentUser)
            }
        }
        auth.addAuthStateListener(authStateListener)
        listenerDeleteItem()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEvents(currentUser: FirebaseUser) {
        lifecycleScope.launch {
            try {
                val events = ApiClient.apiService.getAll(currentUser.uid) as MutableList<MyEvent>
                binding.textEmpty.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                val layoutManager = LinearLayoutManager(requireContext())
                binding.recycler.layoutManager = layoutManager

                val combinedEvents = sortedEvent(events)
                userEvents = combinedEvents as MutableList<MyEvent>

                adapter = EventAdapter(combinedEvents)
                binding.recycler.adapter = adapter
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                showToast("Ошибка получения данных")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortedEvent(events: MutableList<MyEvent>): List<MyEvent> {
        val sortedEvents = events.sortedWith(compareBy(
            { it.month },
            { it.day }
        ))
        Log.d("MyTag", sortedEvents.toString())
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.month.value - 1
        val (beforeToday, todayAndAfter) = sortedEvents.partition {
            it.month < currentMonth ||
                    (it.month.toInt() == currentMonth && it.day < currentDate.dayOfMonth)
        }
        val eventsNew = todayAndAfter + beforeToday
        Log.d("MyTag", eventsNew.toString())
        return eventsNew
    }

    private fun listenerDeleteItem() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showDeleteConfirmationDialog(viewHolder.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recycler)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите удалить это событие?")
            .setPositiveButton("Да") { _, _ ->
                val eventAtIndex = userEvents?.get(position)
                lifecycleScope.launch {
                    if (eventAtIndex != null) {
                        val response = ApiClient.apiService.deleteEvent(
                            eventAtIndex.uid,
                            eventAtIndex.id!!
                        )
                        changeUI(response, position)
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                val eventAtIndex = userEvents?.get(position)
                eventAtIndex?.let {
                    // Удаляем элемент из текущей позиции
                    userEvents?.removeAt(position)
                    // Вставляем элемент обратно на ту же самую позицию
                    userEvents?.add(position, it)
                    // Уведомляем адаптер о изменении данных
                    adapter?.notifyDataSetChanged()
                }
                // В случае отмены, закрываем диалог
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun changeUI(
        response: Response<MyStatus>,
        position: Int
    ) {
        if (response.isSuccessful) {
            userEvents?.removeAt(position)
            adapter?.notifyItemRemoved(position)
            showToast("Удален")
        } else {
            showToast("Ошибка удаления")
        }
    }


    //    @RequiresApi(Build.VERSION_CODES.O)
//    suspend fun getUserEvents(): MutableList<Event> {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        userId = currentUser?.uid
//
//        if (userId != null) {
//            val eventsCollection = firestore.collection("users").document(userId!!).collection("events")
//
//            try {
//                // Получаем все документы из коллекции "events" пользователя
//                val querySnapshot = eventsCollection.get().await()
//
//                // Преобразуем результат запроса в список событий
//                val events = querySnapshot.documents.map { document ->
//                    val eventId = document.id
//                    val eventName = document.getString("name") ?: ""
//                    val day = document.getLong("day") ?: 0
//                    val month = document.getLong("month") ?: 0
//                    val year = document.getLong("year") ?: 0
//                    val eventDate = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
//
//                    Event(eventId, eventName, eventDate)
//                }
//                val currentDate = LocalDate.now()
//
//
//                val sortedEvents = events.sortedWith(compareBy(
//                    { it.eventDate.month.value },
//                    { it.eventDate.dayOfMonth }
//                ))
//
//                val (beforeToday, todayAndAfter) = sortedEvents.partition {
//                    it.eventDate.month.value < currentDate.month.value ||
//                            (it.eventDate.month.value == currentDate.month.value && it.eventDate.dayOfMonth < currentDate.dayOfMonth)
//                }
//                val combinedEvents = todayAndAfter + beforeToday
//                return combinedEvents as MutableList<Event>
//            } catch (e: Exception) {
//                // Обработка ошибок
//                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
//                e.printStackTrace()
//            }
//        }
//
//        return mutableListOf()
//    }
//        // В случае ошибки или отсутствия пользователя возвращаем пустой список
//        return emptyList()
//    }
    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}