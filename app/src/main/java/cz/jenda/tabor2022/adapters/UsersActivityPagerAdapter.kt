package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.fragments.UserNewFragment
import cz.jenda.tabor2022.fragments.UsersListFragment

class UsersActivityPagerAdapter(
    private val activity: BasicActivity
) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.userstab_header_list),
        activity.getString(R.string.userstab_header_new),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UsersListFragment()
            1 -> UserNewFragment()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}


