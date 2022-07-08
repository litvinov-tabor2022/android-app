package cz.jenda.tabor2022.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.*
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.GroupWithUsers

abstract class GroupViewModel : ViewModel() {
    protected val groupComparator =
        Comparator { group1: GroupWithUsers, group2: GroupWithUsers ->
            val totalPointsDelta =
                group2.totalPoints() - group1.totalPoints()
            if (totalPointsDelta == 0) {
                group1.group.name.compareTo(group2.group.name)
            } else {
                totalPointsDelta
            }
        }

    abstract val groupsDataSource: LiveData<List<GroupWithUsers>>
}

class GroupsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return (@SuppressLint("StaticFieldLeak")
            object : GroupViewModel() {
                override val groupsDataSource: LiveData<List<GroupWithUsers>> =
                    PortalApp.instance.db.groupDao().getAll().asLiveData()
                        .map { it.sortedWith(groupComparator) }
            }) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}