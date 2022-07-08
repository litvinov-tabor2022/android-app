package cz.jenda.tabor2022.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.jenda.tabor2022.data.model.UserSkillCrossRef

@Dao
interface UserSkillCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(join: UserSkillCrossRef)

    @Query("INSERT INTO users_and_skills VALUES (:userId, :skillId)")
    fun addSkill(userId: Long, skillId: Long)

    @Query("DELETE FROM users_and_skills WHERE  user_id = :userId AND skill_id = :skillId")
    fun removeSkill(userId: Long, skillId: Long)

}