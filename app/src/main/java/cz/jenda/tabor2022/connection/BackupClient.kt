package cz.jenda.tabor2022.connection

import android.net.Uri
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import cz.jenda.tabor2022.BuildConfig
import cz.jenda.tabor2022.PortalApp
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import kotlin.coroutines.CoroutineContext


interface BackupClient : CoroutineScope {

    companion object BackupClient {
        fun create(rootUrl: Uri): cz.jenda.tabor2022.connection.BackupClient {
            return object : cz.jenda.tabor2022.connection.BackupClient {
                private var job: Job = Job()

                override val coroutineContext: CoroutineContext
                    get() = Dispatchers.IO + job

                private val sardine by lazy {
                    val client = OkHttpSardine()
                    client.setCredentials(
                        BuildConfig.WEBDAV_USERNAME,
                        BuildConfig.WEBDAV_PASSWORD,
                        true
                    )
                    client
                }

                override suspend fun uploadBackup() {
                    PortalApp.instance.db.close()
                    sardine.put("$rootUrl/db-portal.bin", File(PortalApp.instance.dbPath), null)

                }

                override suspend fun downloadLatestBackup(): InputStream? {
                    return sardine.get("$rootUrl/db-portal.bin")
                }

                private suspend fun lastModified(): Instant? {
                    return sardine.list("$rootUrl/db-portal.bin").first().modified.toInstant()
                }

                override suspend fun applyBackup() {
                    val tmpFile = File.createTempFile("backup", ".bin")
                    runCatching {
                        downloadLatestBackup().use { input ->
                            tmpFile.outputStream().use { output ->
                                input?.copyTo(output)
                            }
                        }
                    }.onSuccess {
                        tmpFile.let { sourceFile ->
                            PortalApp.instance.db.close()
                            Files.delete(Paths.get(PortalApp.instance.dbPath))
                            PortalApp.instance.populateDb(sourceFile)
                        }
                    }.onFailure {
                        throw it
                    }
                }
            }
        }
    }

    suspend fun uploadBackup()
    suspend fun applyBackup()
    suspend fun downloadLatestBackup(): InputStream?
}