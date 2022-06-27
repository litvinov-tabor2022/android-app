package cz.jenda.tabor2022.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.jenda.tabor2022.Constants.DbName
import cz.jenda.tabor2022.data.dao.GameTransactionDao
import cz.jenda.tabor2022.data.dao.SkillDao
import cz.jenda.tabor2022.data.dao.UserDao
import cz.jenda.tabor2022.data.dao.UserSkillCrossRefDao
import cz.jenda.tabor2022.data.model.*
import java.io.File

@Database(
    entities = [
        User::class,
        GameTransaction::class,
        Skill::class,
        UserSkillCrossRef::class
    ],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        fun populateFromFile(context: Context, file: File) {
            buildDatabaseFromFile(context, file).also { INSTANCE = it }
        }

        private fun buildDatabaseFromFile(context: Context, file: File) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DbName)
                .createFromFile(file)
                .fallbackToDestructiveMigration()
                .build()

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DbName)
                .fallbackToDestructiveMigration()
                .build()
    }

    abstract fun usersDao(): UserDao
    abstract fun skillDao(): SkillDao
    abstract fun userSkillCrossRefDao(): UserSkillCrossRefDao
    abstract fun transactionsDao(): GameTransactionDao
}