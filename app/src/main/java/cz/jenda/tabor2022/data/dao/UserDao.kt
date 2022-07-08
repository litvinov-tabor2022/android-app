package cz.jenda.tabor2022.data.dao

import androidx.room.*
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.model.UserWithSkills
import cz.jenda.tabor2022.data.model.UserWithGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun create(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(user: User)

    @Delete
    suspend fun remove(user: User)

    @Transaction
    @Query("SELECT * FROM users ORDER by name ASC")
    fun getAll(): Flow<List<UserWithGroup>>

    @Transaction
    @Query("SELECT * FROM users ORDER by name ASC")
    suspend fun getAllWithSkills(): List<UserWithSkills>

//    @Query("SELECT * FROM users where user_id = :id limit 1")
//    suspend fun getById(id: Long): UserAndSkills

    @Transaction
    @Query("SELECT * FROM users where user_id = :id limit 1")
    fun getById(id: Long): Flow<UserWithGroup>

    @Query("UPDATE users set strength = strength + :delta where user_id = :userId")
    suspend fun adjustStrength(userId: Long, delta: Int)

    @Query("UPDATE users set dexterity = dexterity + :delta where user_id = :userId")
    suspend fun adjustDexterity(userId: Long, delta: Int)

    @Query("UPDATE users set magic = magic + :delta where user_id = :userId")
    suspend fun adjustMagic(userId: Long, delta: Int)

    @Query("UPDATE users set bonus_points = bonus_points + :delta where user_id = :userId")
    suspend fun adjustBonusPoints(userId: Long, delta: Int)

//    @Query("SELECT * FROM users WHERE id = :id ORDER BY name ASC")
//    suspend fun getSkillsById(id: Int): List<Skill>
}