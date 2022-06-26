package cz.jenda.tabor2022.fragments

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ListView
import android.widget.Toast
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.adapters.UserAdapter
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.data.proto.PlayerDataKt
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.data.proto.playerData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class TagInitFragment(activity: TagsActivity, private val actions: TagActions) :
    TagAwareFragmentBase(activity) {

    private lateinit var users: List<UserAndSkills>
    private var waitingInit: WaitingInit? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tag_init, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = view.findViewById<ListView>(R.id.users_list)

        registerForContextMenu(list);

        launch {
            users = PortalApp.instance.db.usersDao().getAllWithSkills()
            activity?.let { act ->
                act.runOnUiThread {
                    list.adapter = UserAdapter(act, users.map { it.user })
                }
            }
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val user = users[position]

            Log.i(Constants.AppTag, "About to initialize tag for $user")

            launch { initTag(user) }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val list = view as ListView
        val info = menuInfo as AdapterContextMenuInfo
        val userWithSkills: UserAndSkills = users[info.position]
        list.getItemAtPosition(info.position)
        menu.setHeaderTitle(userWithSkills.user.name)
        val inflater: MenuInflater? = activity?.menuInflater
        inflater?.inflate(R.menu.user_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterContextMenuInfo
        val userWithSkills: UserAndSkills = users[info.position]
        Log.d(Constants.AppTag, userWithSkills.toString())
        when (item.itemId) {
            R.id.menu_item_init_tag -> {
                Log.d(Constants.AppTag, "Init tag from menu")
                launch { initTag(userWithSkills) }
            }
            R.id.menu_item_remove_user -> {
                Log.d(Constants.AppTag, "Remove user/tag from menu")
            }
        }
        return true
    }


    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        Log.d(Constants.AppTag, "Tag read @ TagInitFragment: $tagData")

        val waitingInit = this.waitingInit // snapshot
        this.waitingInit = null

        if (waitingInit != null) {
            Log.d(Constants.AppTag, "Will initialize tag: ${waitingInit.data}")
            runCatching { actions.writeToTag(tag, waitingInit.data) }
                .onSuccess { waitingInit.def.complete(Unit) }
                .onFailure { waitingInit.def.completeExceptionally(it) }
            Log.v(Constants.AppTag, "nulling waitingInit")
            return
        }
    }

    private suspend fun initTag(userWithSkills: UserAndSkills) {
        val player = playerData {
            userId = userWithSkills.user.id.toInt()
            strength = userWithSkills.user.strength
            dexterity = userWithSkills.user.dexterity
            magic = userWithSkills.user.magic
            bonusPoints = userWithSkills.user.bonusPoints
            skills.addAll(userWithSkills.skills.map { Portal.Skill.forNumber(it.id.toInt()) })
            secret = Constants.TagSecret
        }

        val def = CompletableDeferred<Unit>()
        waitingInit = WaitingInit(player, def)
        activity?.runOnUiThread {
            Toast.makeText(activity, R.string.tag_init_insert_tag, Toast.LENGTH_SHORT).show()
        }

        runCatching { def.await() }
            .onSuccess {
                activity?.runOnUiThread {
                    Toast.makeText(
                        activity,
                        R.string.tag_init_done,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .onFailure { e ->
                activity?.runOnUiThread {
                    Toast.makeText(activity, R.string.tag_init_failure, Toast.LENGTH_SHORT).show()
                }
                Log.e(Constants.AppTag, "Couldn't initialize the tag!", e)
            }
    }

    private data class WaitingInit(val data: Portal.PlayerData, val def: CompletableDeferred<Unit>)
}
