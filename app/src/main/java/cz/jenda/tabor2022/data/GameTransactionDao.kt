package cz.jenda.tabor2022.data

import androidx.room.*

@Dao
interface GameTransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun save(gameTransaction: GameTransaction)

    @Transaction
    @Query("SELECT * FROM users WHERE id IN (SELECT DISTINCT(user_id) FROM transactions)")
    suspend fun getAll(): List<UserAndTransactions>
}