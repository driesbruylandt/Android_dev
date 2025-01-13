package com.driesbruylandt.villageclean.ui.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.driesbruylandt.villageclean.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var map: GoogleMap
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var municipality: String

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
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        getUserCommunity()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnPolygonClickListener { polygon ->
            val streetName = polygon.tag as String
            val bundle = Bundle().apply {
                putString("streetName", streetName)
                putString("municipality", municipality)
            }
            findNavController().navigate(R.id.action_mapFragment_to_streetInfoFragment, bundle)
        }
    }

    private fun getUserCommunity() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            firestore.collection("communities")
                .whereArrayContains("members", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(context, "No community found", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in documents) {
                            Log.d("MapFragment", "${document.id} => ${document.data}")
                            municipality = document.getString("municipality") ?: ""
                            fetchMunicipalityCoordinates(municipality)
                        }
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
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMunicipalityCoordinates(municipality: String) {
        firestore.collection("municipalities").document(municipality).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val geoPoint = document.getGeoPoint("coordinates")
                    if (geoPoint != null) {
                        val municipalityLatLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(municipalityLatLng, 18f))
                        loadPolygons(municipality)
                    }
                } else {
                    Toast.makeText(context, "Municipality data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Error fetching municipality data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadPolygons(municipality: String) {
        firestore.collection("municipalities").document(municipality).collection("streets")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val polygonPoints = document.get("polygon") as List<GeoPoint>
                    val latLngList = polygonPoints.map { LatLng(it.latitude, it.longitude) }

                    val polygonOptions = PolygonOptions()
                        .addAll(latLngList)
                        .clickable(true)
                    map.addPolygon(polygonOptions).tag = document.getString("name")
                }
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
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}