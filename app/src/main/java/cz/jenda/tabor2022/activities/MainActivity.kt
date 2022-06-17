package cz.jenda.tabor2022.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import cz.jenda.tabor2022.BuildConfig
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.connection.BackupClient
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private val rxPermissions = RxPermissions(this)
    private lateinit var webDavClient: BackupClient

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BackupClient.create(
            Uri.parse(BuildConfig.WEBDAV_ENDPOINT)
        ).also { webDavClient = it }

        findViewById<Button>(R.id.button_sync)?.setOnClickListener {
            val intent = Intent(this, SynchronizeActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_tags)?.setOnClickListener {
            val intent = Intent(this, TagsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_users)?.setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_backup)?.setOnClickListener {
            runOnUiThread {
                Toast.makeText(applicationContext, R.string.backup_started, Toast.LENGTH_SHORT)
                    .show()
            }
            launch {
                runCatching { webDavClient.uploadBackup() }.onSuccess {
                    runOnUiThread {
                        Toast.makeText(applicationContext, R.string.backup_done, Toast.LENGTH_SHORT)
                            .show()
                    }
                }.onFailure {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.backup_failed,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Log.e(Constants.AppTag, it.toString())
                }
            }
        }

        findViewById<Button>(R.id.button_load_from_backup)?.setOnClickListener {
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
                    webDavClient.applyBackup()
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
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            R.string.backup_load_failed,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Log.e(Constants.AppTag, it.toString())
                }
            }
        }

        rxPermissions.setLogging(true);

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
                    Toast.makeText(this, R.string.allow_all_permissions, Toast.LENGTH_SHORT).show()
                }
            }
    }
}