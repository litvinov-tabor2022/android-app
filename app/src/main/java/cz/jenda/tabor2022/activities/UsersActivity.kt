package cz.jenda.tabor2022.activities

import android.content.Intent
import android.nfc.tech.MifareClassic
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UsersActivityPagerAdapter
import cz.jenda.tabor2022.data.Helpers
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.databinding.ActivityUsersBinding
import cz.jenda.tabor2022.fragments.dialogs.InconsistentDataDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UsersActivity : NfcActivityBase(), InconsistentDataDialog.InconsistentDataDialogListener {
    private var tagData: Portal.PlayerData? = null

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        var userWithGroup: UserWithGroup?
        this.tagData = tagData
        launch(Dispatchers.IO) {
            userWithGroup = tagData?.userId?.let {
                PortalApp.instance.db.usersDao().getById(it.toLong()).first()
            }

            tagData?.let {
                userWithGroup?.let { user ->
                    if (!Helpers.isEqual(user.userWithSkills, tagData)) {
                        val dialog = InconsistentDataDialog()
                        dialog.show(supportFragmentManager, "")
                    } else {
                        val intent = Intent(applicationContext, UserDetailActivity::class.java)
                        intent.putExtra(Extras.USER_EXTRA, tagData.userId.toLong())
                        startActivity(intent)
                    }
                }
            }
        }.join()
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

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val intent = Intent(applicationContext, UserDetailActivity::class.java)
        if (tagData != null) {
            intent.putExtra(Extras.USER_EXTRA, tagData?.userId?.toLong())
            intent.putExtra(Extras.PLAYERS_DATA_EXTRA, tagData)
            startActivity(intent)
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}