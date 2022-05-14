package cz.jenda.tabor2022.data

import java.time.Instant

data class JsonTransaction(
    val time: Instant,
    val device_id: String,
    val user_id: UInt,
    val strength: Int,
    val dexterity: Int,
    val magic: Int,
    val bonus_points: Int
)
