package com.example.happybirthday.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.happybirthday.R
import com.example.happybirthday.databinding.FragmentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var selectedDate: MutableMap<String, Any>? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
//            binding.nik.text = currentUser.email
        } else {
            findNavController().navigate(R.id.action_navigation_user_to_AuthFragment)
        }
        binding.dateInput.setOnClickListener {
            showDatePickerDialog()
        }
        binding.save.setOnClickListener{
            saveDateToFirestore()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Установите фокус на другой элемент
        binding.root.requestFocus()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = hashMapOf(
                    "day" to selectedDay,
                    "month" to selectedMonth + 1, // Месяцы в Calendar начинаются с 0
                    "year" to selectedYear
                )

                binding.dateInput.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")

            },
            year, month, dayOfMonth
        )

        datePickerDialog.show()
    }


    private fun saveDateToFirestore() {
        val userId = auth.currentUser?.uid
        hideKeyboard()
        if (userId != null) {

            val nameInput = binding.nameInput.text.toString()
            selectedDate?.set("name", nameInput)

            val userCollection = firestore.collection("users").document(userId).collection("events")

            selectedDate?.let {
                userCollection.add(it)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Успешно", Toast.LENGTH_SHORT).show()
                        binding.nameInput.setText("")
                        binding.dateInput.setText("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Не успешно", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}