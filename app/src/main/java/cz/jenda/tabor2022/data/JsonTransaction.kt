package cz.jenda.tabor2022.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class JsonTransaction(
    @JsonProperty("time") val time: Instant,
    @JsonProperty("device_id") val deviceId: String,
    @JsonProperty("user_id") val userId: Int,
    @JsonProperty("strength") val strength: Int = 0,
    @JsonProperty("dexterity") val dexterity: Int = 0,
    @JsonProperty("magic") val magic: Int = 0,
    @JsonProperty("bonus_points") val bonusPoints: Int = 0
)
