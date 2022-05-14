package cz.jenda.tabor2022

import java.time.Duration

object Constants {
    const val AppTag: String = "PORTAL-APP"

    val PortalConnectionTimeout: Duration = Duration.ofMillis(500)

    const val PortalGroupId: String = "tabor2022"

    const val DbName: String = "portal-db"
}