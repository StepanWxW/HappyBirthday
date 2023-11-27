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
            viewLifecycleOwner.lifecycleScope.launch {
                val userDoc = firestore.collection("users").document(it.uid).get().await()
                val time = userDoc.getLong("time")
                if (time == null){
                    binding.textStandart.text = requireContext().getString(R.string.push_standart)
                } else {
                    val pushTime = requireContext().getString(R.string.push_time)
                    val message = pushTime + time.toString()
                    binding.textStandart.text = message
                }
            }
        }

        binding.timeButton.setOnClickListener {

            val numberPicker = NumberPicker(context)
            numberPicker.minValue = 0
            numberPicker.maxValue = 23

            val alertDialog = AlertDialog.Builder(context)
                .setTitle("Выберите час")
                .setView(numberPicker)
                .setPositiveButton("OK") { _, _ ->
                    val selectedHour = numberPicker.value

                    val currentTimeZoneOffset = getCurrentTimeZoneOffset()
                    Toast.makeText(requireContext(), currentTimeZoneOffset.toString(), Toast.LENGTH_LONG).show()
                    // Здесь вы получаете выбранный час (selectedHour)
                    // Можете использовать его в дальнейшем
                }
                .setNegativeButton("Отмена", null)
                .create()

            alertDialog.show()
        }
    }

    private fun createButtonExitAndClickListener() {
        binding.exitAccountButton.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите Выйти из своего аккаунта?")
                .setPositiveButton("Да") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.let {
                            val userDoc =
                                FirebaseFirestore.getInstance().collection("users").document(it.uid)
                            userDoc.set(hashMapOf("token" to ""))

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
                        userDoc.set(hashMapOf("token" to token))
                        val auth = Firebase.auth
                        val currentUser = auth.currentUser
                        if(currentUser?.email != null) {
                            userDoc.set(hashMapOf("name" to currentUser.email,
                                "token" to token))
                        } else {
                            userDoc.set(hashMapOf("name" to currentUser?.phoneNumber,
                                "token" to token))
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
}