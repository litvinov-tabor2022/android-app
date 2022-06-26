package cz.jenda.tabor2022.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index("name", unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val id: Long,
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
