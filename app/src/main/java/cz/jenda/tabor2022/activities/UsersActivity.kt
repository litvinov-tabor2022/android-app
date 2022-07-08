package cz.jenda.tabor2022.activities

import android.content.Intent
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UsersActivityPagerAdapter
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.databinding.ActivityUsersBinding
import cz.jenda.tabor2022.fragments.abstractions.TagAwareFragmentBase
import kotlinx.coroutines.flow.first

class UsersActivity : NfcActivityBase() {

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        val intent = Intent(applicationContext, UserDetailActivity::class.java)
        if (tagData != null) {
            intent.putExtra(Extras.USER_EXTRA, tagData.userId.toLong())
            startActivity(intent)
        }
    }

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