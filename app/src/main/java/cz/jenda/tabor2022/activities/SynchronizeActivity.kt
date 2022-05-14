package cz.jenda.tabor2022.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.jenda.tabor2022.PortalActions
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.PortalAdapter
import cz.jenda.tabor2022.connection.PortalConnection
import cz.jenda.tabor2022.connection.PortalsConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SynchronizeActivity : AppCompatActivity(), CoroutineScope {

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

        findViewById<Button>(R.id.buttonSyncTime).setOnClickListener {
            launch { PortalActions.synchronizeTime(this@SynchronizeActivity, portals) }
        }

        findViewById<Button>(R.id.buttonDownloadData).setOnClickListener {
            launch { PortalActions.synchronizeData(this@SynchronizeActivity, portals) }
        }

        findViewById<Button>(R.id.buttonDeleteData).setOnClickListener {
            launch { PortalActions.deleteData(this@SynchronizeActivity, portals) }
        }
    }

    private fun refresh(portals: Set<PortalConnection>) {
        this.portals = portals

        launch(Dispatchers.Main) {
            list.adapter = PortalAdapter(this@SynchronizeActivity, portals.toList())
        }
    }

    override fun onStart() {
        super.onStart()
        rescan()
    }

    private fun rescan() {
        launch {
            refresh(portalsConnector.rescan())
        }
    }
}