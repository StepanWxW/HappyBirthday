package com.example.happybirthday.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
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
    private var isFilled = false
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val userId = auth.currentUser?.uid

        if(userId == null){
            binding.work.visibility = View.GONE
        } else {
            binding.textReg.visibility = View.GONE
            binding.imageUser.visibility = View.GONE
        }
        binding.changeDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.save.setOnClickListener {
            if (isFilled) {
                saveDateToFirestore()
            } else {
                Toast.makeText(requireContext(), "Нужно заполнить поля", Toast.LENGTH_LONG).show()
            }
        }
        textWatcher()
    }

    override fun onResume() {
        super.onResume()
        binding.number.setText("")
        binding.month.setText("")
        binding.year.setText("")
        binding.outlinedEditName.setText("")
    }

    private fun textWatcher() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isYearFilled = !binding.month.text.isNullOrBlank()
                val isMonthFilled = !binding.month.text.isNullOrBlank()
                val isDateFilled = !binding.number.text.isNullOrBlank()
                val isNameFilled = !binding.outlinedEditName.text.isNullOrBlank()
                isFilled = isDateFilled && isNameFilled && isMonthFilled && isYearFilled
            }
        }
        binding.year.addTextChangedListener(textWatcher)
        binding.month.addTextChangedListener(textWatcher)
        binding.number.addTextChangedListener(textWatcher)
        binding.outlinedEditName.addTextChangedListener(textWatcher)
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        binding.root.requestFocus()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = hashMapOf(
                    "day" to selectedDay,
                    "month" to selectedMonth + 1,
                    "year" to selectedYear
                )
                binding.number.setText(selectedDay.toString())
                binding.month.setText((selectedMonth+1).toString())
                binding.year.setText(selectedYear.toString())
            },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun saveDateToFirestore() {
        val userId = auth.currentUser?.uid
        hideKeyboard()
        if (userId != null) {
            val nameInput = binding.outlinedEditName.text.toString()
            selectedDate?.set("name", nameInput)

            val userCollection = firestore.collection("users").document(userId).collection("events")

            selectedDate?.let {
                userCollection.add(it)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Успешно сохранили!", Toast.LENGTH_SHORT).show()
                        binding.number.setText("")
                        binding.month.setText("")
                        binding.year.setText("")
                        binding.outlinedEditName.setText("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Не успешно! Ошибка.", Toast.LENGTH_SHORT).show()
                    }
            }
        } else{
            Toast.makeText(requireContext(), "Пожалуйста зарегистрируйтесь, затем сможете сохранять", Toast.LENGTH_SHORT).show()
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