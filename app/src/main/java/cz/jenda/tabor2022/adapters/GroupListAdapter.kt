package cz.jenda.tabor2022.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.model.GroupWithUsers

class GroupListAdapter :
    ListAdapter<GroupWithUsers, GroupListAdapter.GroupViewHolder>(GroupComparator()),
    WithItemListeners<GroupWithUsers> {

    var itemShortClick: OnItemShortClickListener<GroupWithUsers>? = null
    var itemLongClick: OnItemLongClickListener<GroupWithUsers>? = null
    var lastClickedItem: GroupWithUsers? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupViewHolder {
        return GroupViewHolder.create(this, parent)
    }


    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GroupViewHolder(val adapter: GroupListAdapter, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val userItemView: TextView = itemView.findViewById(R.id.group_name)
        private val totalPointsView: TextView = itemView.findViewById(R.id.total_points)

        fun bind(group: GroupWithUsers) {
            userItemView.text = group.group.name
            totalPointsView.text = group.totalPoints().toString()
            itemView.setOnClickListener {
                adapter.itemShortClick?.itemShortClicked(group)
                adapter.lastClickedItem = group
            }
            itemView.setOnLongClickListener {
                adapter.itemLongClick?.itemLongClicked(group)
                true
            }
        }

        companion object {
            fun create(adapter: GroupListAdapter, parent: ViewGroup): GroupViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_entry, parent, false)
                return GroupViewHolder(adapter, view)
            }
        }
    }

    class GroupComparator : DiffUtil.ItemCallback<GroupWithUsers>() {
        override fun areItemsTheSame(oldItem: GroupWithUsers, newItem: GroupWithUsers): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: GroupWithUsers, newItem: GroupWithUsers): Boolean {
            return oldItem.group.name == newItem.group.name
        }
    }

    override fun setOnItemClickListener(listener: OnItemShortClickListener<GroupWithUsers>) {
        itemShortClick = listener
    }

    override fun setOnLongItemClickListener(listener: OnItemLongClickListener<GroupWithUsers>) {
        itemLongClick = listener
    }
}