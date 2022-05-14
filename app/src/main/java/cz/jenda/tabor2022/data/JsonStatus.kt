package cz.jenda.tabor2022.data

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonStatus(
    @JsonProperty("status")
    val status: String,
    @JsonProperty("device_group")
    val deviceGroup: String,
    @JsonProperty("device_id")
    val deviceId: String,
)
