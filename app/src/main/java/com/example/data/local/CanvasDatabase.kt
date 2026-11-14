package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MagazinePostEntity::class, VisitorCommentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CanvasDatabase : RoomDatabase() {
    abstract fun magazineDao(): MagazineDao

    companion object {
        @Volatile
        private var INSTANCE: CanvasDatabase? = null

        fun getInstance(context: Context): CanvasDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CanvasDatabase::class.java,
                    "canvas27_editorial.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
