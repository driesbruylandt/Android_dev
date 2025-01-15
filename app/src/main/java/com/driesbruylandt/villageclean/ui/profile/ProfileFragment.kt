package com.driesbruylandt.villageclean.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.driesbruylandt.villageclean.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var pointsTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var deleteButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pointsTextView = view.findViewById(R.id.points_text_view)
        emailTextView = view.findViewById(R.id.email_text_view)
        deleteButton = view.findViewById(R.id.delete_button)

        fetchUserData()

        deleteButton.setOnClickListener {
            deleteUserAccount()
        }
    }

    private fun fetchUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val points = document.getLong("points") ?: 0
                    val email = document.getString("email") ?: "No email"
                    pointsTextView.text = "Points: $points"
                    emailTextView.text = "Email: $email"
                } else {
                    pointsTextView.text = "Points: 0"
                    emailTextView.text = "Email: No email"
                }
            }
            .addOnFailureListener { e ->
                pointsTextView.text = "Error fetching points: ${e.message}"
                emailTextView.text = "Error fetching email: ${e.message}"
            }
    }

    private fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "User account deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete user account", Toast.LENGTH_SHORT).show()
            }
        }
    }
}