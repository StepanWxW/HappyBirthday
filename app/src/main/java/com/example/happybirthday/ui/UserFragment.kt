package com.example.happybirthday.ui

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.happybirthday.MainActivity
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.databinding.FragmentUserBinding
import com.example.happybirthday.showToast
import kotlinx.coroutines.launch
import java.util.TimeZone

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        createButtonExitAndClickListener()
        checkUser()
    }

//    private fun getTime(selectedHour: Int): Int {
//        val currentTimeZoneOffset = getCurrentTimeZoneOffset()
//        val count = currentTimeZoneOffset - 3
//        var total = selectedHour - count
//        if (total > 24) {
//            total -= 24
//        }
//        if (total < 0) {
//            total += 24
//        }
//        return total
//    }
//
//    private fun getTimeInBase(selectedHour: Int): Int {
//        val currentTimeZoneOffset = getCurrentTimeZoneOffset()
//        val count = currentTimeZoneOffset - 3
//        var total = selectedHour + count
//        if (total > 24) {
//            total -= 24
//        }
//        if (total < 0) {
//            total += 24
//        }
//        return total
//    }

    private fun createButtonExitAndClickListener() {
        binding.exitAccountButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите выйти из своего аккаунта?")
                .setPositiveButton("Да") { dialog, _ ->
                    if(isInternetAvailable()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            auth.uid?.let { it1 ->
                                val result = ApiClient.apiService.deleteToken(it1)
                                if (result.isSuccessful) {
                                    outFromFirebase()
                                }
                            }
                        }
                    } else {
                        showToast("ОШИБКА! Нет соединения с интернетом!")
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private fun outFromFirebase() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                showToast("Вы успешно вышли из аккаунта")
                (activity as MainActivity).checkUser()
            }
    }

    override fun onResume() {
        super.onResume()
        checkUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.phoneNumber != null && currentUser.phoneNumber != "") {
                binding.nik.text = currentUser.phoneNumber
            } else {
                binding.nik.text = currentUser.email
            }
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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}