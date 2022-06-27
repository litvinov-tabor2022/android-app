package cz.jenda.tabor2022.fragments

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.adapters.OnItemShortClickListener
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.data.proto.playerData
import cz.jenda.tabor2022.fragments.abstractions.TagAwareFragmentBase
import cz.jenda.tabor2022.fragments.abstractions.AbsUserListFragment
import cz.jenda.tabor2022.viewmodel.UserViewModel
import cz.jenda.tabor2022.viewmodel.UserViewModelFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class TagInitFragment(
    activity: TagsActivity,
    private val actions: TagActions
) :
    TagAwareFragmentBase(activity) {

    private var waitingInit: WaitingInit? = null

    class UserList(val fragment: TagInitFragment) : AbsUserListFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            userListAdapter.setOnItemClickListener(object :
                OnItemShortClickListener<UserAndSkills> {
                override fun itemShortClicked(item: UserAndSkills) {
                    Log.i(Constants.AppTag, "About to initialize tag for $item")
                    launch { fragment.initTag(item) }
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager
            .beginTransaction()
            .add(R.id.users_list, UserList(this))
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tag_init, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val adapter = UserListAdapter(object : OnItemClickListener<UserAndSkills> {
//            override fun onItemClick(item: UserAndSkills?) {
//
//            }
//
//        })
//        listView.adapter = adapter
//        listView.layoutManager = LinearLayoutManager(context)
//        userViewModel.users.observe(viewLifecycleOwner) { users ->
//            data = users
//            users?.let { adapter.submitList(it.toMutableList()) }
//            Log.i(Constants.AppTag, users.toString())
//        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
//        super.onCreateContextMenu(menu, view, menuInfo)
//        val userWithSkills: UserAndSkills = users[info.position]
//        list.getItemAtPosition(info.position)
//        menu.setHeaderTitle(userWithSkills.user.name)
//        val inflater: MenuInflater? = activity?.menuInflater
//        inflater?.inflate(R.menu.user_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
//        super.onContextItemSelected(item)
//        val info = item.menuInfo as AdapterContextMenuInfo
//        val userWithSkills: UserAndSkills = users[info.position]
//        Log.d(Constants.AppTag, userWithSkills.toString())
//        when (item.itemId) {
//            R.id.menu_item_init_tag -> {
//                Log.d(Constants.AppTag, "Init tag from menu")
//                launch { initTag(userWithSkills) }
//            }
//            R.id.menu_item_remove_user -> {
//                Log.d(Constants.AppTag, "Remove user/tag from menu")
//            }
//        }
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

    override fun onResume() {
        super.onResume()
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
