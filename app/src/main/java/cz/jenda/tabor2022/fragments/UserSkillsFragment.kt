package cz.jenda.tabor2022.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.fragments.abstractions.AbsSkillListFragment
import cz.jenda.tabor2022.viewmodel.AbsSkillViewModel

class UserSkillsFragment(userWithSkills: UserAndSkills) :
    AbsSkillListFragment(userWithSkills, UserSkillsViewModel(userWithSkills)) {

    class UserSkillsViewModel(userWithSkills: UserAndSkills) : AbsSkillViewModel() {
        override val skills: LiveData<List<Skill>> =
            PortalApp.instance.db.usersDao().getById(userWithSkills.user.id).asLiveData()
                .map { it.skills }
    }
}