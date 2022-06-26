package cz.jenda.tabor2022.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UserAndSkills(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "skill_id",
        associateBy = Junction(UserSkillCrossRef::class)
    )
    val skills: List<Skill>,
)

data class SkillsAndUser(
    @Embedded
    val skill: Skill,
    @Relation(
        parentColumn = "skill_id",
        entityColumn = "user_id",
        associateBy = Junction(UserSkillCrossRef::class)
    )
    val users: List<User>,
)