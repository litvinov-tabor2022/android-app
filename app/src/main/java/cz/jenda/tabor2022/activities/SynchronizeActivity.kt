package cz.jenda.tabor2022.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.PortalAdapter
import cz.jenda.tabor2022.connection.PortalConnection
import cz.jenda.tabor2022.connection.PortalsConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class SynchronizeActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var portalsConnector: PortalsConnector
    private lateinit var list: ListView

    private var portals = emptySet<PortalConnection>()

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronize)

        list = findViewById(R.id.portals_list)
        portalsConnector = PortalsConnector()

        findViewById<FloatingActionButton>(R.id.rescanButton).setOnClickListener {
            rescan()
        }
    }

    private fun refresh(portals: Set<PortalConnection>) {
        this.portals = portals

        async(Dispatchers.Main) {
            list.adapter = PortalAdapter(this@SynchronizeActivity, portals.toList())
        }.start()
    }

    override fun onStart() {
        super.onStart()

        rescan()

        findViewById<Button>(R.id.button5).setOnClickListener {
            val currentTime = System.currentTimeMillis() / 1000

            Log.w(Constants.AppTag, "Synchronizing time of all connected portals to $currentTime")

            async {
                val failures = mutableListOf<String>()

                for (conn in portals) {
                    runCatching { conn.client.updateTime(currentTime) }.onFailure { e ->
                        failures += conn.deviceId
                        Log.w(Constants.AppTag, "Could not update time for $conn", e)
                    }
                }

                async(Dispatchers.Main) {
                    if (failures.size == 0) {
                        Toast.makeText(this@SynchronizeActivity, "OK!!!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@SynchronizeActivity,
                            "Failures: ${failures.joinToString()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.start()
            }.start()
        }
    }

    private fun rescan() {
        async {
            val portals = portalsConnector.rescan()
            refresh(portals)
        }.start()
    }
}