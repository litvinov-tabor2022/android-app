package cz.jenda.tabor2022.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.jenda.tabor2022.data.model.UserSkillCrossRef

@Dao
interface UserSkillCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(join: UserSkillCrossRef)
}