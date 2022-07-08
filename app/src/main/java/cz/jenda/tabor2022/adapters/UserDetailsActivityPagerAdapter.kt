package cz.jenda.tabor2022.adapters

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BasicActivity
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.fragments.AddSkillToUserFragment
import cz.jenda.tabor2022.fragments.UserDetailsOverviewFragment

class UserDetailsActivityPagerAdapter(
    val activity: BasicActivity,
    val userWithGroup: UserWithGroup,
    var data: MutableLiveData<Portal.PlayerData.Builder>
) : FragmentStateAdapter(activity) {

    val headerNames = arrayOf(
        activity.getString(R.string.userdetail_tab_overview),
        activity.getString(R.string.userstab_header_skills),
    )

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserDetailsOverviewFragment(this)
            1 -> AddSkillToUserFragment(this)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    fun addSkill(skill: Skill) {
        data.postValue(data.value?.addSkills(Portal.Skill.forNumber(skill.id.toInt())))
    }

    fun removeSkill(skill: Skill) {
        val updatedPlayerData: () -> Portal.PlayerData.Builder? = {
            val ds = data.value
            val skills = ds?.skillsList?.filter { it.number != skill.id.toInt() }
            ds?.clearSkills()
            ds?.addAllSkills(skills)
            ds
        }
        data.postValue(updatedPlayerData())
    }
}