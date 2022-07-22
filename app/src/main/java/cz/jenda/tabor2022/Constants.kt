package cz.jenda.tabor2022

import java.time.Duration
import java.util.prefs.Preferences

object Constants {
    const val AppTag: String = "PORTAL-APP"

    val PortalConnectionTimeout: Duration = Duration.ofMillis(500)

    const val PortalGroupId: String = "tabor2022"
    const val AppDeviceId: String = "android-app"
    const val TagSecret: String = "\$1\$gJvI"

    const val DbName: String = "portal-db"
    const val PreferencesName: String = "portal-preferences"

    const val TagDataSizeLimit: Int = 64 * 4

    val MifareKey1: ByteArray = byteArrayOf(
        0xFF.toByte(),
        0xFF.toByte(),
        0xFF.toByte(),
        0xFF.toByte(),
        0xFF.toByte(),
        0xFF.toByte()
    )
    val MifareKey2: ByteArray = byteArrayOf(
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte()
    )

    object Db {
        const val UniqueConflict: String =
            "(code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])"
    }

    object Prefs {
        const val IpRange = "ip-range"
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}