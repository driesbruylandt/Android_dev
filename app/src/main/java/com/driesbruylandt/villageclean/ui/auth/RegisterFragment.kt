package com.driesbruylandt.villageclean.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.driesbruylandt.villageclean.R
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val btnRegister: Button = view.findViewById(R.id.btnRegister)
        val etRegisterEmail: EditText = view.findViewById(R.id.etRegisterEmail)
        val etRegisterPassword: EditText = view.findViewById(R.id.etRegisterPassword)
        val tvLogin: TextView = view.findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email = etRegisterEmail.text.toString()
            val password = etRegisterPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the registered user's UUID
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        // Prepare user data to insert into Firestore
                        val userData = mapOf(
                            "email" to email,
                            "createdAt" to System.currentTimeMillis() // Optional field
                        )

                        // Insert the UUID into the Firestore users collection
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "User added to Firestore!", Toast.LENGTH_SHORT).show()
                                // Navigate to login screen
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
