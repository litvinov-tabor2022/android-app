package cz.jenda.tabor2022.fragments.abstractions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras.DATA_TO_WRITE_ON_TAG
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.TagWriteActivity
import cz.jenda.tabor2022.adapters.UserListAdapter
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.data.proto.playerData
import cz.jenda.tabor2022.fragments.SearchableListFragment
import cz.jenda.tabor2022.viewmodel.UserViewModel
import cz.jenda.tabor2022.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

abstract class AbsUserListFragment :
    SearchableListFragment<UserAndSkills>(R.id.search_bar) {

    private val userViewModel: UserViewModel by viewModels { UserViewModelFactory() }
    lateinit var userListAdapter: UserListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListAdapter = UserListAdapter()

        listView.adapter = userListAdapter
        listView.layoutManager = LinearLayoutManager(context)

        userViewModel.users.observe(viewLifecycleOwner) { users ->
            data = users
            users?.let { userListAdapter.submitList(it.toMutableList()) }
            Log.i(Constants.AppTag, users.toString())
        }

        filteredData.observe(viewLifecycleOwner) { skills ->
            skills?.let { userListAdapter.submitList(it.toMutableList()) }
        }

        registerForContextMenu(listView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val list = view as ListView
        val info = menuInfo as AdapterContextMenuInfo
//        val user: User = filteredData[info.position]
//        list.getItemAtPosition(info.position)
//        menu.setHeaderTitle(user.name)
//        val inflater: MenuInflater? = activity?.menuInflater
//        inflater?.inflate(R.menu.user_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterContextMenuInfo
//        val user: User = filteredData[info.position]
//        Log.d(Constants.AppTag, user.toString())
//        when (item.itemId) {
//            R.id.menu_item_init_tag -> {
//                Log.d(Constants.AppTag, "Init tag from menu")
//                launch { initTag(user) }
//            }
//            R.id.menu_item_remove_user -> {
//                launch {
//                    runCatching {
//                        PortalApp.instance.db.usersDao().remove(user)
//                        Log.d(Constants.AppTag, "Remove user/tag from menu")
//                    }.onSuccess {
//                        data = data.filter { it.id != user.id }
//                    }
//                }
//            }
//        }
        return true
    }

    private fun initTag(user: User) {
        val player = playerData {
            userId = user.id.toInt()
            strength = user.strength
            dexterity = user.dexterity
            magic = user.magic
            bonusPoints = user.bonusPoints
            secret = Constants.TagSecret
        }

        val intent = Intent(view?.context, TagWriteActivity::class.java)
        intent.putExtra(DATA_TO_WRITE_ON_TAG, player)
        startActivity(intent)
    }

    override fun filter(text: CharSequence, data: List<UserAndSkills>): List<UserAndSkills> {
        return data.filter { user ->
            text.let { user.user.name.contains(it, ignoreCase = true) }
        }
    }

    override fun onResume() {
        super.onResume()
        launch {
            view?.findViewById<EditText>(R.id.search_bar)?.text = null
        }
    }

}

