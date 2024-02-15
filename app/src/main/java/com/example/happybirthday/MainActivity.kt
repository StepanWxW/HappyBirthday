package com.example.happybirthday


import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.happybirthday.data.ApiClient
import com.example.happybirthday.data.TOKEN
import com.example.happybirthday.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }
    private var isBackPressed = false
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        checkUser()
        auth?.addAuthStateListener(authStateListener)
        createPhoneAuthAndClickListener()
        listenerGoogleReg()

        TIMEZONE = TimeZone.getDefault().rawOffset / (1000 * 60 * 60)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
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
                val uid = auth?.currentUser?.uid
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

    fun checkUser() {
        val currentUser = auth?.currentUser
        if (currentUser == null) {
            binding.itemAuth.itemAuth.visibility = View.VISIBLE
            binding.navView.visibility = View.GONE
        } else {
            binding.itemAuth.itemAuth.visibility = View.GONE
            binding.navView.visibility = View.VISIBLE
        }
    }

    private fun createPhoneAuthAndClickListener() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )
        binding.itemAuth.buttonReg.setOnClickListener {
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
        binding.itemAuth.buttonGoogleReg.setOnClickListener {
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            checkUser()
        } else {
            showToast("Не вошли, возможно нет интернета.")
            showToast(result.resultCode.toString())
        }
    }

    override fun onBackPressed() {
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId == R.id.navigation_home)  {
            if (!isBackPressed) {
                showToast("Нажмите еще раз, чтобы выйти")
                val timer = object : CountDownTimer(15000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        isBackPressed = false
                    }
                }
                timer.start()
                isBackPressed = true
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}