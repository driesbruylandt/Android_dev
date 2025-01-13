package com.driesbruylandt.villageclean.ui.community

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.driesbruylandt.villageclean.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.driesbruylandt.villageclean.Models.Community
import com.driesbruylandt.villageclean.databinding.FragmentMyCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyCommunityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentMyCommunityBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var community: Community
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchCommunityData()

        binding.removeMemberButton.setOnClickListener {
            removeSelectedMembers()
        }

        binding.deleteCommunityButton.setOnClickListener {
            deleteCommunity()
        }
    }

    private fun removeSelectedMembers() {
        val adapter = binding.communityMembersRecyclerView.adapter as CommunityMembersAdapter
        val selectedMembers = adapter.getSelectedMembers()
        Log.d("MyCommunityFragment", "Remove members: $selectedMembers, Community id: ${community.id}")

        if (selectedMembers.contains(community.admin)) {
            Toast.makeText(requireContext(), "Admin cannot remove themselves from the community", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMembers.isNotEmpty()) {
            val updatedMembers = community.members.toMutableList().apply {
                removeAll(selectedMembers)
            }

            firestore.collection("communities").document(community.id)
                .update("members", updatedMembers)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Members removed", Toast.LENGTH_SHORT).show()
                    community.members = updatedMembers
                    fetchCommunityData()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to remove members", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No members selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCommunity() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this community?")
            .setPositiveButton("Yes") { dialog, which ->
                firestore.collection("communities").document(community.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Community deleted", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete community", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun populateUI(memberData: List<Pair<String, String>>) {
        binding.communityName.text = community.name
        binding.communityMunicipality.text = community.municipality

        val adapter = CommunityMembersAdapter(memberData) { selectedMember ->
            Log.d("MyCommunityFragment", "Selected member: $selectedMember")
        }
        binding.communityMembersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.communityMembersRecyclerView.adapter = adapter

        if (community.admin == currentUserId) {
            binding.adminOptionsContainer.visibility = View.VISIBLE
        }
    }

    private fun fetchCommunityData() {
        firestore.collection("communities")
            .whereArrayContains("members", currentUserId!!)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No community found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = documents.first()
                community = document.toObject(Community::class.java).apply {
                    id = document.id  // Set the document ID
                }
                Log.d("MyCommunityFragment", "Community: ${community.name}, ID: ${community.id}, Members: ${community.members}")
                fetchMemberNames(community.members)
            }
            .addOnFailureListener { e ->
                Log.e("MyCommunityFragment", "Error fetching community data: ${e.message}", e)
                Toast.makeText(requireContext(), "Error fetching community data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMemberNames(memberIds: List<String>) {
        firestore.collection("users")
            .whereIn(FieldPath.documentId(), memberIds)  // Querying by document IDs
            .get()
            .addOnSuccessListener { documents ->
                val memberData = documents.map { it.id to (it.getString("email") ?: "Unknown") }
                populateUI(memberData)
            }
            .addOnFailureListener { e ->
                Log.e("MyCommunityFragment", "Error fetching member names: ${e.message}", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}