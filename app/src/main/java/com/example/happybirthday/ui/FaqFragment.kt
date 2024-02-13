package com.example.happybirthday.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happybirthday.ui.adapter.FaqAdapter
import com.example.happybirthday.databinding.FragmentFaqBinding
import com.example.happybirthday.model.FaqItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FaqFragment : Fragment() {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!
    private var firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
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
                Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}