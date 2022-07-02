package cz.jenda.tabor2022.connection

import android.util.Log
import cz.jenda.tabor2022.Constants
import kotlinx.coroutines.*
import java.net.Inet4Address
import kotlin.coroutines.CoroutineContext

class PortalsConnector : CoroutineScope {

    private var portals: Set<PortalConnection> = emptySet()

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    suspend fun rescan(): Set<PortalConnection> {
        Log.i(Constants.AppTag, "Starting scanning for portals...")

        val newPortals = scan().toMutableSet()

        var lost = portals.minus(newPortals)

        // given the lost ones one more chance
        newPortals += rescanLost(lost)

        val found = newPortals.minus(portals)
        lost = portals.minus(newPortals)

        if (found.isNotEmpty() || lost.isNotEmpty()) {
            portals = newPortals
            Log.i(Constants.AppTag, "Found portals: $found")
            Log.i(Constants.AppTag, "Lost portals: $lost")
        }

        return portals
    }

    suspend fun rescanConnected(): Set<PortalConnection> {
        Log.v(Constants.AppTag, "Rescanning connected portals...")

        val newPortals = portals.filter { conn ->
            Log.v(Constants.AppTag, "Rescanning connected portal: $conn")
            conn.available()
        }.toHashSet()

        val lost = portals.minus(newPortals)

        if (lost.isNotEmpty()) {
            portals = newPortals
            Log.i(Constants.AppTag, "Lost portals: $lost")
        }

        return portals
    }

    private suspend fun scan(): Set<PortalConnection> {
        return coroutineScope {
            (2..254).map { i ->
                async {
                    // TODO: make range of IPs auto-discoverable
                    val ip = Inet4Address.getByAddress(
                        byteArrayOf(192.toByte(), 168.toByte(), 112.toByte(), i.toByte())
                    )

                    val existing = portals.find { it.ip == ip }

                    // are we able to reuse existing connection?
                    if (existing != null && existing.available()) {
                        existing
                    } else {
                        // OK, create new connection
                        PortalConnection.create(ip as Inet4Address).fold(
                            onSuccess = { c -> c },
                            onFailure = { null } // not available, we can't do anything
                        )
                    }
                }
            }.awaitAll().filterNotNull().toHashSet()
        }
    }

    private suspend fun rescanLost(lost: Set<PortalConnection>): Set<PortalConnection> {
        return lost.filter { conn ->
            Log.d(Constants.AppTag, "Rescanning lost connection: $conn")
            conn.available()
        }.toHashSet()
    }

}