package cz.jenda.tabor2022.fragments

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.adapters.UserAdapter
import cz.jenda.tabor2022.data.User
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.data.proto.playerData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class TagInitFragment(activity: TagsActivity, private val actions: TagActions) : TagAwareFragmentBase(activity) {

    private lateinit var users: List<User>
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

        launch {
            users = PortalApp.instance.db.usersDao().getAll()
            activity?.let { act -> act.runOnUiThread { list.adapter = UserAdapter(act, users) } }
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val user = users[position]

            Log.i(Constants.AppTag, "About to initialize tag for $user")

            launch { initTag(user) }
        }
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

    private suspend fun initTag(user: User) {
        val player = playerData {
            userId = user.id
            strength = user.strength
            dexterity = user.dexterity
            magic = user.magic
            bonusPoints = user.bonusPoints
            secret = Constants.TagSecret
        }

        val def = CompletableDeferred<Unit>()
        waitingInit = WaitingInit(player, def)
        activity?.runOnUiThread {
            Toast.makeText(activity, R.string.tag_init_insert_tag, Toast.LENGTH_SHORT).show()
        }

        runCatching { def.await() }
            .onSuccess { activity?.runOnUiThread { Toast.makeText(activity, R.string.tag_init_done, Toast.LENGTH_SHORT).show() } }
            .onFailure { e ->
                activity?.runOnUiThread {
                    Toast.makeText(activity, R.string.tag_init_failure, Toast.LENGTH_SHORT).show()
                }
                Log.e(Constants.AppTag, "Couldn't initialize the tag!", e)
            }
    }

}

private data class WaitingInit(val data: Portal.PlayerData, val def: CompletableDeferred<Unit>)