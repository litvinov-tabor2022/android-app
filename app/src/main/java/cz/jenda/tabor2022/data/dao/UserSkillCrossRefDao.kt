package cz.jenda.tabor2022.data.dao

import androidx.room.*
import cz.jenda.tabor2022.data.model.UserSkillCrossRef

@Dao
interface UserSkillCrossRefDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(join: UserSkillCrossRef)

    @Transaction
    @Query("INSERT INTO users_and_skills VALUES (:userId, :skillId)")
    fun addSkill(userId: Long, skillId: Long)

    @Transaction
    @Query("DELETE FROM users_and_skills WHERE user_id = :userId AND skill_id = :skillId")
    fun removeSkill(userId: Long, skillId: Long)

    @Transaction
    @Query("DELETE FROM users_and_skills WHERE user_id = :userId")
    fun removeAllSkills(userId: Long)

}