package com.example.happybirthday.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.happybirthday.POSITION
import com.example.happybirthday.TIMEZONE
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.databinding.FragmentEventBinding
import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Calendar

class EventFragment : Fragment() {

    private val viewModel: MainActivityViewModel by activityViewModels()
    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var event: MyEvent? = null
    private var isFilled = false
    private var time: Int = 9

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        buttonListener()
        textWatcher()

        val position = arguments?.getInt(POSITION)
        if(position != null) {
            val events = viewModel.data.value
            event = events!![position]
            showDate(event?.day?.toInt(), event?.month?.toInt(), event?.year?.toInt())
            binding.outlinedEditLastName.setText(event?.lastName)
            binding.outlinedEditPatronymic.setText(event?.patronymic)
            if(event?.telephone?.toInt() != 0) {
                binding.outlinedEditPhone.setText(event?.telephone.toString())
            }
            event?.hour?.let {
                time = convertTimeToUI(it, event!!.timeZone)
                binding.time.text = "с $time до ${time.plus(1)} ч."
            }
            binding.outlinedEditName.setText(event?.firstName)
        }
    }

    private fun buttonListener() {
        binding.changeDate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.changeTime.setOnClickListener {
            changeTime()
        }
        binding.save.setOnClickListener {
            if (isFilled) update() else showToast("Нужно заполнить имя и дату!")
        }
    }

    private fun changeTime() {
        val numberPicker = NumberPicker(context)
        numberPicker.minValue = 0
        numberPicker.maxValue = 23
        numberPicker.value = time
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Выберите час")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedHour = numberPicker.value
                time = selectedHour
                showTime()
            }
            .setNegativeButton("Отмена", null)
            .create()
        alertDialog.show()
    }

    private fun update() {
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
                    event!!.hour = time.toLong()
                    event!!.timeZone = TIMEZONE
                    val status = ApiClient.apiService.patchEvent(event!!)
                    if(status.isSuccessful) {
                        showToast("Успешно изменили!")
                        binding.root.clearFocus()
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
                showDate(selectedDay, selectedMonth, selectedYear)
                event?.day = selectedDay.toLong()
                event?.month = selectedMonth.toLong()
                event?.year = selectedYear.toLong()
            },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showDate(
        selectedDay: Int?,
        selectedMonth: Int?,
        selectedYear: Int?
    ) {
        binding.number.setText(selectedDay.toString())
        if (selectedMonth != null) {
            binding.month.setText((selectedMonth + 1).toString())
        }
        binding.year.setText(selectedYear.toString())
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
        binding.time.text = "с $time до ${time.plus(1)} ч."
    }

//    private fun convertTime(): Long {
//        return time/* - (TIMEZONE - TIMEZONESERVER)*/
//    }
    private fun convertTimeToUI(time: Long, timeZoneServer: Int): Int {
        return time.toInt() - (timeZoneServer - TIMEZONE)
    }
}