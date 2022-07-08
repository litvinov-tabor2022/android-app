package cz.jenda.tabor2022.activities

import android.app.Activity
import android.content.Intent
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UserDetailsActivityPagerAdapter
import cz.jenda.tabor2022.data.Helpers.toPlayerData
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.databinding.ActivityUserDetailsBinding
import cz.jenda.tabor2022.fragments.dialogs.WriteToTagDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class UserDetailActivity : NfcActivityBase(), WriteToTagDialog.WriteToTagDialogListener {
    private var userId by Delegates.notNull<Long>()
    private lateinit var userWithGroup: UserWithGroup
    private lateinit var referencePlayerData: Portal.PlayerData
    private val playerData: MutableLiveData<Portal.PlayerData.Builder> = MutableLiveData()
    private lateinit var fabSave: FloatingActionButton

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // clear transaction log
                fabSave.visibility = FloatingActionButton.INVISIBLE
                referencePlayerData = playerData.value?.build() ?: referencePlayerData
            }
        }

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        // suppress showing app selection menu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = intent.getSerializableExtra(Extras.USER_EXTRA) as Long
        setContentView(R.layout.fragment_user_details)

        val binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        fabSave = binding.fab
        setContentView(binding.root)
        val viewPager = binding.viewPager

        runBlocking {
            launch(Dispatchers.IO) {
                userWithGroup = PortalApp.instance.db.usersDao().getById(userId).first()
            }.join()
        }

        referencePlayerData = userWithGroup.userWithSkills.toPlayerData()
        playerData.value = userWithGroup.userWithSkills.toPlayerData().toBuilder()

        val pagerAdapter =
            UserDetailsActivityPagerAdapter(
                this@UserDetailActivity,
                userWithGroup,
                playerData
            )
        viewPager.adapter = pagerAdapter

        playerData.observe(this) {
            Log.i(Constants.AppTag, "Transaction buffer changed: $it")

            if (it.build() != referencePlayerData) {
                fabSave.visibility = FloatingActionButton.VISIBLE
            } else {
                fabSave.visibility = FloatingActionButton.INVISIBLE
            }
            binding.tabs.setSelectedTabIndicatorColor(
                if (it.build() != referencePlayerData) ContextCompat.getColor(
                    this,
                    R.color.green
                ) else ContextCompat.getColor(this, R.color.design_default_color_error)
            )
        }

        fabSave.setOnClickListener {
            val intent = Intent(it.context, TagWriteActivity::class.java)
            intent.putExtra(
                Extras.DATA_TO_WRITE_ON_TAG,
                playerData.value?.build()
            )
            startForResult.launch(intent)
        }

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }
}