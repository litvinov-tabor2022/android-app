package cz.jenda.tabor2022.activities

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.TagsActivityPagerAdapter
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.databinding.ActivityTagsBinding
import cz.jenda.tabor2022.fragments.abstractions.TagAwareFragmentBase

class TagsActivity : NfcActivityBase() {
    private var currentFragment: TagAwareFragmentBase? = null

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        kotlin.runCatching { currentFragment?.onTagRead(tag, tagData) }
            .onFailure { e -> Log.e(Constants.AppTag, "Tag handling by $currentFragment failed!", e) }
    }

    fun setCurrentFragment(fr: TagAwareFragmentBase?) {
        currentFragment = fr
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        val binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val pagerAdapter = TagsActivityPagerAdapter(this, actions)

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }
}
