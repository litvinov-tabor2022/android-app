package cz.jenda.tabor2022.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.fragments.abstractions.AbsSkillListFragment
import cz.jenda.tabor2022.viewmodel.AbsSkillViewModel

class UserSkillsFragment(userWithGroup: UserWithGroup) :
    AbsSkillListFragment(userWithGroup, UserSkillsViewModel(userWithGroup)) {

    class UserSkillsViewModel(userWithGroup: UserWithGroup) : AbsSkillViewModel() {
        override val skills: LiveData<List<Skill>> =
            PortalApp.instance.db.usersDao().getById(userWithGroup.userWithSkills.user.id).asLiveData()
                .map { it.userWithSkills.skills }
    }
}