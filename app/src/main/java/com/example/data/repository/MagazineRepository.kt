package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.MagazineDao
import com.example.data.local.MagazinePostEntity
import com.example.data.local.MagazinePostWithComments
import com.example.data.local.VisitorCommentEntity
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MagazineRepository(
    private val dao: MagazineDao,
    private val appContext: Context
) {
    val allPosts: Flow<List<MagazinePostWithComments>> = dao.observeAllPostsWithComments()

    suspend fun seedInitialContentIfNeeded() {
        if (dao.getPostCount() > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour

        val samplePosts = listOf(
            MagazinePostEntity(
                title = "Shadows in Travertine: How Sunlight Curates Modern Brutalism",
                category = "Art & Photography",
                authorName = "Elena Vance",
                content = """
                    Step inside a quiet concrete gallery just after dawn and you realize the architecture is never truly still. Across rough-hewn travertine floors, sharp diagonal blades of morning sunlight carve temporary geometric sculptures that vanish by noon.

                    For Issue 27, photographer Elena Vance spent six days inside minimalist cultural spaces documenting how natural illumination transforms raw concrete from austere to deeply intimate. Instead of fighting harsh midday contrast, modern spatial curators lean into it—letting the building act as a living sundial.

                    "Concrete remembers warmth," Vance notes in her field journal. "When a single terracotta form sits at the intersection of shadow and stone, the entire room slows down to the pace of human breath."

                    Whether you shoot on medium-format film or a pocket digital camera, chasing architectural light rewards patience: arrive early, watch how corners soften, and frame the stillness between footsteps.
                """.trimIndent(),
                imageUri = "drawable://img_post_architecture",
                aspectRatio = 1.33f,
                isFeatured = true,
                readTimeMinutes = 5,
                likesCount = 42,
                createdAt = now - 2 * hour
            ),
            MagazinePostEntity(
                title = "Slow Clay: Inside the Quiet Revival of Unglazed Terracotta Studios",
                category = "Lifestyle",
                authorName = "Rafiul Karim",
                content = """
                    On a narrow sunlit worktable draped in unbleached linen, a dozen hand-thrown stoneware vessels dry in the morning breeze. None of them wear high-gloss industrial glaze; instead, they celebrate the honest iron warmth of raw earth.

                    Across independent studios from Kyoto to Dhaka, a new wave of ceramicists is returning to tactile imperfection—leaving subtle throwing rings intact and firing clay at lower temperatures so every cup holds a quiet dialogue with the hand that shaped it.

                    Living slowly rarely means retreating from the modern world. More often, it begins with choosing objects that invite attention: a kettle poured without rushing, a matte bowl warmed by tea, a studio window left open to morning light.

                    Next time you step into a neighborhood craft market, pick up a piece of raw stoneware and feel its grain. Good design doesn't shout for space; it settles naturally into your daily rituals.
                """.trimIndent(),
                imageUri = "drawable://img_post_ceramics",
                aspectRatio = 0.78f,
                isFeatured = true,
                readTimeMinutes = 4,
                likesCount = 38,
                createdAt = now - 1 * day
            ),
            MagazinePostEntity(
                title = "Tactile Futures: Why Creators Are Returning to Analog Knobs & Warm Sound",
                category = "Tech & Ideas",
                authorName = "Marcus Chen",
                content = """
                    In an era where every creative tool lives behind flat glass, hardware designers are staging a quiet rebellion. Across home studios and sound labs, brushed aluminum encoders, weighted walnut consoles, and clicky mechanical switches are making a celebrated comeback.

                    Why does physical resistance matter in digital art? Cognitive researchers and musicians point to the same truth: muscle memory unlocks creative flow. Turning a dedicated filter knob under warm studio rim light creates an immediate physical bond between intention and sound.

                    Modern hybrid instruments blend the best of both worlds—lossless digital engines paired with tactile analog interfaces that invite experimentation with your eyes closed.

                    As creative technology matures, the smartest tools aren't the ones with the most menus—they are the ones that disappear beneath your fingertips and let your ideas take the lead.
                """.trimIndent(),
                imageUri = "drawable://img_post_synth",
                aspectRatio = 1.33f,
                isFeatured = false,
                readTimeMinutes = 6,
                likesCount = 29,
                createdAt = now - 2 * day
            ),
            MagazinePostEntity(
                title = "Monsoon Reflections: Chasing Twilight Stories After the Rain",
                category = "Art & Photography",
                authorName = "Ananya Sen",
                content = """
                    There is a brief fifteen-minute window right after a monsoon downpour when city streets turn into dark mirrors. Warm amber shopfronts and cool royal-blue twilight hues ripple across wet asphalt, turning ordinary evening commutes into cinema.

                    Armed with a weather-sealed 35mm lens, visual storyteller Ananya Sen walked the rain-washed avenues of Dhaka to capture the poetic rhythm of cycle rickshaws gliding beneath glowing streetlamps.

                    "During the rains, people rush for shelter, yet the street itself becomes calmer," Sen explains. "Every puddle doubles the color palette. You don't need elaborate staging—just respect for the atmosphere and an eye for reflection."

                    Our tip for urban night shooters: expose for the highlights on wet pavement, embrace gentle motion blur, and let the story emerge from the shadows.
                """.trimIndent(),
                imageUri = "drawable://img_post_monsoon",
                aspectRatio = 0.75f,
                isFeatured = false,
                readTimeMinutes = 5,
                likesCount = 54,
                createdAt = now - 3 * day
            )
        )

        val insertedIds = dao.insertPosts(samplePosts)
        if (insertedIds.size >= 4) {
            dao.insertComments(
                listOf(
                    VisitorCommentEntity(
                        postId = insertedIds[0],
                        visitorName = "Nadia Rahman",
                        body = "The diagonal light in this gallery frame feels like a painting. Truly inspiring issue!",
                        likesCount = 7,
                        createdAt = now - 45 * 60_000L
                    ),
                    VisitorCommentEntity(
                        postId = insertedIds[1],
                        visitorName = "Tarek Aziz",
                        body = "অসাধারণ একটি লেখা! মাটির কাজের এই শান্ত সৌন্দর্য মন ছুঁয়ে গেল।",
                        likesCount = 5,
                        createdAt = now - 5 * hour
                    ),
                    VisitorCommentEntity(
                        postId = insertedIds[3],
                        visitorName = "Sadia Chowdhury",
                        body = "Dhaka after monsoon rain has a magic all its own. Love the blue and amber contrast.",
                        likesCount = 9,
                        createdAt = now - 14 * hour
                    )
                )
            )
        }
    }

    suspend fun publishPost(
        title: String,
        category: String,
        authorName: String,
        content: String,
        pickedImageUri: Uri,
        isFeatured: Boolean = false
    ): Long {
        val persistedImagePath = copyImageToAppStorage(pickedImageUri)
        val wordCount = content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val estimatedReadMinutes = max(1, (wordCount / 140).coerceAtLeast(2))

        val post = MagazinePostEntity(
            title = title.trim(),
            category = category,
            authorName = authorName.trim(),
            content = content.trim(),
            imageUri = persistedImagePath,
            aspectRatio = 1.0f,
            isFeatured = isFeatured,
            readTimeMinutes = estimatedReadMinutes,
            likesCount = 1,
            isLiked = true,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertPost(post)
    }

    suspend fun togglePostLike(postId: Long) {
        dao.togglePostLike(postId)
    }

    suspend fun addVisitorComment(postId: Long, visitorName: String, body: String) {
        val comment = VisitorCommentEntity(
            postId = postId,
            visitorName = visitorName.trim(),
            body = body.trim(),
            createdAt = System.currentTimeMillis()
        )
        dao.insertComment(comment)
    }

    suspend fun toggleCommentLike(commentId: Long) {
        dao.toggleCommentLike(commentId)
    }

    suspend fun deleteComment(commentId: Long) {
        dao.deleteCommentById(commentId)
    }

    private suspend fun copyImageToAppStorage(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val uploadsDir = File(appContext.filesDir, "uploads").apply {
            if (!exists()) mkdirs()
        }
        val destFile = File(uploadsDir, "canvas_${UUID.randomUUID()}.jpg")
        appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open selected image.")
        destFile.toURI().toString()
    }
}
