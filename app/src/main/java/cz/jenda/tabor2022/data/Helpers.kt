package cz.jenda.tabor2022.data

import cz.jenda.tabor2022.data.proto.Portal

object Helpers {
    fun compare(user: User, playerData: Portal.PlayerData): Boolean {
        if (user.id != playerData.userId) return false;
        if (user.strength != playerData.strength) return false;
        if (user.magic != playerData.magic) return false;
        if (user.dexterity != playerData.dexterity) return false;
        if (user.bonusPoints != playerData.bonusPoints) return false;
        return true;
    }

    fun toUser(playerData: Portal.PlayerData): User {
        return User(
            playerData.userId,
            "N/A",
            playerData.strength,
            playerData.dexterity,
            playerData.magic,
            playerData.magic
        )
    }
}