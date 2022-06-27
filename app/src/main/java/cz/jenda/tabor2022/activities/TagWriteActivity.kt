package cz.jenda.tabor2022.activities

import android.app.Activity
import android.database.sqlite.SQLiteConstraintException
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.withTransaction
import cz.jenda.tabor2022.*
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.exception.TagCannotBeWritten
import cz.jenda.tabor2022.exception.WritingToTagBelongingAnotherUser
import cz.jenda.tabor2022.fragments.TagInitFragment
import cz.jenda.tabor2022.fragments.dialogs.AddSkillDialog
import cz.jenda.tabor2022.fragments.dialogs.WriteToTagDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

class TagWriteActivity : NfcActivityBase(), WriteToTagDialog.WriteToTagDialogListener {
    lateinit var playerData: Portal.PlayerData
    lateinit var userWithSkills: UserAndSkills
    private var waitingInit: WaitingResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_tag)
        playerData = intent.getSerializableExtra(Extras.DATA_TO_WRITE_ON_TAG) as Portal.PlayerData
        launch(Dispatchers.IO) {
            userWithSkills = PortalApp.instance.db.usersDao().getById(
                playerData.userId.toLong()
            ).first()
            findViewById<TextView>(R.id.text_attach_tag)?.text =
                getString(
                    R.string.write_to_tag,
                    userWithSkills.user.name ?: throw TagCannotBeWritten("Invalid user")
                )
        }

    }

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        runCatching {
            if (tagData?.userId != playerData.userId) {
                val dialog = WriteToTagDialog()
                dialog.show(supportFragmentManager, "")
                val def = CompletableDeferred<Unit>()
                waitingInit = tagData?.let { WaitingResponse(it, def) }
                runCatching { def.await() }.onFailure { throw WritingToTagBelongingAnotherUser("User decline changes") }
            }
            actions.writeToTag(tag, playerData);
        }.onSuccess {
            runOnUiThread {
                Toast.makeText(applicationContext, R.string.data_written, Toast.LENGTH_SHORT).show()
            }
            persistTransaction()
            setResult(Activity.RESULT_OK)
            finish()
        }.onFailure { e ->
            Log.d(Constants.AppTag, "Inserted tag couldn't be written", e)
            runOnUiThread {
                findViewById<TextView>(R.id.text_tag_write_error)?.text =
                    getString(R.string.writing_data_failed)
            }
        }
    }

    private suspend fun persistTransaction() {
        buildTransaction()?.let {
            runCatching {
                val DB = PortalApp.instance.db
                DB.withTransaction {
                    DB.transactionsDao().save(it)

                    if (it.strength != 0) {
                        DB.usersDao().adjustStrength(it.userId, it.strength)
                    }
                    if (it.dexterity != 0) {
                        DB.usersDao().adjustDexterity(it.userId, it.dexterity)
                    }
                    if (it.magic != 0) {
                        DB.usersDao().adjustMagic(it.userId, it.magic)
                    }
                    if (it.bonusPoints != 0) {
                        DB.usersDao().adjustBonusPoints(it.userId, it.bonusPoints)
                    }
                }
            }.onFailure { e ->
                when (e) {
                    is SQLiteConstraintException -> {
                        if (e.message?.contains(Constants.Db.UniqueConflict) == true) {
                            Log.v(Constants.AppTag, "Transaction $it is already imported!")
                            // rethrow all different errors!
                        }
                    }
                }
                throw e
            }
        }
    }

    private fun buildTransaction(): GameTransaction? {
        return userWithSkills.let {
            GameTransaction(
                time = Instant.now(),
                userId = it.user.id,
                deviceId = Constants.AppDeviceId,
                strength = playerData.strength - it.user.strength,
                dexterity = playerData.dexterity - it.user.dexterity,
                magic = playerData.magic - it.user.magic,
                bonusPoints = playerData.bonusPoints - it.user.bonusPoints,
                skillId = 0
            )
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        waitingInit?.def?.complete(Unit)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        waitingInit?.def?.completeExceptionally(WritingToTagBelongingAnotherUser("User decline changes"))
    }

    private data class WaitingResponse(
        val data: Portal.PlayerData,
        val def: CompletableDeferred<Unit>
    )
}