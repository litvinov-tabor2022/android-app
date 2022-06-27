package cz.jenda.tabor2022.activities

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UserDetailsActivityPagerAdapter
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.databinding.ActivityUserDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class UserDetailActivity : BasicActivity() {
    private var userId by Delegates.notNull<Long>()
    private lateinit var userWithSkills: UserAndSkills
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = intent.getSerializableExtra(Extras.USER_EXTRA) as Long
        setContentView(R.layout.fragment_user_details)

        val binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewPager = binding.viewPager

        runBlocking {
            launch(Dispatchers.IO) {
                userWithSkills = PortalApp.instance.db.usersDao().getById(userId).first()
            }.join()
        }

        val pagerAdapter =
            UserDetailsActivityPagerAdapter(this@UserDetailActivity, userWithSkills)
        viewPager.adapter = pagerAdapter

        binding.tabs.setSelectedTabIndicatorColor(
            if (pagerAdapter.transactionsBuffer.value == null) ContextCompat.getColor(
                this,
                R.color.green
            ) else ContextCompat.getColor(this, R.color.design_default_color_error)
        )

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }
}