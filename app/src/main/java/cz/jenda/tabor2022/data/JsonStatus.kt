package cz.jenda.tabor2022.data

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonStatus(
    @JsonProperty("status")
    val status: String,
    @JsonProperty("device_group")
    val device_group: String,
    @JsonProperty("device_id")
    val device_id: String,
)
