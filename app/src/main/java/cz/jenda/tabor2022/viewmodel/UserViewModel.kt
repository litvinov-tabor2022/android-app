package cz.jenda.tabor2022.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.*
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.UserWithGroup

abstract class UserViewModel : ViewModel() {
    protected val userComparator =
        Comparator { user1: UserWithGroup, user2: UserWithGroup ->
            val totalPointsDelta =
                user2.userWithSkills.user.totalPoints() - user1.userWithSkills.user.totalPoints()
            if (totalPointsDelta == 0) {
                user1.userWithSkills.user.name.compareTo(user2.userWithSkills.user.name)
            } else {
                totalPointsDelta
            }
        }

    abstract val usersDataSource: LiveData<List<UserWithGroup>>
}

class AllUsersViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return (@SuppressLint("StaticFieldLeak")
            object : UserViewModel() {
                override val usersDataSource: LiveData<List<UserWithGroup>> =
                    PortalApp.instance.db.usersDao().getAll().asLiveData()
                        .map { it.sortedWith(userComparator) }
            }) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserViewModelFactory(private val dataSource: LiveData<List<UserWithGroup>>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return (@SuppressLint("StaticFieldLeak")
            object : UserViewModel() {
                override val usersDataSource: LiveData<List<UserWithGroup>> = dataSource
            }) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}