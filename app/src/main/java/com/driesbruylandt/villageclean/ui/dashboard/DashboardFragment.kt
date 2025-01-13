package com.driesbruylandt.villageclean.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.driesbruylandt.villageclean.R
import com.driesbruylandt.villageclean.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.userEmail.text = user.email
            checkUserCommunity(user.uid)
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        // Dropdown menu on user email
        binding.userEmail.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Set navigation actions
        binding.profileButton.setOnClickListener {
            // Navigate to Profile (update navGraph.xml)
            navigateToProfile()
        }

        binding.mapButton.setOnClickListener {
            // Navigate to Map (update navGraph.xml)
            navigateToMap()
        }

        binding.mapButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_mapFragment)
        }
        binding.communityButton.setOnClickListener{
            findNavController().navigate(R.id.action_dashboardFragment_to_myCommunityFragment)
        }
        binding.joinCommunityButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_joinCommunityFragment)
        }
    }

//    private fun checkUserCommunity(userId: String) {
//        val firestore = FirebaseFirestore.getInstance()
//
//        firestore.collection("users").document(userId).get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    val community = document.getString("community")
//                    if (community.isNullOrEmpty()) {
//                        binding.emptyStateContainer.visibility = View.VISIBLE
//                        binding.dashboardContainer.visibility = View.GONE
//                    } else {
//                        binding.emptyStateContainer.visibility = View.GONE
//                        binding.dashboardContainer.visibility = View.VISIBLE
//                    }
//                } else {
//                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//                Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun checkUserCommunity(userId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Query the "communities" collection where the user is a member
        firestore.collection("communities")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // If no community is found, show the empty state UI
                    binding.emptyStateContainer.visibility = View.VISIBLE
                    binding.dashboardContainer.visibility = View.GONE
                } else {
                    // If a community is found, populate the UI
                    binding.emptyStateContainer.visibility = View.GONE
                    binding.dashboardContainer.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Error fetching community data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.user_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        requireActivity().supportFragmentManager.popBackStack()
    }


    private fun navigateToProfile() {
        // TODO: Add navigation to Profile Fragment
    }

    private fun navigateToMap() {
        // TODO: Add navigation to Map Fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
