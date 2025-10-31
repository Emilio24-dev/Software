package com.example.registro.db

import android.content.Context

object DbProvider {
    fun dao(context: Context): ClipDao {
        return ClipsDb.getInstance(context).clipDao()
    }
}
