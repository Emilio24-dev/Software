package com.example.registro.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClipsDb : RoomDatabase() {

    abstract fun clipDao(): ClipDao

    companion object {
        @Volatile
        private var INSTANCE: ClipsDb? = null

        fun getInstance(ctx: Context): ClipsDb {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    ctx.applicationContext,
                    ClipsDb::class.java,
                    "sentri.db"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}
