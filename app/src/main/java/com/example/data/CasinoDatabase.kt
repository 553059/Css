package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameSession::class, ActiveBonus::class, AIPensamiento::class],
    version = 1,
    exportSchema = false
)
abstract class CasinoDatabase : RoomDatabase() {

    abstract fun casinoDao(): CasinoDao

    companion object {
        @Volatile
        private var INSTANCE: CasinoDatabase? = null

        fun getDatabase(context: Context): CasinoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CasinoDatabase::class.java,
                    "casino_analyzer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
