package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.fragments.AddSkillToUserFragment
import cz.jenda.tabor2022.fragments.UserDetailsOverviewFragment

class UserDetailsActivityPagerAdapter(
    val activity: BasicActivity, private val userWithSkills: UserAndSkills
) : FragmentStateAdapter(activity) {
    val transactionsBuffer: MutableLiveData<GameTransaction> by lazy {
        MutableLiveData<GameTransaction>()
    }

    val headerNames = arrayOf(
        activity.getString(R.string.userdetail_tab_overview),
        activity.getString(R.string.userstab_header_skills),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserDetailsOverviewFragment(userWithSkills, this)
            1 -> AddSkillToUserFragment(userWithSkills, this)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    fun pushTransaction(transaction: GameTransaction) {
//        transactionsBuffer = transactionsBuffer + transaction
//        binding.tab.setSelectedTabIndicatorColor(
//            if (pagerAdapter.transactionsBuffer.isEmpty()) ContextCompat.getColor(
//                this,
//                R.color.design_default_color_secondary
//            ) else ContextCompat.getColor(this, R.color.design_default_color_error)
//        )
    }
}