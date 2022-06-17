package cz.jenda.tabor2022.activities

import android.app.Activity
import android.database.sqlite.SQLiteConstraintException
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.room.withTransaction
import cz.jenda.tabor2022.*
import cz.jenda.tabor2022.data.GameTransaction
import cz.jenda.tabor2022.data.User
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.exception.TagCannotBeWritten
import kotlinx.coroutines.launch
import java.time.Instant

class TagWriteActivity : NfcActivityBase() {
    lateinit var playerData: Portal.PlayerData
    var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_tag)
        playerData = intent.getSerializableExtra(Extras.DATA_TO_WRITE_ON_TAG) as Portal.PlayerData
        launch {
            user = PortalApp.instance.db.usersDao().getById(
                playerData.userId
            )
            findViewById<TextView>(R.id.text_attach_tag)?.text =
                getString(
                    R.string.write_to_tag,
                    user?.name ?: throw TagCannotBeWritten("Invalid user")
                )

        }

    }

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        runCatching {
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
        return user?.let {
            GameTransaction(
                time = Instant.now(),
                userId = it.id,
                deviceId = "",
                strength = playerData.strength - it.strength,
                dexterity = playerData.dexterity - it.dexterity,
                magic = playerData.magic - it.magic,
                bonusPoints = playerData.bonusPoints - it.bonusPoints,
                skillId = 0
            )
        }
    }
}