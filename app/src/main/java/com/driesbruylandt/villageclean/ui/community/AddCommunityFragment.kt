package com.driesbruylandt.villageclean.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.driesbruylandt.villageclean.R
import android.widget.Toast
import com.driesbruylandt.villageclean.databinding.FragmentAddCommunityBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.driesbruylandt.villageclean.Models.Community

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddCommunityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentAddCommunityBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()

    // Hardcoded list of Flemish municipalities
    private val flemishMunicipalities = listOf(
        "Antwerp", "Ghent", "Bruges", "Leuven", "Hasselt", "Mechelen", "Kortrijk", "Sint-Niklaas", "Genk", "Turnhout"
    )

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
        _binding = FragmentAddCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up AutoCompleteTextView for municipality
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            flemishMunicipalities
        )
        binding.selectMunicipalityInput.setAdapter(adapter)

        binding.createCommunityButton.setOnClickListener {
            createCommunity()
        }
    }

    private fun createCommunity() {
        val municipality = binding.selectMunicipalityInput.text.toString()
        val communityName = binding.communityNameInput.text.toString()
        val maxMembers = binding.maxMembersInput.text.toString().toIntOrNull()

        // Validate inputs
        if (municipality.isEmpty() || communityName.isEmpty() || maxMembers == null || maxMembers <= 0) {
            // Handle validation error (e.g., show a Toast)
            return
        }

        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create a new community object
        val community = Community(
            name = communityName,
            municipality = municipality,
            maxMembers = maxMembers,
            members = listOf(userId),  // Current user is the first member (admin)
            admin = userId
        )

        firestore.collection("communities")
            .add(community)
            .addOnSuccessListener {
                // Navigate back to JoinCommunityFragment or show success message
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                // Handle error (e.g., show a Toast)
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
         * @return A new instance of fragment AddCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}