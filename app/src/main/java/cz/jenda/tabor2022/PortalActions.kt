package cz.jenda.tabor2022

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import cz.jenda.tabor2022.connection.PortalConnection
import cz.jenda.tabor2022.data.Helpers.execute
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.datetime.toKotlinInstant
import kotlin.coroutines.CoroutineContext

object PortalActions : CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val jsonMapper = ObjectMapper()
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

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

        // TODO delete
        PortalApp.instance.db.usersDao().save(User(137, "Jenda", 10, 10, 10, 0, null))

        val namesMapping = jsonMapper.writeValueAsBytes(
            PortalApp.instance.db.usersDao().getAll().first().map { u ->
                JsonNameMapping(u.userWithSkills.user.id, u.userWithSkills.user.name, 1) // TODO group from user
            })

        for (conn in portals) {
            runCatching { synchronizeTransactions(conn) }.onFailure { e ->
                failures += conn.deviceId
                Log.w(Constants.AppTag, "Could not synchronize data from $conn", e)
            }.onSuccess {
                JsonStringEncoder.getInstance()
                    .runCatching { updateNamesMapping(conn, namesMapping) }.onFailure { e ->
                        failures += conn.deviceId
                        Log.w(Constants.AppTag, "Could not synchronize names mapping to $conn", e)
                    }
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

    private suspend fun updateNamesMapping(portal: PortalConnection, data: ByteArray) {
        Log.d(Constants.AppTag, "Synchronizing names mapping to $portal")

        portal.client.updateNamesMapping(data)
    }

    private suspend fun synchronizeTransactions(portal: PortalConnection) {
        Log.d(Constants.AppTag, "Synchronizing data from $portal")

        portal.client.fetchData().collect { transaction ->
            runCatching {
                Log.v(Constants.AppTag, "Transaction from $portal: $transaction")
                GameTransaction(
                    time = transaction.time.toKotlinInstant(),
                    userId = transaction.userId,
                    deviceId = transaction.deviceId,
                    strength = transaction.strength,
                    dexterity = transaction.dexterity,
                    magic = transaction.magic,
                    bonusPoints = transaction.bonusPoints,
                    skillId = transaction.skill,
                ).execute()
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

data class JsonNameMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("group")
    val group: Int,
)