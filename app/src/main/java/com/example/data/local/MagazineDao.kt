package com.example.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class MagazinePostWithComments(
    @Embedded val post: MagazinePostEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val comments: List<VisitorCommentEntity>
)

@Dao
interface MagazineDao {
    @Transaction
    @Query("SELECT * FROM magazine_posts ORDER BY createdAt DESC")
    fun observeAllPostsWithComments(): Flow<List<MagazinePostWithComments>>

    @Query("SELECT COUNT(*) FROM magazine_posts")
    suspend fun getPostCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: MagazinePostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<MagazinePostEntity>): List<Long>

    @Query(
        "UPDATE magazine_posts SET isLiked = NOT isLiked, " +
            "likesCount = CASE WHEN isLiked = 1 THEN MAX(0, likesCount - 1) ELSE likesCount + 1 END " +
            "WHERE id = :postId"
    )
    suspend fun togglePostLike(postId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: VisitorCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<VisitorCommentEntity>)

    @Query(
        "UPDATE visitor_comments SET isLiked = NOT isLiked, " +
            "likesCount = CASE WHEN isLiked = 1 THEN MAX(0, likesCount - 1) ELSE likesCount + 1 END " +
            "WHERE id = :commentId"
    )
    suspend fun toggleCommentLike(commentId: Long)

    @Query("DELETE FROM visitor_comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: Long)
}
