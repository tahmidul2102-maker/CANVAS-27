package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArticleReaderScreen
import com.example.ui.components.AskCanvasAiButton
import com.example.ui.components.CanvasAiSheet
import com.example.ui.components.MagazineCard
import com.example.ui.components.MastheadHeader
import com.example.ui.components.UploadPostSheet
import com.example.ui.viewmodel.MagazineViewModel

@Composable
fun CanvasMagazineApp(
    viewModel: MagazineViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                MastheadHeader(
                    selectedCategory = state.selectedCategory,
                    isSearchActive = state.isSearchActive,
                    searchQuery = state.searchQuery,
                    storyCount = state.filteredPosts.size,
                    onSelectCategory = viewModel::selectCategory,
                    onToggleSearch = viewModel::toggleSearch,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onOpenUpload = viewModel::openUploadSheet
                )
            }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val columns = when {
                    maxWidth >= 900.dp -> 3
                    maxWidth >= 560.dp -> 2
                    else -> 1
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    state.filteredPosts.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No stories in this section yet",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try switching categories or publish a fresh visual story.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = viewModel::openUploadSheet,
                                modifier = Modifier.testTag("empty_state_submit_button")
                            ) {
                                Text("Upload Story")
                            }
                        }
                    }

                    else -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(columns),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("editorial_feed_grid"),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 104.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalItemSpacing = 16.dp
                        ) {
                            items(
                                items = state.filteredPosts,
                                key = { it.post.id }
                            ) { item ->
                                MagazineCard(
                                    item = item,
                                    onClick = { viewModel.openPost(item.post.id) },
                                    onToggleLike = { viewModel.togglePostLike(item.post.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full-Screen Story Reading Overlay
        AnimatedVisibility(
            visible = state.selectedPost != null,
            enter = slideInVertically(initialOffsetY = { it / 8 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 8 }) + fadeOut()
        ) {
            state.selectedPost?.let { postWithComments ->
                ArticleReaderScreen(
                    item = postWithComments,
                    onBack = viewModel::closePost,
                    onTogglePostLike = viewModel::togglePostLike,
                    onSummarizeWithAi = viewModel::askAiToSummarizePost,
                    onAddComment = viewModel::addVisitorComment,
                    onToggleCommentLike = viewModel::toggleCommentLike,
                    onDeleteComment = viewModel::deleteComment
                )
            }
        }

        // Floating Bottom-Right Ask Canvas AI Widget
        AskCanvasAiButton(
            onClick = viewModel::openAiChat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
        )

        // Upload / Submit Story Bottom Sheet
        if (state.isUploadSheetOpen) {
            UploadPostSheet(
                draft = state.uploadDraft,
                showAiFirebaseNotice = state.aiRequiresFirebaseNotice,
                onDismiss = viewModel::closeUploadSheet,
                onPickPhoto = viewModel::updateDraftPhoto,
                onTitleChange = viewModel::updateDraftTitle,
                onCategoryChange = viewModel::updateDraftCategory,
                onAuthorChange = viewModel::updateDraftAuthor,
                onContentChange = viewModel::updateDraftContent,
                onFeaturedChange = viewModel::updateDraftFeatured,
                onSuggestWithAi = viewModel::suggestUploadWithAi,
                onPublish = viewModel::publishDraftPost
            )
        }

        // Canvas AI Bilingual Assistant Bottom Sheet
        if (state.isAiChatOpen) {
            CanvasAiSheet(
                messages = state.aiMessages,
                isThinking = state.isAiThinking,
                showFirebaseNotice = state.aiRequiresFirebaseNotice,
                onSendMessage = viewModel::sendAiMessage,
                onDismiss = viewModel::closeAiChat
            )
        }
    }
}
