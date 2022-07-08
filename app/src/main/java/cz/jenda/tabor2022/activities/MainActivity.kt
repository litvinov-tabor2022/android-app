package cz.jenda.tabor2022.activities

import android.Manifest
import android.app.UiModeManager
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.tbruyelle.rxpermissions3.RxPermissions
import cz.jenda.tabor2022.*
import cz.jenda.tabor2022.connection.BackupClient
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private val rxPermissions = RxPermissions(this)
    private lateinit var backupClient: BackupClient
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val actions = TagActions(this) { _, tagData ->
                val intent = Intent(applicationContext, UserDetailActivity::class.java)
                if (tagData != null) {
                    intent.putExtra(Extras.USER_EXTRA, tagData.userId.toLong())
                    startActivity(intent)
                }
            }
            launch { actions.handleIntent(intent) }
        }

        setContentView(R.layout.activity_main)

        BackupClient.create(
            Uri.parse(BuildConfig.WEBDAV_ENDPOINT)
        ).also { backupClient = it }

        findViewById<Button>(R.id.button_sync)?.setOnClickListener {
            val intent = Intent(this, SynchronizeActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_batch)?.setOnClickListener {
            val intent = Intent(this, TagsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_users)?.setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_backup)?.setOnClickListener {
            uploadBackup()
        }

        findViewById<Button>(R.id.button_load_from_backup)?.setOnClickListener {
            downloadBackup()
        }

        rxPermissions.setLogging(true)

        rxPermissions
            .request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
            )
            .subscribe { granted: Boolean ->
                if (!granted) {
                    // At least one permission is denied
                    Toast.makeText(this, R.string.allow_all_permissions, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun downloadBackup() {
        launch {
            runCatching {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        R.string.backup_loading,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                backupClient.applyBackup()
            }.onSuccess {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        R.string.backup_loaded,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }.onFailure {
                if (it is java.net.UnknownHostException || it is java.net.ConnectException) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.check_network_connection,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.backup_load_failed,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                Log.e(Constants.AppTag, it.javaClass.kotlin.toString())
            }
        }
    }

    private fun uploadBackup() {
        runOnUiThread {
            Toast.makeText(applicationContext, R.string.backup_started, Toast.LENGTH_SHORT)
                .show()
        }
        launch {
            runCatching { backupClient.uploadBackup() }.onSuccess {
                runOnUiThread {
                    Toast.makeText(applicationContext, R.string.backup_done, Toast.LENGTH_SHORT)
                        .show()
                }
            }.onFailure {
                if (it is java.net.UnknownHostException || it is java.net.ConnectException) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.check_network_connection,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.backup_failed,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                Log.e(Constants.AppTag, it.toString())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(Constants.AppTag, "Data from all portals deleted!")
        Toast.makeText(applicationContext, "Opened from intent", Toast.LENGTH_SHORT).show()
//        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
//            if (tagFromIntent?.techList?.contains(MifareClassic::class.qualifiedName) == true) {
        Intent(this, UserDetailActivity::class.java).also {
            startActivity(it)
        }
//            }
//        }
    }
}