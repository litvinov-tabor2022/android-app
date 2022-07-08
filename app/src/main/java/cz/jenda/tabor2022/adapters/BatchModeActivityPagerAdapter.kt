package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.fragments.TagInitFragment
import cz.jenda.tabor2022.fragments.UserNewFragment

class BatchModeActivityPagerAdapter(private val activity: TagsActivity, private val actions: TagActions) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.tagstab_header_init),
        activity.getString(R.string.userstab_header_new),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TagInitFragment(activity, actions)
            1 -> UserNewFragment()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return headerNames.size
    }

}


