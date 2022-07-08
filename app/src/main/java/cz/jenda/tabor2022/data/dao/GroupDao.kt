package cz.jenda.tabor2022.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import cz.jenda.tabor2022.data.model.GroupWithUsers
import cz.jenda.tabor2022.data.model.UserWithGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Transaction
    @Query("SELECT * FROM groups ORDER by group_name ASC")
    fun getAll(): Flow<List<GroupWithUsers>>

    @Transaction
    @Query("SELECT * FROM groups where group_id = :id limit 1")
    fun getById(id: Long): Flow<GroupWithUsers>

    @Transaction
    @Query("SELECT * FROM users JOIN groups ON users.group_id = groups.group_id where groups.group_id = :group_id")
    fun members(group_id: Long): Flow<List<UserWithGroup>>
}