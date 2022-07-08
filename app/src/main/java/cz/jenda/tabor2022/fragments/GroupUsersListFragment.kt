package cz.jenda.tabor2022.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.activities.UserDetailActivity
import cz.jenda.tabor2022.adapters.OnItemShortClickListener
import cz.jenda.tabor2022.data.model.Group
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.fragments.abstractions.AbsUserListFragment
import cz.jenda.tabor2022.viewmodel.UserViewModel
import cz.jenda.tabor2022.viewmodel.UserViewModelFactory

class GroupUsersListFragment(group: Group) : AbsUserListFragment() {
    override val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            PortalApp.instance.db.groupDao().members(group.id).asLiveData()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter.setOnItemClickListener(object :
            OnItemShortClickListener<UserWithGroup> {
            override fun itemShortClicked(item: UserWithGroup) {
                val intent = Intent(view.context, UserDetailActivity::class.java)
                item.let { intent.putExtra(Extras.USER_EXTRA, it.userWithSkills.user.id) }
                startActivity(intent)
            }
        })
    }
}