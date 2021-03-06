package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.data.model.Group
import cz.jenda.tabor2022.fragments.*

class UsersActivityPagerAdapter(
    private val activity: BasicActivity
) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.userstab_header_list),
        activity.getString(R.string.groups_header),
        activity.getString(R.string.userstab_header_skills),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UsersListFragment()
            1 -> GroupListFragment()
            2 -> SkillsListFragment()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}


