package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CanvasDatabase
import com.example.data.local.MagazinePostWithComments
import com.example.data.repository.AiChatMessage
import com.example.data.repository.CanvasAiRepository
import com.example.data.repository.MagazineRepository
import com.example.data.repository.SampleCanvasAiRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "MagazineViewModel"

class MagazineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MagazineRepository
    private val aiRepository: CanvasAiRepository = SampleCanvasAiRepository()

    private val _selectedCategory = MutableStateFlow(MagazineCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _selectedPostId = MutableStateFlow<Long?>(null)
    private val _isUploadSheetOpen = MutableStateFlow(false)
    private val _uploadDraft = MutableStateFlow(UploadDraftState())
    private val _isAiChatOpen = MutableStateFlow(false)
    private val _isAiThinking = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome",
                isUser = false,
                isSampleMode = true,
                text = "Hello! I'm **Canvas AI**, the editorial assistant for **CANVAS 27**.\n\n" +
                    "Ask me in **English** or **বাংলা** to find stories, summarize articles, or craft creative headlines & captions for your photography."
            )
        )
    )

    init {
        val db = CanvasDatabase.getInstance(application)
        repository = MagazineRepository(db.magazineDao(), application.applicationContext)
        viewModelScope.launch {
            try {
                repository.seedInitialContentIfNeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed initial magazine content", e)
            }
        }
    }

    private data class UiControlState(
        val category: MagazineCategory,
        val searchQuery: String,
        val isSearchActive: Boolean,
        val selectedPostId: Long?,
        val isUploadSheetOpen: Boolean,
        val uploadDraft: UploadDraftState,
        val isAiChatOpen: Boolean,
        val aiMessages: List<AiChatMessage>,
        val isAiThinking: Boolean,
        val snackbarMessage: String?
    )

    private val controlsPart1 = combine(
        _selectedCategory,
        _searchQuery,
        _isSearchActive,
        _selectedPostId,
        _isUploadSheetOpen
    ) { category, query, searchActive, postId, uploadOpen ->
        listOf(category, query, searchActive, postId, uploadOpen)
    }

    private val controlsPart2 = combine(
        _uploadDraft,
        _isAiChatOpen,
        _aiMessages,
        _isAiThinking,
        _snackbarMessage
    ) { draft, aiOpen, messages, thinking, snackbar ->
        listOf(draft, aiOpen, messages, thinking, snackbar)
    }

    @Suppress("UNCHECKED_CAST")
    private val controlStateFlow = combine(controlsPart1, controlsPart2) { p1, p2 ->
        UiControlState(
            category = p1[0] as MagazineCategory,
            searchQuery = p1[1] as String,
            isSearchActive = p1[2] as Boolean,
            selectedPostId = p1[3] as Long?,
            isUploadSheetOpen = p1[4] as Boolean,
            uploadDraft = p2[0] as UploadDraftState,
            isAiChatOpen = p2[1] as Boolean,
            aiMessages = p2[2] as List<AiChatMessage>,
            isAiThinking = p2[3] as Boolean,
            snackbarMessage = p2[4] as String?
        )
    }

    val uiState: StateFlow<MagazineUiState> = combine(
        repository.allPosts,
        controlStateFlow
    ) { posts, controls ->
        val filtered = filterPosts(posts, controls.category, controls.searchQuery)
        MagazineUiState(
            isLoading = false,
            allPosts = posts,
            filteredPosts = filtered,
            selectedCategory = controls.category,
            searchQuery = controls.searchQuery,
            isSearchActive = controls.isSearchActive,
            selectedPostId = controls.selectedPostId,
            isUploadSheetOpen = controls.isUploadSheetOpen,
            uploadDraft = controls.uploadDraft,
            isAiChatOpen = controls.isAiChatOpen,
            aiMessages = controls.aiMessages,
            isAiThinking = controls.isAiThinking,
            aiRequiresFirebaseNotice = aiRepository.requiresFirebaseSetup,
            snackbarMessage = controls.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MagazineUiState()
    )

    private fun filterPosts(
        posts: List<MagazinePostWithComments>,
        category: MagazineCategory,
        query: String
    ): List<MagazinePostWithComments> {
        val byCategory = when (category) {
            MagazineCategory.ALL -> posts
            MagazineCategory.FEATURED -> posts.filter { it.post.isFeatured }
            else -> posts.filter { it.post.category == category.label }
        }
        val q = query.trim()
        if (q.isBlank()) return byCategory
        return byCategory.filter { item ->
            item.post.title.contains(q, ignoreCase = true) ||
                item.post.authorName.contains(q, ignoreCase = true) ||
                item.post.category.contains(q, ignoreCase = true) ||
                item.post.content.contains(q, ignoreCase = true)
        }
    }

    fun selectCategory(category: MagazineCategory) {
        _selectedCategory.value = category
    }

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openPost(postId: Long) {
        _selectedPostId.value = postId
    }

    fun closePost() {
        _selectedPostId.value = null
    }

    fun togglePostLike(postId: Long) {
        viewModelScope.launch {
            repository.togglePostLike(postId)
        }
    }

    fun addVisitorComment(postId: Long, visitorName: String, body: String) {
        val cleanName = visitorName.trim()
        val cleanBody = body.trim()
        if (cleanName.isBlank() || cleanBody.isBlank()) {
            _snackbarMessage.value = "Please enter both your name and comment."
            return
        }
        viewModelScope.launch {
            try {
                repository.addVisitorComment(postId, cleanName, cleanBody)
                _snackbarMessage.value = "Comment published"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to post comment", e)
                _snackbarMessage.value = "Could not save comment."
            }
        }
    }

    fun toggleCommentLike(commentId: Long) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId)
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
            _snackbarMessage.value = "Comment removed"
        }
    }

    fun openUploadSheet() {
        _isUploadSheetOpen.value = true
    }

    fun closeUploadSheet() {
        _isUploadSheetOpen.value = false
    }

    fun updateDraftPhoto(uri: Uri?) {
        _uploadDraft.update { it.copy(pickedImageUri = uri, errorMessage = null) }
    }

    fun updateDraftTitle(title: String) {
        _uploadDraft.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateDraftCategory(category: String) {
        _uploadDraft.update { it.copy(category = category) }
    }

    fun updateDraftAuthor(author: String) {
        _uploadDraft.update { it.copy(authorName = author, errorMessage = null) }
    }

    fun updateDraftContent(content: String) {
        _uploadDraft.update { it.copy(content = content, errorMessage = null) }
    }

    fun updateDraftFeatured(isFeatured: Boolean) {
        _uploadDraft.update { it.copy(isFeatured = isFeatured) }
    }

    fun suggestUploadWithAi(preferBengali: Boolean) {
        val current = _uploadDraft.value
        if (current.isAiSuggesting) return
        viewModelScope.launch {
            _uploadDraft.update { it.copy(isAiSuggesting = true, errorMessage = null) }
            try {
                val suggestion = aiRepository.suggestTitleAndCaption(
                    category = current.category,
                    notes = current.title.ifBlank { current.content },
                    preferBengali = preferBengali
                )
                _uploadDraft.update {
                    it.copy(
                        title = suggestion.headline,
                        content = suggestion.caption,
                        isAiSuggesting = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI upload suggestion failed", e)
                _uploadDraft.update {
                    it.copy(isAiSuggesting = false, errorMessage = "Unable to generate suggestion.")
                }
            }
        }
    }

    fun publishDraftPost() {
        val draft = _uploadDraft.value
        val photoUri = draft.pickedImageUri
        when {
            photoUri == null -> {
                _uploadDraft.update { it.copy(errorMessage = "Please select a photo for your story.") }
                return
            }
            draft.title.isBlank() -> {
                _uploadDraft.update { it.copy(errorMessage = "Please add an editorial headline.") }
                return
            }
            draft.authorName.isBlank() -> {
                _uploadDraft.update { it.copy(errorMessage = "Please enter the author's name.") }
                return
            }
            draft.content.isBlank() -> {
                _uploadDraft.update { it.copy(errorMessage = "Please write an article or caption.") }
                return
            }
        }

        viewModelScope.launch {
            _uploadDraft.update { it.copy(isPublishing = true, errorMessage = null) }
            try {
                val newId = repository.publishPost(
                    title = draft.title,
                    category = draft.category,
                    authorName = draft.authorName,
                    content = draft.content,
                    pickedImageUri = photoUri,
                    isFeatured = draft.isFeatured
                )
                _uploadDraft.value = UploadDraftState()
                _isUploadSheetOpen.value = false
                _selectedCategory.value = MagazineCategory.ALL
                _selectedPostId.value = newId
                _snackbarMessage.value = "Story published to CANVAS 27"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to publish post", e)
                _uploadDraft.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = e.localizedMessage ?: "Failed to save story."
                    )
                }
            }
        }
    }

    fun openAiChat() {
        _isAiChatOpen.value = true
    }

    fun closeAiChat() {
        _isAiChatOpen.value = false
    }

    fun askAiToSummarizePost(postTitle: String) {
        _isAiChatOpen.value = true
        sendAiMessage("Summarize \"$postTitle\"")
    }

    fun sendAiMessage(message: String) {
        val clean = message.trim()
        if (clean.isBlank() || _isAiThinking.value) return

        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = clean
        )
        val updatedHistory = _aiMessages.value + userMsg
        _aiMessages.value = updatedHistory
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val currentPosts = uiState.value.allPosts
                val replyText = aiRepository.askAssistant(
                    userMessage = clean,
                    posts = currentPosts,
                    history = updatedHistory
                )
                val botMsg = AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = replyText,
                    isSampleMode = aiRepository.requiresFirebaseSetup
                )
                _aiMessages.value = _aiMessages.value + botMsg
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Canvas AI response failed", e)
                val errorMsg = AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "I couldn't process that right now. Please try again."
                )
                _aiMessages.value = _aiMessages.value + errorMsg
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
