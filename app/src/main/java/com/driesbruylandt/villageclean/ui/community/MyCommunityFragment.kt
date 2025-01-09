package com.driesbruylandt.villageclean.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.driesbruylandt.villageclean.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.driesbruylandt.villageclean.Models.Community
import com.driesbruylandt.villageclean.databinding.FragmentMyCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        // Logic to remove selected members from the community
        Toast.makeText(requireContext(), "Members removed", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCommunity() {
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

    private fun populateUI() {
        binding.communityName.text = community.name
        binding.communityMunicipality.text = community.municipality

        val adapter = CommunityMembersAdapter(community.members) { selectedMember ->
            // Handle member selection logic (for remove functionality)
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

                community = documents.first().toObject(Community::class.java)
                populateUI()
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