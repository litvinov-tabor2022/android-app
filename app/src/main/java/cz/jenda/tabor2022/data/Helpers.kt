package cz.jenda.tabor2022.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.model.UserWithSkills
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.proto.Portal

object Helpers {
    fun compare(user: User, playerData: Portal.PlayerData): Boolean {
        if (user.id != playerData.userId.toLong()) return false
        if (user.strength != playerData.strength) return false
        if (user.magic != playerData.magic) return false
        if (user.dexterity != playerData.dexterity) return false
        if (user.bonusPoints != playerData.bonusPoints) return false
        return true
    }

    fun toUser(playerData: Portal.PlayerData): User {
        return User(
            playerData.userId.toLong(),
            "N/A",
            playerData.strength,
            playerData.dexterity,
            playerData.magic,
            playerData.bonusPoints,
            null
        )
    }

    fun UserWithSkills.toPlayerData(): Portal.PlayerData {
        val builder = Portal.PlayerData.newBuilder()
        builder.userId = this.user.id.toInt()
        builder.secret = Constants.TagSecret
        builder.strength = this.user.strength
        builder.dexterity = this.user.dexterity
        builder.magic = this.user.magic
        builder.bonusPoints = this.user.bonusPoints
        this.skills.forEach { builder.addSkills(Portal.Skill.forNumber(it.id.toInt())) }
        return builder.build()
    }

    fun UserWithGroup.toPlayerData(): Portal.PlayerData {
        val builder = Portal.PlayerData.newBuilder()
        builder.userId = this.userWithSkills.user.id.toInt()
        builder.secret = Constants.TagSecret
        builder.strength = this.userWithSkills.user.strength
        builder.dexterity = this.userWithSkills.user.dexterity
        builder.magic = this.userWithSkills.user.magic
        builder.bonusPoints = this.userWithSkills.user.bonusPoints
        this.userWithSkills.skills.forEach { builder.addSkills(Portal.Skill.forNumber(it.id.toInt())) }
        return builder.build()
    }

    suspend fun GameTransaction.execute() {
        val DB = PortalApp.instance.db
        val userId = this.userId
        runCatching {
            DB.withTransaction {
                DB.transactionsDao().save(this)

                if (this.strength != 0) {
                    DB.usersDao().adjustStrength(userId, this.strength)
                }
                if (this.dexterity != 0) {
                    DB.usersDao().adjustDexterity(userId, this.dexterity)
                }
                if (this.magic != 0) {
                    DB.usersDao().adjustMagic(userId, this.magic)
                }
                if (this.bonusPoints != 0) {
                    DB.usersDao().adjustBonusPoints(userId, this.bonusPoints)
                }

                if (this.skillId != 0) {
                    if (this.skillId < 0) {
                        DB.userSkillCrossRefDao().removeSkill(this.userId, this.skillId.toLong())
                    }
                    if (this.skillId > 0) {
                        DB.userSkillCrossRefDao().addSkill(this.userId, this.skillId.toLong())
                    }
                }
            }
        }.onFailure { e ->
            if (e is SQLiteConstraintException) {
                Log.w(Constants.AppTag, "Trying to add skill that user already has!")
            } else {
                throw e
            }
        }
    }
}