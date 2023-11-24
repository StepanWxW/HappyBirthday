package com.example.happybirthday.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.fragment.findNavController
import com.example.happybirthday.R
import com.example.happybirthday.databinding.FragmentUserBinding
import com.google.firebase.firestore.FirebaseFirestore

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
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.nik.text = currentUser.email
        } else {
            findNavController().navigate(R.id.action_navigation_user_to_AuthFragment)
        }

//        val updateBtn: BottomNavigationItemView? = activity?.findViewById(R.id.navigation_home)
//        updateBtn?.setOnClickListener {
//            findNavController().navigate(R.id.navigation_home)
//        }
    }

    private fun createButtonExitAndClickListener() {
        binding.exitAccountButton.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userDoc = FirebaseFirestore.getInstance().collection("users").document(it.uid)
                    userDoc.set(hashMapOf("token" to ""))
                }
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    Toast.makeText(requireContext(), "Вы успешно вышли из аккаунта", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_navigation_user_to_AuthFragment)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}