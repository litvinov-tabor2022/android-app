package cz.jenda.tabor2022.fragments.abstractions

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.GroupListAdapter
import cz.jenda.tabor2022.data.model.GroupWithUsers
import cz.jenda.tabor2022.fragments.SearchableListFragment
import cz.jenda.tabor2022.viewmodel.GroupViewModel
import cz.jenda.tabor2022.viewmodel.GroupsViewModelFactory
import kotlinx.coroutines.launch

abstract class AbsGroupListFragment : SearchableListFragment<GroupWithUsers>(R.id.search_bar) {
    val viewModel: GroupViewModel by viewModels { GroupsViewModelFactory() }
    lateinit var listAdapter: GroupListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = GroupListAdapter()

        listView.adapter = listAdapter
        listView.layoutManager = LinearLayoutManager(context)

        viewModel.groupsDataSource.observe(viewLifecycleOwner) { groups ->
            data = groups
            groups?.let { listAdapter.submitList(it.toMutableList()) }
            Log.i(Constants.AppTag, "Updating list of groups: $groups")
        }

        filteredData.observe(viewLifecycleOwner) { skills ->
            skills?.let { listAdapter.submitList(it.toMutableList()) }
        }
    }

    override fun filter(text: CharSequence, data: List<GroupWithUsers>): List<GroupWithUsers> {
        return data.filter { group ->
            text.let { group.group.name.contains(it, ignoreCase = true) }
        }
    }

    override fun onResume() {
        super.onResume()
        launch {
            view?.findViewById<EditText>(R.id.search_bar)?.text = null
        }
    }

}