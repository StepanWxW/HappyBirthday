package com.example.happybirthday.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.ui.adapter.QuestionAdapter
import com.example.happybirthday.databinding.FragmentFaqBinding
import kotlinx.coroutines.launch

class FaqFragment : Fragment() {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

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
            val questions = ApiClient.apiService.getQuestions()
            val adapter = QuestionAdapter(questions)
            binding.recycler.adapter = adapter
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}