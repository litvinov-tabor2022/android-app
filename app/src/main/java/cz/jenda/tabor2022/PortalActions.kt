package cz.jenda.tabor2022

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.widget.Toast
import androidx.room.withTransaction
import cz.jenda.tabor2022.connection.PortalConnection
import cz.jenda.tabor2022.data.GameTransaction
import cz.jenda.tabor2022.data.User
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext

object PortalActions : CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    suspend fun synchronizeTime(ctx: Context, portals: Set<PortalConnection>) {
        val currentTime = System.currentTimeMillis() / 1000

        Log.i(Constants.AppTag, "Synchronizing time of all connected portals to $currentTime")

        val failures = mutableListOf<String>()

        for (conn in portals) {
            runCatching { conn.client.updateTime(currentTime) }.onFailure { e ->
                failures += conn.deviceId
                Log.w(Constants.AppTag, "Could not update time for $conn", e)
            }
        }

        coroutineScope {
            launch(Dispatchers.Main) {
                if (failures.size == 0) {
                    Toast.makeText(ctx, R.string.time_updated, Toast.LENGTH_SHORT).show()
                    Log.i(Constants.AppTag, "Time for all portals synchronized to $currentTime")
                } else {
                    Toast.makeText(
                        ctx,
                        "Failures: ${failures.joinToString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    suspend fun synchronizeData(ctx: Context, portals: Set<PortalConnection>) {
        Log.i(Constants.AppTag, "Synchronizing data from all connected portals")

        val failures = mutableListOf<String>()

        PortalApp.instance.db.usersDao().save(User(137, "Jenda", 10, 10, 10, 0))

        for (conn in portals) {
            runCatching { synchronizeData(conn) }.onFailure { e ->
                failures += conn.deviceId
                Log.w(Constants.AppTag, "Could not synchronize data from $conn", e)
            }
        }

        coroutineScope {
            launch(Dispatchers.Main) {
                if (failures.size == 0) {
                    Log.i(Constants.AppTag, "Data from all portals synchronized!")
                    Toast.makeText(ctx, R.string.data_imported, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        ctx,
                        "Failures: ${failures.joinToString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    suspend fun deleteData(ctx: Context, portals: Set<PortalConnection>) {
        Log.i(Constants.AppTag, "Deleting data from all portals")

        val failures = mutableListOf<String>()

        for (conn in portals) {
            runCatching { conn.client.deleteData() }.onFailure { e ->
                failures += conn.deviceId
                Log.w(Constants.AppTag, "Could not delete data from $conn", e)
            }
        }

        coroutineScope {
            launch(Dispatchers.Main) {
                if (failures.size == 0) {
                    Log.i(Constants.AppTag, "Data from all portals deleted!")
                    Toast.makeText(ctx, R.string.data_deleted, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        ctx,
                        "Failures: ${failures.joinToString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun synchronizeData(portal: PortalConnection) {
        Log.d(Constants.AppTag, "Synchronizing data from $portal")

        portal.client.fetchData().collect { transaction ->
            runCatching {
                val DB = PortalApp.instance.db
                val userId = transaction.userId

                Log.v(Constants.AppTag, "Transaction from $portal: $transaction")

                DB.withTransaction {
                    DB.transactionsDao().save(
                        GameTransaction(
                            time = transaction.time,
                            userId = userId,
                            deviceId = transaction.deviceId,
                            strength = transaction.strength,
                            dexterity = transaction.dexterity,
                            magic = transaction.magic,
                            bonusPoints = transaction.bonusPoints,
                        )
                    )

                    if (transaction.strength != 0) {
                        DB.usersDao().adjustStrength(userId, transaction.strength)
                    }
                    if (transaction.dexterity != 0) {
                        DB.usersDao().adjustDexterity(userId, transaction.dexterity)
                    }
                    if (transaction.magic != 0) {
                        DB.usersDao().adjustMagic(userId, transaction.magic)
                    }
                    if (transaction.bonusPoints != 0) {
                        DB.usersDao().adjustBonusPoints(userId, transaction.bonusPoints)
                    }
                }
            }.onFailure { e ->
                when (e) {
                    is SQLiteConstraintException -> {
                        if (e.message?.contains(Constants.Db.UniqueConflict) == true) {
                            Log.v(Constants.AppTag, "Transaction $transaction is already imported!")
                        } else {
                            // rethrow all different errors!
                            throw e
                        }
                    }
                }
            }
        }
    }
}