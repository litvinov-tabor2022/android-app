package cz.jenda.tabor2022.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserWithGroup

/**
 * @param user - if not null, skills limitations are color based on the user's stats
 */
class SkillListAdapter(
    private val user: UserWithGroup?
) :
    ListAdapter<Skill, SkillListAdapter.SkillViewHolder>(SkillComparator()),
    WithItemListeners<Skill> {

    var itemShortClick: OnItemShortClickListener<Skill>? = null
    var itemLongClick: OnItemLongClickListener<Skill>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        return SkillViewHolder.create(this, parent, user)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = getItem(position)
        holder.bind(skill)
    }

    class SkillViewHolder(
        val adapter: SkillListAdapter,
        itemView: View,
        val userWithSkills: UserWithGroup?
    ) : RecyclerView.ViewHolder(itemView) {
        private val userItemView: TextView = itemView.findViewById(R.id.skill_name)
        private val strengthLimit = itemView.findViewById(R.id.strength_limit) as TextView
        private val dexterityLimit = itemView.findViewById(R.id.dexterity_limit) as TextView
        private val magicLimit = itemView.findViewById(R.id.magic_limit) as TextView

        fun bind(skill: Skill) {
            val user = userWithSkills?.userWithSkills?.user
            userItemView.text = skill.name
            strengthLimit.text =
                strengthLimit.context.getString(R.string.strength_limit, skill.strength)
            dexterityLimit.text =
                dexterityLimit.context.getString(R.string.dexterity_limit, skill.dexterity)
            magicLimit.text =
                magicLimit.context.getString(R.string.magic_limit, skill.magic)
            user?.let {
                val green = ContextCompat.getColor(strengthLimit.context, R.color.green)
                val red = ContextCompat.getColor(
                    strengthLimit.context,
                    R.color.design_default_color_error
                )
                strengthLimit.setTextColor(
                    if (user.strength >= skill.strength) green else red
                )
                dexterityLimit.setTextColor(
                    if (user.dexterity >= skill.dexterity) green else red
                )
                magicLimit.setTextColor(
                    if (user.magic >= skill.magic) green else red
                )
            }
            itemView.setOnClickListener {
                adapter.itemShortClick?.itemShortClicked(skill)
            }

            itemView.setOnLongClickListener {
                adapter.itemLongClick?.let {
                    it.itemLongClicked(skill)
                    itemView.showContextMenu()
                }
                true
            }
        }

        companion object {
            fun create(
                adapter: SkillListAdapter,
                parent: ViewGroup,
                user: UserWithGroup?
            ): SkillViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.skill_entry, parent, false)
                return SkillViewHolder(adapter, view, user)
            }
        }
    }

    class SkillComparator : DiffUtil.ItemCallback<Skill>() {
        override fun areItemsTheSame(oldItem: Skill, newItem: Skill): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Skill, newItem: Skill): Boolean {
            return oldItem.name == newItem.name
        }
    }

    override fun setOnItemClickListener(listener: OnItemShortClickListener<Skill>) {
        itemShortClick = listener
    }

    override fun setOnLongItemClickListener(listener: OnItemLongClickListener<Skill>) {
        itemLongClick = listener
    }
}