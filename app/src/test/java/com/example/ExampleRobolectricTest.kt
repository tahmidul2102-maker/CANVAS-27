package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CanvasDatabase
import com.example.data.repository.MagazineRepository
import com.example.data.repository.SampleCanvasAiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Canvas 27", appName)
    }

    @Test
    fun `seed magazine posts and persist visitor comments`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, CanvasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = MagazineRepository(db.magazineDao(), context)
            repo.seedInitialContentIfNeeded()
            val seeded = repo.allPosts.first()
            assertEquals(4, seeded.size)

            val firstPostId = seeded.first().post.id
            val initialComments = seeded.first().comments.size
            repo.addVisitorComment(firstPostId, "Arif", "Great editorial photography!")

            val updated = repo.allPosts.first().first { it.post.id == firstPostId }
            assertEquals(initialComments + 1, updated.comments.size)
            val addedComment = updated.comments.first { it.visitorName == "Arif" }

            repo.toggleCommentLike(addedComment.id)
            val liked = repo.allPosts.first().first { it.post.id == firstPostId }
                .comments.first { it.id == addedComment.id }
            assertTrue(liked.isLiked)
            assertEquals(1, liked.likesCount)

            repo.deleteComment(addedComment.id)
            val afterDelete = repo.allPosts.first().first { it.post.id == firstPostId }
            assertEquals(initialComments, afterDelete.comments.size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `sample canvas ai responds in bengali and english`() = runTest {
        val ai = SampleCanvasAiRepository()
        val bengaliReply = ai.askAssistant("বাংলায় সেরা আর্টিকেল দেখাও", emptyList(), emptyList())
        assertTrue(bengaliReply.contains("নমুনা এআই উত্তর"))

        val englishReply = ai.askAssistant("Suggest a photo caption", emptyList(), emptyList())
        assertTrue(englishReply.contains("Sample AI Preview"))
    }
}
