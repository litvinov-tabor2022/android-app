package cz.jenda.tabor2022.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.GroupDetailActivityPagerAdapter
import cz.jenda.tabor2022.data.model.Group
import cz.jenda.tabor2022.data.model.GroupWithUsers
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.databinding.ActivityUsersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class GroupDetailActivity : BasicActivity() {
    private var groupId by Delegates.notNull<Long>()
    private lateinit var group: GroupWithUsers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = intent.getSerializableExtra(Extras.GROUP_EXTRA) as Long
        setContentView(R.layout.fragment_user_details)

        setContentView(R.layout.activity_groups)

        val binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager

        runBlocking {
            launch(Dispatchers.IO) {
                group = PortalApp.instance.db.groupDao().getById(groupId).first()
            }.join()
        }

        val pagerAdapter = GroupDetailActivityPagerAdapter(this, group)

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }
}