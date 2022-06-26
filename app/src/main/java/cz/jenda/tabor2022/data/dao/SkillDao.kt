package cz.jenda.tabor2022.data.dao

import androidx.room.*
import cz.jenda.tabor2022.data.model.Skill

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun create(skill: Skill): Long

    @Delete
    suspend fun remove(skill: Skill)

    @Query("SELECT * FROM skills ORDER by name ASC")
    suspend fun getAll(): List<Skill>

    @Query("SELECT * FROM skills where skill_id = :id limit 1")
    suspend fun getById(id: Int): Skill
}