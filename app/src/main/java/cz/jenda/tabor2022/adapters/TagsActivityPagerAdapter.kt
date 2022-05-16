package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.fragments.TagDiscoverFragment
import cz.jenda.tabor2022.fragments.TagInitFragment

class TagsActivityPagerAdapter(private val activity: TagsActivity, private val actions: TagActions) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.tagstab_header_discover),
        activity.getString(R.string.tagstab_header_init),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TagDiscoverFragment(activity, actions)
            1 -> TagInitFragment(activity, actions)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}


