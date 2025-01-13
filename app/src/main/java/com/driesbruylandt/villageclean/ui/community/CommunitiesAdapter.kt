package com.driesbruylandt.villageclean.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.Models.Community
import com.driesbruylandt.villageclean.R

class CommunitiesAdapter(private val onCommunitySelected: (Community) -> Unit) :
    RecyclerView.Adapter<CommunitiesAdapter.CommunityViewHolder>() {

    private var communities: List<Community> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.bind(communities[position])
    }

    override fun getItemCount(): Int = communities.size

    fun submitList(communities: List<Community>) {
        this.communities = communities
        notifyDataSetChanged()
    }

    inner class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val communityName: TextView = itemView.findViewById(R.id.communityName)

        fun bind(community: Community) {
            val memberCount = community.members.size
            communityName.text = "${community.name} ($memberCount/${community.maxMembers})"
            itemView.setOnClickListener {
                onCommunitySelected(community)
            }
        }
    }
}
