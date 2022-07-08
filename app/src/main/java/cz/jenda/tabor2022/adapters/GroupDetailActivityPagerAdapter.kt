package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.data.model.GroupWithUsers
import cz.jenda.tabor2022.fragments.GroupDetailFragment

class GroupDetailActivityPagerAdapter(
    private val activity: BasicActivity,
    private val groupWithUsers: GroupWithUsers
) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.userdetail_tab_overview),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GroupDetailFragment(groupWithUsers.group)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

}