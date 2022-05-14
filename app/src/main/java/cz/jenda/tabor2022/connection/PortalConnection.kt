package cz.jenda.tabor2022.connection

import cz.jenda.tabor2022.Constants
import kotlinx.coroutines.coroutineScope
import java.net.Inet4Address

class PortalConnection(val client: PortalClient, val ip: Inet4Address, val deviceId: String) {

    companion object {
        suspend fun create(ip: Inet4Address): Result<PortalConnection> {
            val client: PortalClient = PortalClient.create("http://${ip}")

            return try {
                val status = client.getStatus()
                if (status.deviceGroup != Constants.PortalGroupId) return Result.failure(
                    RuntimeException("device_group doesn't match")
                )

                Result.success(PortalConnection(client, ip, status.deviceId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun available(): Boolean = coroutineScope {
        try {
            client.getStatus()
            true
        } catch (e: Exception) {
            false
        }
    }


    override fun equals(other: Any?): Boolean {
        return if (other is PortalConnection) {
            other.ip == this.ip && other.deviceId == this.deviceId
        } else false
    }

    override fun toString(): String {
        return "PortalConnection(ip = $ip, deviceId = '$deviceId')"
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + deviceId.hashCode()
        return result
    }
}