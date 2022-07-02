package cz.jenda.tabor2022.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.UserAndSkills

class UserViewModel : ViewModel() {
    val users: LiveData<List<UserAndSkills>> = PortalApp.instance.db.usersDao().getAll().asLiveData()
}

class UserViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}