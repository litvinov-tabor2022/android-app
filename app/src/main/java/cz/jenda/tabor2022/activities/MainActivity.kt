package cz.jenda.tabor2022.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.jenda.tabor2022.R
import com.tbruyelle.rxpermissions3.RxPermissions

class MainActivity : AppCompatActivity() {
    private val rxPermissions = RxPermissions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button)?.setOnClickListener {
            val intent = Intent(this, SynchronizeActivity::class.java)
            startActivity(intent)
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
                    Toast.makeText(this, "Povol všechna oprávnění!!!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}