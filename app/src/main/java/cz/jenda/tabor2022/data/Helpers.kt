package cz.jenda.tabor2022.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.data.model.UserWithSkills
import cz.jenda.tabor2022.data.proto.Portal

object Helpers {
    fun isEqual(user: UserWithSkills, playerData: Portal.PlayerData): Boolean {
        if (user.user.id != playerData.userId.toLong()) return false
        if (user.user.strength != playerData.strength) return false
        if (user.user.magic != playerData.magic) return false
        if (user.user.dexterity != playerData.dexterity) return false
        if (user.user.bonusPoints != playerData.bonusPoints) return false

        val skillsFromDB = user.skills.map { it.id.toInt() }.toSet()
        val skillsFromTag = playerData.skillsList.map { it.number }.toSet()
        Log.v(Constants.AppTag, "Comparing skills: $skillsFromDB vs $skillsFromTag")

        if (skillsFromDB != skillsFromTag) return false;

        return true
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
            if (e is SQLiteConstraintException && e.message?.contains(Constants.Db.UniqueConflict) == true) {
                Log.v(Constants.AppTag, "Importing already imported transaction", e)
            } else {
                Log.e(Constants.AppTag, "Error while importing", e)
                throw e
            }
        }
    }
}