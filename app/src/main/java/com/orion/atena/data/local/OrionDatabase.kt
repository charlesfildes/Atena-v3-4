package com.orion.atena.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.orion.atena.data.model.ChatSession
import com.orion.atena.data.model.MessageEntity

@Database(
    entities = [ChatSession::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class OrionDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: OrionDatabase? = null

        fun getInstance(context: Context): OrionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OrionDatabase::class.java,
                    "orion_database.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
