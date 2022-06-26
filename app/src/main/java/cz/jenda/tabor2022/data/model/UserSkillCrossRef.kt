package cz.jenda.tabor2022.data.model

import androidx.room.*

@Entity(
    tableName = "users_and_skills",
    primaryKeys = ["user_id", "skill_id"],
)
data class UserSkillCrossRef(
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "skill_id")
    val skillId: Long,
)