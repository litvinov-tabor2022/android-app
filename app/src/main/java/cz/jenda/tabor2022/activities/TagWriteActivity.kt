package cz.jenda.tabor2022.activities

import android.app.Activity
import android.database.sqlite.SQLiteConstraintException
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cz.jenda.tabor2022.*
import cz.jenda.tabor2022.data.Helpers.execute
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.exception.TagCannotBeWritten
import cz.jenda.tabor2022.exception.WritingToTagBelongingAnotherUser
import cz.jenda.tabor2022.fragments.dialogs.WriteToTagDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class TagWriteActivity : NfcActivityBase(), WriteToTagDialog.WriteToTagDialogListener {
    lateinit var playerData: Portal.PlayerData
    lateinit var userWithGroup: UserWithGroup
    private var waitingInit: WaitingResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_tag)
        playerData = intent.getSerializableExtra(Extras.DATA_TO_WRITE_ON_TAG) as Portal.PlayerData
        launch(Dispatchers.IO) {
            userWithGroup = PortalApp.instance.db.usersDao().getById(
                playerData.userId.toLong()
            ).first()
            findViewById<TextView>(R.id.text_attach_tag)?.text =
                getString(
                    R.string.write_to_tag,
                    userWithGroup.userWithSkills.user.name
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
            actions.writeToTag(tag, playerData)
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
        buildTransaction().forEach {
            runCatching {
                it.execute()
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

    private fun buildTransaction(): List<GameTransaction> {
        var iterationCounter = 0
        val transactions = userWithGroup.let {
            val user = it.userWithSkills.user
            val deltaStrength = playerData.strength - user.strength
            val deltaDexterity = playerData.dexterity - user.dexterity
            val deltaMagic = playerData.magic - user.magic
            val deltaBonusPoints = playerData.bonusPoints - user.bonusPoints
            if (deltaStrength != 0 || deltaDexterity != 0 || deltaMagic != 0 || deltaBonusPoints != 0) {
                mutableListOf(
                    GameTransaction(
                        time = Instant.now().toKotlinInstant()
                            .plus(iterationCounter++, DateTimeUnit.MILLISECOND),
                        userId = user.id,
                        deviceId = Constants.AppDeviceId,
                        strength = deltaStrength,
                        dexterity = deltaDexterity,
                        magic = deltaMagic,
                        bonusPoints = deltaBonusPoints,
                        skillId = 0
                    )
                )
            } else {
                mutableListOf()
            }
        }

        val alreadyOwnedSkills = userWithGroup.userWithSkills.skills.map { it.id.toInt() }
        val playerDataSkillsId = playerData.skillsList.map { it.number }

        playerDataSkillsId.forEach {
            if (!alreadyOwnedSkills.contains(it)) {
                transactions.add(
                    GameTransaction(
                        time = Instant.now().toKotlinInstant()
                            .plus(iterationCounter++, DateTimeUnit.MILLISECOND),
                        userId = userWithGroup.userWithSkills.user.id,
                        deviceId = Constants.AppDeviceId,
                        strength = 0,
                        dexterity = 0,
                        magic = 0,
                        bonusPoints = 0,
                        skillId = it
                    )
                )
            }
        }

        alreadyOwnedSkills.forEach {
            if (!playerDataSkillsId.contains(it)) {
                transactions.add(
                    GameTransaction(
                        time = Instant.now().toKotlinInstant()
                            .plus(iterationCounter++, DateTimeUnit.MILLISECOND),
                        userId = userWithGroup.userWithSkills.user.id,
                        deviceId = Constants.AppDeviceId,
                        strength = 0,
                        dexterity = 0,
                        magic = 0,
                        bonusPoints = 0,
                        skillId = -it
                    )
                )

            }
        }
        Log.i(Constants.AppTag, "Transactions to be executed after success write: $transactions ")
        return transactions
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