package cz.jenda.tabor2022.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.room.withTransaction
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
import cz.jenda.tabor2022.fragments.dialogs.ImportDataConfirmationDialog
import cz.jenda.tabor2022.fragments.dialogs.WriteToTagDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class UserDetailActivity : NfcActivityBase(), WriteToTagDialog.WriteToTagDialogListener,
    ImportDataConfirmationDialog.ImportDataConfirmationDialogListener {
    private var userId by Delegates.notNull<Long>()
    private lateinit var userWithGroup: UserWithGroup
    private lateinit var referencePlayerData: Portal.PlayerData
    private val playerData: MutableLiveData<Portal.PlayerData.Builder> = MutableLiveData()
    private lateinit var fabSave: FloatingActionButton
    private lateinit var fabImport: FloatingActionButton
    private var readOnly: Boolean = false

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

        runBlocking {
            launch(Dispatchers.IO) {
                userWithGroup = PortalApp.instance.db.usersDao().getById(userId).first()
            }.join()
        }

        kotlin.runCatching { intent.getSerializableExtra(Extras.PLAYERS_DATA_EXTRA) as Portal.PlayerData }
            .onSuccess {
                playerData.value =
                    (intent.getSerializableExtra(Extras.PLAYERS_DATA_EXTRA) as Portal.PlayerData).toBuilder()
                readOnly = true
            }.onFailure {
                playerData.value = userWithGroup.userWithSkills.toPlayerData()
                    .toBuilder()
            }

        setContentView(R.layout.fragment_user_details)

        val binding = ActivityUserDetailsBinding.inflate(layoutInflater)

        fabSave = binding.fabSave
        fabImport = binding.fabImport

        if (readOnly) {
            fabSave.backgroundTintList = ColorStateList.valueOf(Color.rgb(255, 50, 50))
            fabImport.visibility = FloatingActionButton.VISIBLE
        } else {
            fabImport.visibility = FloatingActionButton.INVISIBLE
        }

        setContentView(binding.root)
        val viewPager = binding.viewPager

        referencePlayerData =
            playerData.value?.build() ?: userWithGroup.userWithSkills.toPlayerData()

        val pagerAdapter =
            UserDetailsActivityPagerAdapter(
                this@UserDetailActivity,
                userWithGroup,
                playerData
            )
        viewPager.adapter = pagerAdapter

        playerData.observe(this) {
            Log.d(Constants.AppTag, "Transaction buffer changed: $it")

            if (it.build() != referencePlayerData) {
                fabSave.visibility = FloatingActionButton.VISIBLE
            } else {
                fabSave.visibility = FloatingActionButton.INVISIBLE
            }
            binding.tabs.setSelectedTabIndicatorColor(
                if (it.build() != referencePlayerData) ContextCompat.getColor(
                    this,
                    R.color.design_default_color_error
                ) else ContextCompat.getColor(this, R.color.green)
            )
        }

        fabSave.setOnClickListener {
            val intent = Intent(it.context, TagWriteActivity::class.java)
            intent.putExtra(
                Extras.DATA_TO_WRITE_ON_TAG,
                playerData.value?.build()
            )
            intent.putExtra(
                Extras.REFERENCE_DATA,
                referencePlayerData
            )
            startForResult.launch(intent)
        }

        fabImport.setOnClickListener {
            val dialog = ImportDataConfirmationDialog()
            dialog.show(this.supportFragmentManager, "")
        }

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.headerNames[position]
        }.attach()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is ImportDataConfirmationDialog) {
            Log.i(Constants.AppTag, "Importing (forcing) data for user ${userWithGroup.userWithSkills}")

            val user = playerData.value?.build()!!
            val db = PortalApp.instance.db

            launch {
                db.withTransaction {
                    db.usersDao().save(userId, user.strength, user.dexterity, user.magic, user.bonusPoints)
                    db.userSkillCrossRefDao().removeAllSkills(userId)

                    user.skillsList.forEach { skill ->
                        db.userSkillCrossRefDao().addSkill(userId, skill.number.toLong())
                    }
                }
            }

            Toast.makeText(this.baseContext, R.string.data_imported, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }
}