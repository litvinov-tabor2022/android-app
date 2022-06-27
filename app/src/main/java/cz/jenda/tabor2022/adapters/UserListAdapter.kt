package cz.jenda.tabor2022.adapters

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.model.UserAndSkills


class UserListAdapter :
    ListAdapter<UserAndSkills, UserListAdapter.UserViewHolder>(UserComparator()),
    WithItemListeners<UserAndSkills> {

    var itemShortClick: OnItemShortClickListener<UserAndSkills>? = null
    var itemLongClick: OnItemLongClickListener<UserAndSkills>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder.create(this, parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(val adapter: UserListAdapter, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val userItemView: TextView = itemView.findViewById(R.id.portal_id)

        fun bind(user: UserAndSkills) {
            userItemView.text = user.user.name
            itemView.setOnClickListener {
                adapter.itemShortClick?.itemShortClicked(user)
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

    class UserComparator : DiffUtil.ItemCallback<UserAndSkills>() {
        override fun areItemsTheSame(oldItem: UserAndSkills, newItem: UserAndSkills): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: UserAndSkills, newItem: UserAndSkills): Boolean {
            return oldItem.user.name == newItem.user.name
        }
    }

    override fun setOnItemClickListener(listener: OnItemShortClickListener<UserAndSkills>) {
        itemShortClick = listener
    }

    override fun setOnLongItemClickListener(listener: OnItemLongClickListener<UserAndSkills>) {
        itemLongClick = listener
    }
}