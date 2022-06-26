package cz.jenda.tabor2022.data.dao

import androidx.room.*
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.UserAndTransactions

@Dao
interface GameTransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun save(gameTransaction: GameTransaction)

    @Transaction
    @Query("SELECT * FROM users WHERE user_id IN (SELECT DISTINCT(user_id) FROM transactions)")
    suspend fun getAll(): List<UserAndTransactions>
}