package cz.jenda.tabor2022.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "skills", indices = [Index("name", unique = true)])
data class Skill(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "skill_id")
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "min_strength")
    val strength: Int,
    @ColumnInfo(name = "min_dexterity")
    val dexterity: Int,
    @ColumnInfo(name = "min_magic")
    val magic: Int,
    @ColumnInfo(name = "price")
    val price: Int,
)