package com.example.happybirthday.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.happybirthday.R
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.data.TOKEN
import com.example.happybirthday.databinding.FragmentRegistrationBinding
import com.example.happybirthday.showToast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private var auth = Firebase.auth

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth.addAuthStateListener(authStateListener)
        createPhoneAuthAndClickListener()
        listenerGoogleReg()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                sendFcmToken()
                TOKEN = task.result.token.toString()
            }
        }
    }

    private fun sendFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid
                val token = task.result
                lifecycleScope.launch {
                    if (uid != null) {
                        val result = ApiClient.apiService.postToken(uid,token)
                        if (!result.isSuccessful){
                            showToast("ошибка токена")
                        }
                    }
                }
            }
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

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            findNavController().navigate(R.id.action_registrationFragment_to_navigation_user)
        } else {
            showToast("Не вошли, возможно нет интернета.")
            showToast(result.resultCode.toString())
        }
    }
}