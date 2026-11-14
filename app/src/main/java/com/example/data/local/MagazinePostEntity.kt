package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "magazine_posts")
data class MagazinePostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val authorName: String,
    val content: String,
    val imageUri: String,
    val aspectRatio: Float = 1.25f,
    val isFeatured: Boolean = false,
    val readTimeMinutes: Int = 4,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
