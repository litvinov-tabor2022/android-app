package cz.jenda.tabor2022.adapters

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.activities.UserDetailActivity
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.fragments.*
import cz.jenda.tabor2022.fragments.abstractions.AbsUserListFragment

class UsersActivityPagerAdapter(
    private val activity: BasicActivity
) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.userstab_header_list),
        activity.getString(R.string.userstab_header_skills),
        activity.getString(R.string.userstab_header_new),
    )

    class UsersList : AbsUserListFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            userListAdapter.setOnItemClickListener(object :
                OnItemShortClickListener<UserAndSkills> {
                override fun itemShortClicked(item: UserAndSkills) {
                    val intent = Intent(view.context, UserDetailActivity::class.java)
                    item.let { intent.putExtra(Extras.USER_EXTRA, it.user.id) }
                    startActivity(intent)
                }
            })
        }
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UsersList()
            1 -> SkillsListFragment()
            2 -> UserNewFragment()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}


