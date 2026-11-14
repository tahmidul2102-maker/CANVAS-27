package com.example.ui.viewmodel

import android.net.Uri
import com.example.data.local.MagazinePostWithComments
import com.example.data.repository.AiChatMessage

enum class MagazineCategory(val label: String) {
    ALL("All Stories"),
    FEATURED("Featured"),
    ART_PHOTOGRAPHY("Art & Photography"),
    LIFESTYLE("Lifestyle"),
    TECH_IDEAS("Tech & Ideas")
}

val UPLOADABLE_CATEGORIES = listOf(
    "Art & Photography",
    "Lifestyle",
    "Tech & Ideas"
)

data class UploadDraftState(
    val pickedImageUri: Uri? = null,
    val title: String = "",
    val category: String = "Art & Photography",
    val authorName: String = "",
    val content: String = "",
    val isFeatured: Boolean = false,
    val isPublishing: Boolean = false,
    val isAiSuggesting: Boolean = false,
    val errorMessage: String? = null
)

data class MagazineUiState(
    val isLoading: Boolean = true,
    val allPosts: List<MagazinePostWithComments> = emptyList(),
    val filteredPosts: List<MagazinePostWithComments> = emptyList(),
    val selectedCategory: MagazineCategory = MagazineCategory.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedPostId: Long? = null,
    val isUploadSheetOpen: Boolean = false,
    val uploadDraft: UploadDraftState = UploadDraftState(),
    val isAiChatOpen: Boolean = false,
    val aiMessages: List<AiChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val aiRequiresFirebaseNotice: Boolean = true,
    val snackbarMessage: String? = null
) {
    val selectedPost: MagazinePostWithComments?
        get() = selectedPostId?.let { id -> allPosts.find { it.post.id == id } }
}
