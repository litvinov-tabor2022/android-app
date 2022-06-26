package cz.jenda.tabor2022.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras.DATA_TO_WRITE_ON_TAG
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.TagWriteActivity
import cz.jenda.tabor2022.adapters.UserAdapter
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.data.proto.playerData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class UsersListFragment :
    BasicFragment() {

    private lateinit var users: List<User>
    private lateinit var filteredUsers: List<User>
    private var waitingInit: WaitingInit? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_users_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = view.findViewById<ListView>(R.id.users_list)

        view.findViewById<EditText>(R.id.search_bar).addTextChangedListener(textWatcher)

        registerForContextMenu(list);

        launch {
            fetchData()
            filteredUsers = users
            activity?.let { act -> act.runOnUiThread { list.adapter = UserAdapter(act, users) } }
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val list = view as ListView
        val info = menuInfo as AdapterContextMenuInfo
        val user: User = filteredUsers[info.position]
        list.getItemAtPosition(info.position)
        menu.setHeaderTitle(user.name)
        val inflater: MenuInflater? = activity?.menuInflater
        inflater?.inflate(R.menu.user_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterContextMenuInfo
        val user: User = filteredUsers[info.position]
        Log.d(Constants.AppTag, user.toString())
        when (item.itemId) {
            R.id.menu_item_init_tag -> {
                Log.d(Constants.AppTag, "Init tag from menu")
                launch { initTag(user) }
            }
            R.id.menu_item_remove_user -> {
                launch {
                    runCatching {
                        PortalApp.instance.db.usersDao().remove(user)
                        Log.d(Constants.AppTag, "Remove user/tag from menu")
                    }.onSuccess {
                        users = users.filter { it.id != user.id }
                        refreshView()
                    }
                }
            }
        }
        return true
    }

    private suspend fun fetchData() {
        users = PortalApp.instance.db.usersDao().getAll()
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

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val list = view?.findViewById<ListView>(R.id.users_list)

            filteredUsers = users.filter { user ->
                p0?.let { user.name.contains(it, ignoreCase = true) } ?: true
            }
            Log.d(Constants.AppTag, filteredUsers.toString())
            activity?.let { act ->
                act.runOnUiThread {
                    list?.adapter = UserAdapter(act, filteredUsers)
                }
            }
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }

    private fun refreshView() {
        val list = view?.findViewById<ListView>(R.id.users_list)
        activity?.let { act ->
            act.runOnUiThread {
                if (list != null) {
                    list.adapter = UserAdapter(act, users)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launch {
            fetchData()
            filteredUsers = users
            view?.findViewById<EditText>(R.id.search_bar)?.text = null
            refreshView()
        }
    }

    private data class WaitingInit(val data: Portal.PlayerData, val def: CompletableDeferred<Unit>)
}

