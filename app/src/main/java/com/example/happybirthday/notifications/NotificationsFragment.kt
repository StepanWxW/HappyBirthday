package com.example.happybirthday.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happybirthday.R
import com.example.happybirthday.adapter.FaqAdapter


import com.example.happybirthday.databinding.FragmentNotificationsBinding
import com.example.happybirthday.model.Event
import com.example.happybirthday.model.FaqItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.layoutManager = layoutManager
        viewLifecycleOwner.lifecycleScope.launch {
            val faqCollection = firestore.collection("faq")
            try {
                // Получаем все документы из коллекции "events" пользователя
                val querySnapshot = faqCollection.get().await()
                val faqList: MutableList<FaqItem> = mutableListOf()
                // Преобразуем результат запроса в список событий
                querySnapshot.documents.map { document ->
                    val question = document.getString("question") ?: ""
                    val answer = document.getString("answer") ?: ""
                    val faq = FaqItem(question, answer)
                    faqList.add(faq)
                }
                val adapter = FaqAdapter(requireContext(), faqList)
                binding.recycler.adapter = adapter

            } catch (e: Exception) {
                // Обработка ошибок
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
//        val faqList = listOf(
//            FaqItem("Как удалить событие?", "Для это в списке событий потяните элемент влево или вправо до конца."),
//            FaqItem("Почему так мало функций?", "Данное приложение было написанно мной как учебное, всего за несколько дней. Я буду очень рад если вы оставите отзыв. В будущем, возможно функции будут дополняться."),
//            FaqItem("Зачем регистрироваться?", "Чтобы вы могли получить push уведомление, а также все данные сохраняются на сервере и если вы смените устройство, то они никуда не пропадаут."),
//            FaqItem("Как я узнаю что настал нужный мне день?", "Вам прийдет push уведомление на телефон. Время прихода уведомления можно задать в настройках. Стандартно оно выставленно на 9 утра по Москве. Выставлять время можете по часовому поясу у вас в телефоне, то есть по вашему местному времени."),
//        )
//
//        val adapter = FaqAdapter(requireContext(), faqList)
//        binding.recycler.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}