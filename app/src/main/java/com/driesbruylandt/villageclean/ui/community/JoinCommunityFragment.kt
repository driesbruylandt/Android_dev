package com.driesbruylandt.villageclean.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.driesbruylandt.villageclean.R
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.driesbruylandt.villageclean.databinding.FragmentJoinCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController
import com.driesbruylandt.villageclean.Models.Community

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JoinCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JoinCommunityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentJoinCommunityBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: CommunitiesAdapter

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
        _binding = FragmentJoinCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()

        binding.addCommunityButton.setOnClickListener {
            // Navigate to Add Community page
            findNavController().navigate(R.id.action_joinCommunityFragment_to_addCommunityFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = CommunitiesAdapter { community ->
            joinCommunity(community)
        }
        binding.communitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.communitiesRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            flemishMunicipalities // Use the list of Flemish municipalities here
        )
        binding.searchMunicipalityInput.setAdapter(adapter)

        binding.searchMunicipalityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCommunities(s.toString())
            }
        })
    }

    private fun searchCommunities(municipality: String) {
        firestore.collection("communities")
            .whereEqualTo("municipality", municipality)
            .get()
            .addOnSuccessListener { documents ->
                val communities = documents.map { it.toObject(Community::class.java) }
                adapter.submitList(communities)
            }
    }

    private fun joinCommunity(community: Community) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update("community", community.name)
                .addOnSuccessListener {
                    findNavController().popBackStack() // Return to dashboard
                }
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
         * @return A new instance of fragment JoinCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}