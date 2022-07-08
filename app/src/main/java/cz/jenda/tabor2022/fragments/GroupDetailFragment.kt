package cz.jenda.tabor2022.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.model.Group
import cz.jenda.tabor2022.data.model.GroupStatistics
import cz.jenda.tabor2022.databinding.FragmentGroupBinding
import cz.jenda.tabor2022.fragments.abstractions.BasicFragment
import cz.jenda.tabor2022.viewmodel.GroupDetailViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GroupDetailFragment(private val group: Group) : BasicFragment() {
    private lateinit var groupStatistics: GroupStatistics
    private lateinit var usersListFragment: GroupUsersListFragment
    private lateinit var viewModel: GroupDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        runBlocking {
            launch {
                val members = PortalApp.instance.db.groupDao().getById(group.id).first()
                groupStatistics = GroupStatistics(group, members.members, 0)
            }.join()
        }
        usersListFragment = GroupUsersListFragment(groupStatistics.group)
        viewModel = GroupDetailViewModel(groupStatistics.members, groupStatistics)
        val binding = FragmentGroupBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            val usersFragment = GroupUsersListFragment(groupStatistics.group)
            childFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.users_list, usersFragment)
            }
        }
    }
}