package com.example.registro.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClipEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val i = Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    "sentri.db"
                ).build()
                INSTANCE = i
                i
            }
        }
    }
}
