package cz.jenda.tabor2022.data.dao

import androidx.room.*
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserAndSkills
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun create(skill: Skill): Long

    @Delete
    suspend fun remove(skill: Skill)

    @Query("SELECT * FROM skills ORDER by name ASC")
    fun getAll(): Flow<List<Skill>>

    @Query("SELECT * FROM skills where skill_id = :id limit 1")
    suspend fun getById(id: Int): Skill
}