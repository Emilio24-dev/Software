package com.example.registro.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClipDao {

    @Insert
    fun insertClip(clip: ClipEntity): Long

    @Query("SELECT * FROM clips ORDER BY timestampMs DESC")
    fun getAllClips(): List<ClipEntity>

    @Query("""
        SELECT * FROM clips
        WHERE timestampMs BETWEEN :fromMs AND :toMs
        ORDER BY timestampMs DESC
    """)
    fun getClipsBetween(fromMs: Long, toMs: Long): List<ClipEntity>
}
