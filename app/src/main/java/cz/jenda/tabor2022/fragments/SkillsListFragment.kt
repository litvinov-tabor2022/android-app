package cz.jenda.tabor2022.fragments

import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import cz.jenda.tabor2022.fragments.abstractions.AbsSkillListFragment
import cz.jenda.tabor2022.viewmodel.SkillViewModel

class SkillsListFragment : AbsSkillListFragment(null, SkillViewModel()) {
    override fun onCreateContextMenu(
        menu: ContextMenu,
        view: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val list = view as ListView
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
//        val user: Skill = filteredData[info.position]
//        list.getItemAtPosition(info.position)
//        menu.setHeaderTitle(user.name)
//        val inflater: MenuInflater? = activity?.menuInflater
//        inflater?.inflate(R.menu.user_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
//        val user: Skill = filteredData[info.position]
//        Log.d(Constants.AppTag, user.toString())
//        when (item.itemId) {
//            R.id.menu_item_init_tag -> {
//                Log.d(Constants.AppTag, "Init tag from menu")
//            }
//            R.id.menu_item_remove_user -> {
//                launch {
//                    runCatching {
//                        Log.d(Constants.AppTag, "Remove user/tag from menu")
//                    }.onSuccess {
//                        data = data.filter { it.id != user.id }
//                    }
//                }
//            }
//        }
        return true
    }
}