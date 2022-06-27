package cz.jenda.tabor2022.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.Skill


abstract class AbsSkillViewModel : ViewModel() {
    abstract val skills: LiveData<List<Skill>>
}

class SkillViewModel : AbsSkillViewModel() {
    override val skills: LiveData<List<Skill>> =
        PortalApp.instance.db.skillDao().getAll().asLiveData()
}