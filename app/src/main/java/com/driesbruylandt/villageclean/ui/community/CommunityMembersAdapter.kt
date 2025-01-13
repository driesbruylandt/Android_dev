package com.driesbruylandt.villageclean.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.driesbruylandt.villageclean.R

class CommunityMembersAdapter(
    private val members: List<Pair<String, String>>,
    private val onMemberSelected: (String) -> Unit
) : RecyclerView.Adapter<CommunityMembersAdapter.MemberViewHolder>() {

    private val selectedMembers = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val (id, email) = members[position]
        holder.bind(id, email, selectedMembers.contains(id))
    }

    override fun getItemCount() = members.size

    fun getSelectedMembers(): List<String> {
        return selectedMembers.toList()
    }

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberName: TextView = itemView.findViewById(R.id.memberName)
        private val memberCheckBox: CheckBox = itemView.findViewById(R.id.memberCheckBox)

        fun bind(id: String, email: String, isSelected: Boolean) {
            memberName.text = email
            memberCheckBox.isChecked = isSelected
            memberCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMembers.add(id)
                } else {
                    selectedMembers.remove(id)
                }
                onMemberSelected(id)
            }
        }
    }
}
