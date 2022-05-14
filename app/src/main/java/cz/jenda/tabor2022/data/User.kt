package cz.jenda.tabor2022.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "strength")
    val strength: Int,
    @ColumnInfo(name = "dexterity")
    val dexterity: Int,
    @ColumnInfo(name = "magic")
    val magic: Int,
    @ColumnInfo(name = "bonus_points")
    val bonusPoints: Int
)
