package com.driesbruylandt.villageclean.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.R

class CommunityMembersAdapter(
    private val members: List<String>,
    private val onMemberSelected: (String) -> Unit
) : RecyclerView.Adapter<CommunityMembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, onMemberSelected)
    }

    override fun getItemCount() = members.size

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberName: TextView = itemView.findViewById(R.id.memberName)
        private val memberCheckBox: CheckBox = itemView.findViewById(R.id.memberCheckBox)

        fun bind(member: String, onMemberSelected: (String) -> Unit) {
            memberName.text = member
            memberCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onMemberSelected(member)
                }
            }
        }
    }
}
