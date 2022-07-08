package cz.jenda.tabor2022.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.activities.GroupDetailActivity
import cz.jenda.tabor2022.adapters.OnItemShortClickListener
import cz.jenda.tabor2022.data.model.GroupWithUsers
import cz.jenda.tabor2022.fragments.abstractions.AbsGroupListFragment

class GroupListFragment : AbsGroupListFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter.setOnItemClickListener(object :
            OnItemShortClickListener<GroupWithUsers> {
            override fun itemShortClicked(item: GroupWithUsers) {
                val intent = Intent(view.context, GroupDetailActivity::class.java)
                item.let { intent.putExtra(Extras.GROUP_EXTRA, it.group.id) }
                startActivity(intent)
            }
        })
    }
}