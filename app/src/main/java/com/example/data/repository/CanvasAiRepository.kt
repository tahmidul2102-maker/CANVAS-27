package com.example.data.repository

import com.example.data.local.MagazinePostWithComments
import kotlinx.coroutines.delay

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isSampleMode: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiUploadSuggestion(
    val headline: String,
    val caption: String,
    val isSampleMode: Boolean = true
)

interface CanvasAiRepository {
    val requiresFirebaseSetup: Boolean

    suspend fun askAssistant(
        userMessage: String,
        posts: List<MagazinePostWithComments>,
        history: List<AiChatMessage>
    ): String

    suspend fun suggestTitleAndCaption(
        category: String,
        notes: String,
        preferBengali: Boolean
    ): AiUploadSuggestion
}

/**
 * Placeholder editorial assistant used when Firebase AI Logic is not enabled.
 * Provides clearly labeled sample responses in both English and Bengali based on live magazine posts.
 */
class SampleCanvasAiRepository : CanvasAiRepository {
    override val requiresFirebaseSetup: Boolean = true

    override suspend fun askAssistant(
        userMessage: String,
        posts: List<MagazinePostWithComments>,
        history: List<AiChatMessage>
    ): String {
        delay(550)
        val trimmed = userMessage.trim()
        val isBengali = containsBengali(trimmed) ||
            trimmed.contains("bengali", ignoreCase = true) ||
            trimmed.contains("bangla", ignoreCase = true)

        val lower = trimmed.lowercase()

        // 1. Summarize request
        if (lower.contains("summarize") || lower.contains("summary") ||
            trimmed.contains("সারসংক্ষেপ") || trimmed.contains("সংক্ষেপে") || trimmed.contains("সারাংশ")
        ) {
            val matched = findBestMatchingPost(trimmed, posts) ?: posts.firstOrNull()
            return if (matched != null) {
                val p = matched.post
                val firstSentence = p.content.lineSequence()
                    .map { it.trim() }
                    .firstOrNull { it.isNotBlank() }
                    .orEmpty()
                if (isBengali) {
                    "[নমুনা এআই উত্তর] **${p.title}** (${p.category}) — লিখেছেন ${p.authorName}।\n\n" +
                        "মূল ভাবনা: $firstSentence\n\n" +
                        "ম্যাগাজিনে পাঠ সময় আনুমানিক ${p.readTimeMinutes} মিনিট এবং এই মুহূর্তে ${matched.comments.size}টি পাঠক মন্তব্য রয়েছে।"
                } else {
                    "[Sample AI Preview] **${p.title}** (${p.category}) by ${p.authorName}:\n\n" +
                        "Key Takeaway: $firstSentence\n\n" +
                        "Estimated ${p.readTimeMinutes}-min read · ${matched.comments.size} visitor comment(s) on CANVAS 27."
                }
            } else {
                if (isBengali) {
                    "[নমুনা এআই উত্তর] এখনো কোনো গল্প পাওয়া যায়নি—উপরের Submit বাটনে ক্লিক করে আপনার প্রথম স্টোরি প্রকাশ করুন!"
                } else {
                    "[Sample AI Preview] There are no stories in the issue yet—tap Submit at the top to publish the first CANVAS 27 feature!"
                }
            }
        }

        // 2. Caption / title suggestion request
        if (lower.contains("caption") || lower.contains("title") || lower.contains("headline") ||
            lower.contains("upload") || trimmed.contains("শিরোনাম") || trimmed.contains("ক্যাপশন") ||
            trimmed.contains("ছবি")
        ) {
            return if (isBengali) {
                "[নমুনা এআই উত্তর] আপনার ছবির জন্য ৩টি সম্পাদকীয় শিরোনাম ও ক্যাপশন প্রস্তাবনা:\n" +
                    "১. \"নীরব আলোর ফ্রেমে শহরের গল্প\" — দৈনন্দিন দৃশ্যের ভেতর লুকিয়ে থাকা শিল্প।\n" +
                    "২. \"রঙ ও ছায়ার একান্ত সংলাপ\" — ক্যামেরার চোখে মুহূর্তের কাব্য।\n" +
                    "৩. \"সময় থামানো এক দুপুর\" — আলো, টেক্সচার আর অনুভূতির মেলবন্ধন।\n\n" +
                    "আপনি পোস্ট আপলোড ফর্মের \"Suggest with Canvas AI\" বাটন থেকেও সরাসরি শিরোনাম নিতে পারেন!"
            } else {
                "[Sample AI Preview] Here are 3 editorial headline ideas for your next CANVAS 27 upload:\n" +
                    "1. \"Quiet Geometry: Finding Stillness in Everyday Light\"\n" +
                    "2. \"Between Shadow and Grain — A Visual Field Note\"\n" +
                    "3. \"Tactile Moments: Stories Framed Without Rush\"\n\n" +
                    "Tip: Pair a crisp 1-sentence scene setter with a personal reflection for maximum editorial impact!"
            }
        }

        // 3. Find / recommend articles
        if (lower.contains("find") || lower.contains("recommend") || lower.contains("article") ||
            lower.contains("story") || lower.contains("featured") || lower.contains("read") ||
            trimmed.contains("আর্টিকেল") || trimmed.contains("গল্প") || trimmed.contains("দেখাও") ||
            trimmed.contains("পড়")
        ) {
            val topPosts = posts.take(3)
            val listText = if (isBengali) {
                topPosts.mapIndexed { idx, item ->
                    "${idx + 1}. **${item.post.title}** (${item.post.category}) — ${item.post.authorName}"
                }.joinToString("\n")
            } else {
                topPosts.mapIndexed { idx, item ->
                    "${idx + 1}. **${item.post.title}** (${item.post.category}) by ${item.post.authorName}"
                }.joinToString("\n")
            }
            return if (isBengali) {
                "[নমুনা এআই উত্তর] CANVAS 27-এর বর্তমান সেরা কিছু গল্প:\n$listText\n\nযেকোনো কার্ডে ট্যাপ করে সম্পূর্ণ লেখা এবং পাঠক মন্তব্য পড়তে পারেন।"
            } else {
                "[Sample AI Preview] Here are standout picks currently live in CANVAS 27:\n$listText\n\nTap any card in the grid to read the full story and join the visitor comments."
            }
        }

        // 4. General creative / bilingual welcome
        return if (isBengali) {
            "[নমুনা এআই উত্তর] স্বাগতম! আমি **Canvas AI**, CANVAS 27-এর সম্পাদকীয় সহকারী।\n\n" +
                "আমি আপনাকে সাহায্য করতে পারি:\n" +
                "• পছন্দের আর্টিকেল খুঁজে দিতে ও সারসংক্ষেপ তৈরি করতে\n" +
                "• আপনার ছবির জন্য সৃজনশীল বাংলা বা ইংরেজি শিরোনাম সাজিয়ে দিতে\n" +
                "• ফটোগ্রাফি, ডিজাইন ও লেখালেখির অনুপ্রেরণা দিতে\n\n" +
                "বর্তমানে নমুনা মোড চালু আছে; লাইভ Gemini এআই ব্যবহারের জন্য Firebase AI Logic সক্রিয় করতে হবে।"
        } else {
            "[Sample AI Preview] Welcome to **CANVAS 27**! I'm **Canvas AI**, your bilingual editorial assistant.\n\n" +
                "Ask me in English or বাংলা to:\n" +
                "• Find or summarize stories in this issue (${posts.size} stories live)\n" +
                "• Brainstorm poetic headlines & captions for your photo uploads\n" +
                "• Spark creative ideas across Art, Lifestyle, and Tech\n\n" +
                "Note: Sample AI mode is active until Firebase AI Logic is enabled."
        }
    }

    override suspend fun suggestTitleAndCaption(
        category: String,
        notes: String,
        preferBengali: Boolean
    ): AiUploadSuggestion {
        delay(400)
        val cleanNotes = notes.trim()
        val useBengali = preferBengali || containsBengali(cleanNotes)

        if (useBengali) {
            val headline = when (category) {
                "Art & Photography" -> "আলো ও ছায়ার ক্যানভাসে নীরব মুহূর্ত"
                "Lifestyle" -> "ধীর জীবনের শিল্প: প্রতিদিনের ছোট গল্প"
                "Tech & Ideas" -> "ভবিষ্যতের স্পর্শ: সৃজনশীল প্রযুক্তির নতুন অধ্যায়"
                else -> "বিশেষ সংখ্যা ২৭: সমসাময়িক সৃষ্টির প্রতিচ্ছবি"
            }
            val caption = if (cleanNotes.isNotBlank()) {
                "[নমুনা এআই প্রস্তাব] $cleanNotes — আলো, অনুভূতি আর সময়ের সূক্ষ্ম মেলবন্ধনে তৈরি একটি ভিজ্যুয়াল গল্প। প্রতিটি ফ্রেমে লুকিয়ে থাকে না বলা কিছু কথা, যা দর্শককে থমকে দাঁড়াতে শেখায়।"
            } else {
                "[নমুনা এআই প্রস্তাব] আমাদের চারপাশের চেনা দৃশ্যগুলোও নতুন দৃষ্টিতে অসাধারণ হয়ে ওঠে। আজকের এই ফ্রেমে ধরা পড়েছে রঙ, টেক্সচার এবং নীরব সৌন্দর্যের এক অনন্য মুহূর্ত।"
            }
            return AiUploadSuggestion(headline = headline, caption = caption, isSampleMode = true)
        }

        val headline = when (category) {
            "Art & Photography" -> "Frames of Stillness: Chasing Light Across Unlikely Corners"
            "Lifestyle" -> "Unhurried Craft: Finding Warmth in Everyday Rituals"
            "Tech & Ideas" -> "Designed to Feel: Where Tactile Craft Meets Modern Ideas"
            else -> "Editor's Pick: Visual Notes From Issue 27"
        }
        val caption = if (cleanNotes.isNotBlank()) {
            "[Sample AI Draft] $cleanNotes — Captured in natural contrast with an eye for quiet detail, this visual essay invites readers to pause and notice the rhythm hidden inside everyday scenes."
        } else {
            "[Sample AI Draft] Every photograph begins before the shutter clicks—in the patience to observe texture, shadow, and human presence. Here is a fleeting study preserved for Issue 27."
        }
        return AiUploadSuggestion(headline = headline, caption = caption, isSampleMode = true)
    }

    private fun containsBengali(text: String): Boolean =
        text.any { it in '\u0980'..'\u09FF' }

    private fun findBestMatchingPost(
        query: String,
        posts: List<MagazinePostWithComments>
    ): MagazinePostWithComments? {
        val tokens = query.lowercase().split(Regex("\\s+")).filter { it.length > 3 }
        return posts.maxByOrNull { item ->
            val haystack = "${item.post.title} ${item.post.category} ${item.post.authorName}".lowercase()
            tokens.count { haystack.contains(it) }
        }
    }
}
