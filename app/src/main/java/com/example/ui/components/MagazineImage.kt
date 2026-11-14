package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MagazineImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableRes = when (imageUri) {
        "drawable://img_post_architecture" -> R.drawable.img_post_architecture
        "drawable://img_post_ceramics" -> R.drawable.img_post_ceramics
        "drawable://img_post_synth" -> R.drawable.img_post_synth
        "drawable://img_post_monsoon" -> R.drawable.img_post_monsoon
        else -> null
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            AsyncImage(
                model = Uri.parse(imageUri),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}

fun formatEditorialDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatRelativeTimestamp(timestamp: Long): String {
    val diffMs = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val minutes = diffMs / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> formatEditorialDate(timestamp)
    }
}
