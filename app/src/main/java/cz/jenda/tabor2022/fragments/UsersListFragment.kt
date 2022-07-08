package cz.jenda.tabor2022.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.activities.UserDetailActivity
import cz.jenda.tabor2022.adapters.OnItemShortClickListener
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.fragments.abstractions.AbsUserListFragment
import cz.jenda.tabor2022.viewmodel.AllUsersViewModelFactory
import cz.jenda.tabor2022.viewmodel.UserViewModel

class UsersListFragment : AbsUserListFragment() {
    override val viewModel: UserViewModel by viewModels { AllUsersViewModelFactory() }
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