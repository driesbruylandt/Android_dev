package com.driesbruylandt.villageclean.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.Models.Community
import com.driesbruylandt.villageclean.R

class CommunitiesAdapter(private val onCommunityClick: (Community) -> Unit) :
    RecyclerView.Adapter<CommunitiesAdapter.CommunityViewHolder>() {

    private val communities = mutableListOf<Community>()

    fun submitList(newCommunities: List<Community>) {
        communities.clear()
        communities.addAll(newCommunities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val community = communities[position]
        holder.bind(community, onCommunityClick)
    }

    override fun getItemCount() = communities.size

    class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val communityName: TextView = itemView.findViewById(R.id.communityName)

        fun bind(community: Community, onClick: (Community) -> Unit) {
            communityName.text = community.name
            itemView.setOnClickListener { onClick(community) }
        }
    }
}
