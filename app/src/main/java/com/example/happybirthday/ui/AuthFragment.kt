package com.example.happybirthday.ui

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.happybirthday.R
import com.example.happybirthday.databinding.FragmentAuthBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class AuthFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private var _binding: FragmentAuthBinding? = null
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createPhoneAuthAndClickListener()
        listenerGoogleReg()
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
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {


            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userDoc = firestore.collection("users").document(it.uid)

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
//                        val uid = "uid_пользователя" // Получите UID пользователя из вашего приложения
                        // Отправьте token и uid на сервер
                        userDoc.set(hashMapOf("token" to token))
                        val auth = Firebase.auth
                        val currentUser = auth.currentUser
                        if(currentUser?.email != null) {
                            userDoc.set(hashMapOf("name" to currentUser.email))
                        } else {
                            userDoc.set(hashMapOf("name" to currentUser?.phoneNumber))
                        }
                    } else {
                        // Обработка ошибки
                    }
                }
            }




            findNavController().navigate(R.id.action_AuthFragment_to_navigation_user)
        } else {
            Toast.makeText(activity, "Не вошли, возможно нет интернета.", Toast.LENGTH_SHORT)
                .show()
        }
    }

}