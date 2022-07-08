package cz.jenda.tabor2022.data.model

import androidx.room.*

@Entity(tableName = "groups", indices = [Index("group_name", unique = true)])
data class Group(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    val id: Long,
    @ColumnInfo(name = "group_name")
    val name: String
)

data class GroupStatistics(
    val group: Group,
    val members: List<User>,
    val rank: Int
) {
    fun noMembers(): Int {
        return members.size
    }

    fun totalPoints(): Int {
        return members.fold(0) { acc, user -> acc + user.totalPoints() }
    }
}

data class GroupWithUsers(
    @Embedded val group: Group,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "group_id"
    )
    val members: List<User>
) {
    fun totalPoints(): Int {
        return members.fold(0) { acc, user -> acc + user.totalPoints() }
    }
}

data class UserWithGroup(
    @Embedded val userWithSkills: UserWithSkills,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "group_id"
    )
    val group: Group?
)