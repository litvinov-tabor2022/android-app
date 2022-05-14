package cz.jenda.tabor2022.activities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.data.proto.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class NfcActivityBase : AppCompatActivity(), CoroutineScope {

    protected var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private var adapter: NfcAdapter? = null
    protected val actions: TagActions = TagActions(this) { tag, data -> onTagRead(tag, data) }

    protected abstract suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // forward the intent to TagActions
        launch { actions.handleIntent(intent) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initNfcAdapter()
    }

    override fun onPause() {
        disableNfcForegroundDispatch()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e(Constants.AppTag, "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e(Constants.AppTag, "Error disabling NFC foreground dispatch", ex)
        }
    }

    private fun initNfcAdapter() {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        adapter = nfcManager.defaultAdapter
    }
}

