package com.example.happybirthday.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.fragment.findNavController
import com.example.happybirthday.R
import com.example.happybirthday.databinding.FragmentUserBinding
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.TimeZone

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        createButtonExitAndClickListener()
        createPhoneAuthAndClickListener()
        listenerGoogleReg()
        checkUser()

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userDoc = firestore.collection("users").document(it.uid)

            val listener = userDoc.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Обработка ошибки
                    return@addSnapshotListener
                }
                if (isAdded) {
                    if (snapshot != null && snapshot.exists()) {
                        val time = snapshot.getLong("time")
                        if (time == null) {
                            binding.textStandart.text =
                                requireContext().getString(R.string.push_standart)
                        } else {
//                        try {
                            val timeNow = getTimeInBase(time.toInt())
                            val pushTime = activity?.getString(R.string.push_time)

                            val suffix = getHourSuffix(timeNow)
                            val message = "$pushTime\n$timeNow $suffix"
                            binding.textStandart.text = message
//                        } catch (e :Exception){
//                            println(e)
//                        }
                        }
                    }
                }
            }
            // Чтобы прекратить прослушивание, используйте:
            // listener.remove()
        }

        binding.timeButton.setOnClickListener {

            val numberPicker = NumberPicker(context)
            numberPicker.minValue = 0
            numberPicker.maxValue = 23
            val user = FirebaseAuth.getInstance().currentUser
            val userDoc = user?.let { it1 -> firestore.collection("users").document(it1.uid) }

            val alertDialog = AlertDialog.Builder(context)
                .setTitle("Выберите час")
                .setView(numberPicker)
                .setPositiveButton("OK") { _, _ ->
                    val selectedHour = numberPicker.value

                    if (userDoc != null) {
                        userDoc.update("time", getTime(selectedHour))
                        Toast.makeText(requireContext(), "Успешно сохранили", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Отмена", null)
                .create()

            alertDialog.show()
        }
    }

    private fun getTime(selectedHour: Int): Int {
        val currentTimeZoneOffset = getCurrentTimeZoneOffset()
        val count = currentTimeZoneOffset - 3
        var total = selectedHour - count
        if (total > 24) {
            total -= 24
        }
        if (total < 0) {
            total += 24
        }
        return total
    }

    private fun getTimeInBase(selectedHour: Int): Int {
        val currentTimeZoneOffset = getCurrentTimeZoneOffset()
        val count = currentTimeZoneOffset - 3
        var total = selectedHour + count
        if (total > 24) {
            total -= 24
        }
        if (total < 0) {
            total += 24
        }
        return total
    }

    private fun createButtonExitAndClickListener() {
        binding.exitAccountButton.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите выйти из своего аккаунта?")
                .setPositiveButton("Да") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.let {
                            val userDoc =
                                FirebaseFirestore.getInstance().collection("users").document(it.uid)
                            userDoc.update(hashMapOf("token" to "") as Map<String, Any>)

                        }
                        AuthUI.getInstance()
                            .signOut(requireContext())
                            .addOnCompleteListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Вы успешно вышли из аккаунта",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
//                            findNavController().navigate(R.id.action_navigation_user_to_AuthFragment)
                            }
                        binding.userInfo.visibility = View.GONE
                        binding.registration.visibility = View.VISIBLE
                    }
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    // В случае отмены, закрываем диалог
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userDoc = firestore.collection("users").document(it.uid)

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        userDoc.update(hashMapOf("token" to token) as Map<String, Any>)
                        val auth = Firebase.auth
                        val currentUser = auth.currentUser
                        if(currentUser?.email != null) {
                            userDoc.update(
                                hashMapOf("name" to currentUser.email,
                                    "token" to token) as Map<String, Any>
                            )
                        } else {
                            userDoc.update(
                                hashMapOf("name" to currentUser?.phoneNumber,
                                    "token" to token) as Map<String, Any>
                            )
                        }
                    } else {
                        // Обработка ошибки
                    }
                }
            }
            checkUser()
        } else {
            Toast.makeText(activity, "Не вошли, возможно нет интернета.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.userInfo.visibility = View.VISIBLE
            binding.registration.visibility = View.GONE
            if (currentUser.email != null) {
                binding.nik.text = currentUser.email
            } else {
                binding.nik.text = currentUser.phoneNumber
            }
        } else {
            binding.userInfo.visibility = View.GONE
            binding.registration.visibility = View.VISIBLE
        }
    }

    private fun createPhoneAuthAndClickListener() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )
        binding.buttonReg.setOnClickListener {
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    private fun listenerGoogleReg() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        binding.buttonGoogleReg.setOnClickListener {
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }
    fun getCurrentTimeZoneOffset(): Int {
        // Получаем смещение в миллисекундах
        val rawOffset = TimeZone.getDefault().rawOffset

        // Переводим миллисекунды в часы
        return rawOffset / (60 * 60 * 1000)
    }

    fun getHourSuffix(hour: Int): String {
        return when {
            hour % 10 == 1 && hour % 100 != 11 -> "час"
            hour % 10 in 2..4 && hour % 100 !in 12..14 -> "часа"
            else -> "часов"
        }
    }
}