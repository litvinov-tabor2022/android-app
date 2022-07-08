package cz.jenda.tabor2022.adapters

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.model.UserWithGroup


class UserListAdapter :
    ListAdapter<UserWithGroup, UserListAdapter.UserViewHolder>(UserComparator()),
    WithItemListeners<UserWithGroup> {

    var itemShortClick: OnItemShortClickListener<UserWithGroup>? = null
    var itemLongClick: OnItemLongClickListener<UserWithGroup>? = null
    var lastClickedItem: UserWithGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder.create(this, parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(val adapter: UserListAdapter, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val userItemView: TextView = itemView.findViewById(R.id.user_name)
        private val totalPointsView: TextView = itemView.findViewById(R.id.total_points)
        private val groupView: TextView = itemView.findViewById(R.id.group)

        fun bind(user: UserWithGroup) {
            userItemView.text = user.userWithSkills.user.name
            totalPointsView.text = user.userWithSkills.user.totalPoints().toString()
            groupView.text = user.group?.name ?: "N/A"
            itemView.setOnClickListener {
                adapter.itemShortClick?.itemShortClicked(user)
                adapter.lastClickedItem = user
            }
            itemView.setOnLongClickListener {
                adapter.itemLongClick?.itemLongClicked(user)
                true
            }
        }

        companion object {
            fun create(adapter: UserListAdapter, parent: ViewGroup): UserViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_entry, parent, false)
                return UserViewHolder(adapter, view)
            }
        }
    }

    class UserComparator : DiffUtil.ItemCallback<UserWithGroup>() {
        override fun areItemsTheSame(oldItem: UserWithGroup, newItem: UserWithGroup): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: UserWithGroup, newItem: UserWithGroup): Boolean {
            return oldItem.userWithSkills.user.name == newItem.userWithSkills.user.name
        }
    }

    override fun setOnItemClickListener(listener: OnItemShortClickListener<UserWithGroup>) {
        itemShortClick = listener
    }

    override fun setOnLongItemClickListener(listener: OnItemLongClickListener<UserWithGroup>) {
        itemLongClick = listener
    }
}