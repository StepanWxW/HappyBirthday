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
import com.example.happybirthday.TIMEZONE
import com.example.happybirthday.TIMEZONESERVER
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.databinding.FragmentAddBinding
import com.example.happybirthday.model.MyEvent
import com.example.happybirthday.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var event: MyEvent? = null
    private var isFilled = false
    private var time: Int = 9
    private val regPlease = "Нужно сначала зарегистрироваться!"
    private val calendar = Calendar.getInstance()

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
        newEvent()
        showTime()
        buttonListener()
        textWatcher()

//        calendar.add(Calendar.HOUR_OF_DAY, time)
    }

    private fun newEvent() {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            event = MyEvent(userUid)
        }
    }

    private fun buttonListener() {
        binding.changeDate.setOnClickListener {
            if(event != null) showDatePickerDialog() else showToast(regPlease)
        }
        binding.changeTime.setOnClickListener {
            if(event != null) changeTime() else showToast(regPlease)
        }
        binding.save.setOnClickListener {
            if(event == null) {
                showToast(regPlease)
                return@setOnClickListener
            }
            if (isFilled) save() else showToast("Нужно заполнить имя и дату!")
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
                time = selectedHour
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
                    showToast(time.toString())
                    Log.d("Event", calendar.toString())
                    calendar.add(Calendar.HOUR_OF_DAY, time)
                    Log.d("Event", calendar.toString())
                    val petersburgTimeZone = TimeZone.getTimeZone("Europe/Moscow") // Часовой пояс Питера
                    calendar.timeZone = petersburgTimeZone

                    event!!.year = calendar.get(Calendar.YEAR).toLong()
                    event!!.month  = calendar.get(Calendar.MONTH).toLong()
                    event!!.day = calendar.get(Calendar.DAY_OF_MONTH).toLong()
                    event!!.hour = calendar.get(Calendar.HOUR_OF_DAY).toLong()

                    Log.d("Event", calendar.toString())
//                    event!!.hour = convertTime()
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
            } else {
                showToast(regPlease)
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
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        binding.root.requestFocus()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                Log.d("MyTag", calendar.timeZone.toString())
                binding.number.setText(selectedDay.toString())
                binding.month.setText((selectedMonth+1).toString())
                binding.year.setText(selectedYear.toString())
                calendar.set(selectedYear, selectedMonth, selectedDay)
//                calendar.set(Calendar.HOUR_OF_DAY, time)
//                calendar.add(Calendar.DAY_OF_MONTH, selectedDay)
//                calendar.add(Calendar.MONTH, selectedMonth)
//                calendar.add(Calendar.YEAR, selectedYear)
//                event?.day = selectedDay.toLong()
//                event?.month = selectedMonth.toLong()
//                event?.year = selectedYear.toLong()
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
        showTime()
        newEvent()
        calendar.clear()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.timeZone = TimeZone.getDefault()
//        calendar.add(Calendar.HOUR_OF_DAY, time)
        Log.d("MyTag", calendar.timeZone.toString())
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
//
//        private fun getTime(): Long {
//        var total = time - (TIMEZONE - TIMEZONESERVER)
//        if (total > 24) {
//            total -= 24
//        }
//        if (total < 0) {
//            total += 24
//        }
//        return total
//    }

//    private fun convertTime(): Long {
//        return time - (TIMEZONE - TIMEZONESERVER)
//    }
}