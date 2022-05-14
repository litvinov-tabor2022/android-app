package cz.jenda.tabor2022.data

import androidx.room.*

data class UserAndTransactions(
    @Embedded
    val user: User,
    @Relation(parentColumn = "id", entityColumn = "user_id")
    val gameTransactions: List<GameTransaction>,
)
