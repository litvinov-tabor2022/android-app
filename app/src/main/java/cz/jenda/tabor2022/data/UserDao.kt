package cz.jenda.tabor2022.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun create(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(user: User)

    @Query("SELECT * FROM users ORDER by name ASC")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users where id = :id limit 1")
    suspend fun getById(id: Int): User

    @Query("UPDATE users set strength = strength + :delta where id = :userId")
    suspend fun adjustStrength(userId: Int, delta: Int)

    @Query("UPDATE users set dexterity = dexterity + :delta where id = :userId")
    suspend fun adjustDexterity(userId: Int, delta: Int)

    @Query("UPDATE users set magic = magic + :delta where id = :userId")
    suspend fun adjustMagic(userId: Int, delta: Int)

    @Query("UPDATE users set bonus_points = bonus_points + :delta where id = :userId")
    suspend fun adjustBonusPoints(userId: Int, delta: Int)
}