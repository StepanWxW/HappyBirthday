package com.example.happybirthday.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.databinding.FragmentAddBinding
import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var event: MyEvent? = null
    private var isFilled = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        val userUid = auth.currentUser?.uid
        if(userUid != null) {
            event = MyEvent(userUid)
            event?.hour = 9
        }
        showTime()
        buttonListener()
        textWatcher()
    }


    private fun buttonListener() {
        binding.changeDate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.changeTime.setOnClickListener {
            changeTime()
        }
        binding.save.setOnClickListener {
            if (isFilled) {
                save()
            } else {
                showToast("Нужно заполнить имя и дату!")
            }
        }
    }

    private fun changeTime() {
        val numberPicker = NumberPicker(context)
        numberPicker.minValue = 0
        numberPicker.maxValue = 23
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Выберите час")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedHour = numberPicker.value
                event?.hour = selectedHour.toLong()
                showTime()
            }
            .setNegativeButton("Отмена", null)
            .create()
        alertDialog.show()
    }

    private fun save() {
        lifecycleScope.launch {
            if(event != null) {
                try {
                    hideKeyboard()
                    event!!.firstName = binding.outlinedEditName.text.toString()
                    event!!.lastName = binding.outlinedEditLastName.text.toString()
                    event!!.patronymic = binding.outlinedEditPatronymic.text.toString()
                    val phoneNumberString = binding.outlinedEditPhone.text.toString()
                    if (phoneNumberString.isNotBlank() && phoneNumberString.all { it.isDigit() }) {
                        event!!.telephone = phoneNumberString.toLong()
                    }
                    val status = ApiClient.apiService.postEvent(event!!)
                    if(status.isSuccessful) {
                        showToast("Успешно сохранили!")
                        clearColumns()
                    } else {
                        showToast("Ошибка сохранения")
                    }
                } catch (e: Exception) {
                    Log.d("MyTag", e.toString())
                    showToast("Ошибка сохранения данных")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clearColumns()
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
                binding.number.setText(selectedDay.toString())
                binding.month.setText((selectedMonth+1).toString())
                binding.year.setText(selectedYear.toString())
                event?.day = selectedDay.toLong()
                event?.month = selectedMonth.toLong()
                event?.year = selectedYear.toLong()
            },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun clearColumns() {
        binding.number.setText("")
        binding.month.setText("")
        binding.year.setText("")
        binding.outlinedEditLastName.setText("")
        binding.outlinedEditPatronymic.setText("")
        binding.outlinedEditPhone.setText("")
        binding.outlinedEditName.setText("")
        event?.hour = 9
        showTime()
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
    @SuppressLint("SetTextI18n")
    private fun showTime() {
        binding.time.text = "с ${event?.hour} до ${event?.hour?.plus(1)} ч."
    }

}