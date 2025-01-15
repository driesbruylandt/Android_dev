package com.driesbruylandt.villageclean.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.R

class CommunityMembersAdapter(
    private val members: List<Triple<String, String, Long>>,
    private val onMemberSelected: (String) -> Unit
) : RecyclerView.Adapter<CommunityMembersAdapter.MemberViewHolder>() {

    private val selectedMembers = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val (id, email, points) = members[position]
        holder.bind(id, email, points)
    }

    override fun getItemCount() = members.size

    fun getSelectedMembers(): List<String> {
        return selectedMembers.toList()
    }

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberName: TextView = itemView.findViewById(R.id.memberName)
        private val memberPoints: TextView = itemView.findViewById(R.id.memberPoints)

        fun bind(id: String, email: String, points: Long) {
            memberName.text = email
            memberPoints.text = "Points: $points"
            itemView.setOnClickListener {
                onMemberSelected(id)
            }
        }
    }
}
