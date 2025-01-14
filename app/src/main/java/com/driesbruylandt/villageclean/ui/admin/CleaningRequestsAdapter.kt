package com.driesbruylandt.villageclean.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.Models.CleaningRequest
import com.driesbruylandt.villageclean.R
import java.text.SimpleDateFormat
import java.util.Locale

class CleaningRequestsAdapter(
    private val cleaningRequests: List<CleaningRequest>,
    private val onActionClick: (String, String) -> Unit
) : RecyclerView.Adapter<CleaningRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val streetNameTextView: TextView = view.findViewById(R.id.street_name_text_view)
        val municipalityTextView: TextView = view.findViewById(R.id.municipality_text_view)
        val requestedAtTextView: TextView = view.findViewById(R.id.requested_at_text_view)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)
        val denyButton: Button = view.findViewById(R.id.deny_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cleaning_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cleaningRequest = cleaningRequests[position]
        holder.streetNameTextView.text = cleaningRequest.streetName
        holder.municipalityTextView.text = cleaningRequest.municipality
        holder.requestedAtTextView.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cleaningRequest.requestedAt)

        holder.confirmButton.setOnClickListener {
            onActionClick(cleaningRequest.id, "confirm")
        }

        holder.denyButton.setOnClickListener {
            onActionClick(cleaningRequest.id, "deny")
        }
    }

    override fun getItemCount(): Int = cleaningRequests.size
}