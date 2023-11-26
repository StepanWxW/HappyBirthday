package com.example.happybirthday.ui

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var selectedDate: MutableMap<String, Any>? = null
    private var userEvents: MutableList<Event>? = null
    private var adapter: EventAdapter? = null
    private var firestore = FirebaseFirestore.getInstance()
    private var userId: String? = null

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
            viewLifecycleOwner.lifecycleScope.launch {
                userEvents = getUserEvents()
                if(userEvents!!.isEmpty()){
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.imageAdd.visibility = View.VISIBLE
                }
                val layoutManager = LinearLayoutManager(requireContext())
                binding.recycler.layoutManager = layoutManager
                adapter = EventAdapter(userEvents!!)
                binding.recycler.adapter = adapter
            }
        } else {
            binding.recycler.visibility = View.GONE
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Не реализовываем, так как не поддерживаем перемещение элементов
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Вызывается при свайпе влево или вправо
//                val position = viewHolder.adapterPosition
//                // Удаление элемента из списка
//                userEvents?.removeAt(position)
//                // Уведомление адаптера о изменении данных
//                adapter?.notifyItemRemoved(position)

                showDeleteConfirmationDialog(viewHolder.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recycler)

    }

    fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите удалить это событие?")
            .setPositiveButton("Да") { _, _ ->
                val eventAtIndex = userEvents?.get(position)

// Замените "documentIdToDelete" на фактический идентификатор документа, который вы хотите удалить
                val documentIdToDelete = eventAtIndex?.eventId
                val eventsCollection = userId?.let { firestore.collection("users").document(it).collection("events") }

                if (documentIdToDelete != null) {
                    eventsCollection?.document(documentIdToDelete)
                        ?.delete()
                        ?.addOnSuccessListener {
                            // Удаление элемента из списка
                            userEvents?.removeAt(position)
                            // Уведомление адаптера о изменении данных
                            adapter?.notifyItemRemoved(position)
                            // Успешно удалено
                            // Здесь вы можете выполнить дополнительные действия, если необходимо
                        }
                        ?.addOnFailureListener { e ->
                            // Ошибка при удалении
                            // Здесь вы можете обработать ошибку
                            Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                        }
                }


            }
            .setNegativeButton("Отмена") { dialog, _ ->
                // В случае отмены, закрываем диалог
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserEvents(): MutableList<Event> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId != null) {
            val eventsCollection = firestore.collection("users").document(userId!!).collection("events")

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


                val sortedEvents = events.sortedWith(compareBy(
                    { it.eventDate.month.value },
                    { it.eventDate.dayOfMonth }
                ))

                val (beforeToday, todayAndAfter) = sortedEvents.partition {
                    it.eventDate.month.value < currentDate.month.value ||
                            (it.eventDate.month.value == currentDate.month.value && it.eventDate.dayOfMonth < currentDate.dayOfMonth)
                }
                val combinedEvents = todayAndAfter + beforeToday
                return combinedEvents as MutableList<Event>
            } catch (e: Exception) {
                // Обработка ошибок
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        return emptyList<Event>() as MutableList<Event>
    }
//        // В случае ошибки или отсутствия пользователя возвращаем пустой список
//        return emptyList()
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}