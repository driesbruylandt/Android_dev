package com.driesbruylandt.villageclean.ui.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.driesbruylandt.villageclean.R
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StreetInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StreetInfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var streetName: String
    private lateinit var municipality: String

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            streetName = it.getString("streetName") ?: ""
            municipality = it.getString("municipality") ?: ""
            Log.d("StreetInfoFragment", "Street name: $streetName, Municipality: $municipality")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_street_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val streetInfoTextView: TextView = view.findViewById(R.id.street_info_text_view)
        val lastCleanedTextView: TextView = view.findViewById(R.id.last_cleaned_text_view)
        val updateLastCleanedButton: Button = view.findViewById(R.id.update_last_cleaned_button)

        fetchStreetInfo(streetInfoTextView, lastCleanedTextView)

        updateLastCleanedButton.setOnClickListener {
            createCleaningRequest()
        }
    }

    private fun fetchStreetInfo(streetInfoTextView: TextView, lastCleanedTextView: TextView) {
        firestore.collection("municipalities").document(municipality)
            .collection("streets").document(streetName).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val streetInfo = document.getString("name") ?: "Street information not found"
                    val lastCleanedTimestamp = document.getTimestamp("lastCleaned")
                    val lastCleaned = lastCleanedTimestamp?.toDate()?.let {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                    } ?: "Not available"
                    streetInfoTextView.text = streetInfo
                    lastCleanedTextView.text = lastCleaned
                } else {
                    streetInfoTextView.text = "Street information not found"
                    lastCleanedTextView.text = "Not available"
                }
            }
            .addOnFailureListener { e ->
                streetInfoTextView.text = "Error fetching street information: ${e.message}"
            }
    }

    private fun createCleaningRequest() {
        val currentDate = Timestamp.now()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("communities")
            .whereArrayContains("members", FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "No community found", Toast.LENGTH_SHORT).show()
                } else {
                    val community = documents.first()
                    val admin = community.getString("admin") ?: ""
                    val cleaningRequest = hashMapOf(
                        "streetName" to streetName,
                        "municipality" to municipality,
                        "requestedAt" to currentDate,
                        "status" to "pending",
                        "adminId" to admin,
                        "userId" to currentUserId
                    )

                    firestore.collection("cleaningRequests")
                        .add(cleaningRequest)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Cleaning request created", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to create cleaning request: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch community: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(streetName: String, municipality: String) =
            StreetInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("streetName", streetName)
                    putString("municipality", municipality)
                }
            }
    }
}