package com.driesbruylandt.villageclean.ui.admin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.Models.CleaningRequest
import com.driesbruylandt.villageclean.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var cleaningRequestsRecyclerView: RecyclerView
    private lateinit var cleaningRequestsAdapter: CleaningRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cleaningRequestsRecyclerView = view.findViewById(R.id.cleaning_requests_recycler_view)
        cleaningRequestsRecyclerView.layoutManager = LinearLayoutManager(context)

        fetchCleaningRequests()
    }

    private fun handleCleaningRequestAction(requestId: String, action: String) {
        val status = if (action == "confirm") "confirmed" else "denied"

        // Run updates in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the cleaning request status
                firestore.collection("cleaningRequests").document(requestId)
                    .update("status", status).await()

                if (action == "confirm") {
                    // Fetch the cleaning request details
                    val requestDoc = firestore.collection("cleaningRequests").document(requestId).get().await()
                    val userId = requestDoc.getString("userId") ?: return@launch
                    val streetName = requestDoc.getString("streetName") ?: return@launch
                    val municipality = requestDoc.getString("municipality") ?: return@launch
                    Log.d("AdminFragment", "Cleaning request confirmed for user: $userId")
                    // Increment user points
                    firestore.collection("users").document(userId)
                        .update("points", FieldValue.increment(10)).await()

                    // Update the lastCleaned field for the street
                    firestore.collection("municipalities").document(municipality)
                        .collection("streets").document(streetName)
                        .update("lastCleaned", Timestamp.now()).await()
                }

                // Refresh the list on success
                withContext(Dispatchers.Main) {
                    fetchCleaningRequests()
                }
            } catch (e: Exception) {
                // Log and handle errors
                Log.e("AdminFragment", "Error handling cleaning request: ${e.message}", e)
            }
        }
    }


    private fun fetchCleaningRequests() {
        val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("cleaningRequests")
            .whereEqualTo("adminId", adminId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                val cleaningRequests = documents.map { document ->
                    val requestId = document.id
                    CleaningRequest(
                        requestId,
                        document.getString("streetName") ?: "",
                        document.getString("municipality") ?: "",
                        document.getTimestamp("requestedAt")?.toDate() ?: Date(),
                        document.getString("userId") ?: "" // Fetch userId
                    )
                }
                cleaningRequestsAdapter = CleaningRequestsAdapter(cleaningRequests) { requestId, action ->
                    handleCleaningRequestAction(requestId, action)
                }
                cleaningRequestsRecyclerView.adapter = cleaningRequestsAdapter
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}