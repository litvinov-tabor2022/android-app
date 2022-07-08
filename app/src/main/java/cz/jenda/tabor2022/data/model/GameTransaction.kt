package cz.jenda.tabor2022.data.model

import androidx.room.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["user_id"]), Index(value = ["time", "device_id"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GameTransaction(
    @ColumnInfo(name = "time")
    val time: Instant,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "strength")
    val strength: Int,
    @ColumnInfo(name = "dexterity")
    val dexterity: Int,
    @ColumnInfo(name = "magic")
    val magic: Int,
    @ColumnInfo(name = "bonus_points")
    val bonusPoints: Int,
    @ColumnInfo(name = "skill")
    val skillId: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}