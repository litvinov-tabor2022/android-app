package cz.jenda.tabor2022.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalActions
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.PortalAdapter
import cz.jenda.tabor2022.connection.PortalConnection
import cz.jenda.tabor2022.connection.PortalsConnector
import cz.jenda.tabor2022.fragments.dialogs.DeleteDataConfirmationDialog
import cz.jenda.tabor2022.fragments.dialogs.NumberPickDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SynchronizeActivity : AppCompatActivity(), DeleteDataConfirmationDialog.DeleteDataConfirmationDialogListener,
    NumberPickDialog.NumberPickDialogListener, CoroutineScope {

    private lateinit var portalsConnector: PortalsConnector
    private lateinit var list: ListView

    private var portals = emptySet<PortalConnection>()

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronize)

        list = findViewById(R.id.portals_list)
        portalsConnector = PortalsConnector()

        findViewById<FloatingActionButton>(R.id.buttonRescan).setOnClickListener {
            rescan()
        }

        findViewById<Button>(R.id.buttonIpRange).setOnClickListener {
            val dialog = NumberPickDialog()
            dialog.show(supportFragmentManager, "")
        }

        findViewById<Button>(R.id.buttonSynchronizeData).setOnClickListener {
            launch { PortalActions.synchronizeData(this@SynchronizeActivity, portals) }
        }

        findViewById<Button>(R.id.buttonDeleteData).setOnClickListener {
            val dialog = DeleteDataConfirmationDialog()
            dialog.show(supportFragmentManager, "")
        }
    }

    private fun refresh(portals: Set<PortalConnection>) {
        this.portals = portals

        launch(Dispatchers.Main) {
            list.adapter = PortalAdapter(this@SynchronizeActivity, portals.toList())
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is DeleteDataConfirmationDialog) {
            launch { PortalActions.deleteData(this@SynchronizeActivity, portals) }
        } else {
            Log.e(Constants.AppTag, "Unknown dialog")
        }
    }

    override fun onValueChosen(value: Int) {
        Log.d(Constants.AppTag, "Updating IP range: $value")
        PortalApp.instance.sharedPrefs.edit().putInt(Constants.Prefs.IpRange, value).commit()
        rescan()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // do nothing
    }

    override fun onCancelled() {
        // do nothing
    }

    override fun onStart() {
        super.onStart()
        rescan()
        // check periodically for disconnection
        kotlin.concurrent.timer("rescanConnectedPortals", false, 3000, 3000) {
            launch {
                refresh(portalsConnector.rescanConnected())
            }
        }
    }

    private fun rescan() {
        val ipRange = PortalApp.instance.sharedPrefs.getInt(Constants.Prefs.IpRange, 0)

        launch {
            refresh(portalsConnector.rescan(ipRange))
        }
    }
}