package cz.jenda.tabor2022.data.model

import androidx.room.*

data class UserAndTransactions(
    @Embedded
    val user: User,
    @Relation(parentColumn = "user_id", entityColumn = "user_id")
    val gameTransactions: List<GameTransaction>,
)
