package com.example.happybirthday

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.happybirthday.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private var isBackPressed = false
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth = Firebase.auth
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TIMEZONE = TimeZone.getDefault().rawOffset / (1000 * 60 * 60)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
        navViewListener()
    }

    private fun navViewListener() {
        binding.navView.setOnItemSelectedListener { item ->
            navController.popBackStack(R.id.navigation_home, false)
            when (item.itemId) {
                R.id.navigation_home -> { navController.navigate(R.id.navigation_home) }
                R.id.navigation_dashboard -> { navController.navigate(R.id.navigation_dashboard) }
                R.id.navigation_faq -> { navController.navigate(R.id.navigation_faq) }
                R.id.navigation_user -> {
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        navController.navigate(R.id.registrationFragment)
                    } else {
                        navController.navigate(R.id.navigation_user)
                    }
                }
            }
            true
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