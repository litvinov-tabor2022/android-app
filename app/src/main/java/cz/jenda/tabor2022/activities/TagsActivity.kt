package cz.jenda.tabor2022.activities

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.data.proto.playerData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class TagsActivity : NfcActivityBase() {
    private var lastTag: MifareClassic? = null
    private var lastPlayerData: Portal.PlayerData? = null

    private val waitingInit: AtomicReference<WaitingInit?> = AtomicReference(null)

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        lastTag = tag

        val waitingInit = this.waitingInit.get()
        if (waitingInit != null) {
            Log.d(Constants.AppTag, "Will initialize tag: ${waitingInit.data}")
            runCatching { actions.writeToTag(tag, waitingInit.data) }
                .onSuccess { waitingInit.def.complete(Unit) }
                .onFailure { waitingInit.def.completeExceptionally(it) }
            Log.v(Constants.AppTag, "nulling waitingInit")
            this.waitingInit.compareAndSet(waitingInit, null)
            runOnUiThread { showMsg("Tag inicializovan!") }
            return
        }
        // else

        if (tagData != null) {
            lastPlayerData = tagData

            val nd = tagData.toBuilder().setBonusPoints(tagData.bonusPoints + 1).build()
            launch {
                runCatching { actions.writeToTag(tag, nd) }
                    .onSuccess { Log.i(Constants.AppTag, "Data written to tag") }
                    .onFailure { e -> Log.e(Constants.AppTag, "Couldn't write to tag!", e) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        findViewById<Button>(R.id.buttonInitTag).setOnClickListener {
            val name = findViewById<EditText>(R.id.personName).text.toString()

            if (name.isEmpty()) {
                runOnUiThread { Toast.makeText(this, R.string.err_missing_name, Toast.LENGTH_LONG).show() }
            } else {
                launch { initTag(name) }
            }
        }
    }

    private suspend fun initTag(name: String) {
        // TODO handle DB

        val player = playerData {
            userId = Random.nextInt(1, 998)
            strength = 10
            dexterity = 10
            magic = 10
            bonusPoints = 0
            secret = Constants.TagSecret
        }

        val def = CompletableDeferred<Unit>()
        waitingInit.set(WaitingInit(player, def))
        runOnUiThread {
            Toast.makeText(this, R.string.tag_init_insert_tag, Toast.LENGTH_SHORT).show()
            showMsg("Cekam na tag; init $name")
        }

        runCatching { def.await() }
            .onSuccess { runOnUiThread { Toast.makeText(this, R.string.tag_init_done, Toast.LENGTH_SHORT).show() } }
            .onFailure { e ->
                runOnUiThread {
                    Toast.makeText(this, R.string.tag_init_failure, Toast.LENGTH_SHORT).show()
                    showMsg("Tag NEBYL inicializovan!")
                }
                Log.e(Constants.AppTag, "Couldn't initialize the tag!", e)
            }
    }

    private fun showMsg(text: String) {
        findViewById<TextView>(R.id.textView).text = text
    }
}

private data class WaitingInit(val data: Portal.PlayerData, val def: CompletableDeferred<Unit>)