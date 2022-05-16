package cz.jenda.tabor2022.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UsersActivityPagerAdapter
import cz.jenda.tabor2022.databinding.ActivityUsersBinding

class UsersActivity : BasicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        val binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val pagerAdapter = UsersActivityPagerAdapter(this)

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }
}