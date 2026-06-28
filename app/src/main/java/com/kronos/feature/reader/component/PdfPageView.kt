package com.kronos.feature.reader.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kronos.domain.model.PdfPageBitmapState

@Composable
fun PdfPageView(
    state: PdfPageBitmapState,
    isNightMode: Boolean,
    isBookmarked: Boolean,
    hasQuote: Boolean,
    onIsZoomedChange: (Boolean) -> Unit,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val transformableState = rememberTransformableState { zoom, pan, _ ->
        val newScale = (scale * zoom).coerceIn(1f, 5f)
        if (newScale != scale) {
            onIsZoomedChange(newScale > 1f)
        }
        scale = newScale
        if (newScale > 1f) {
            val maxX = (newScale - 1f) * containerSize.width / 2f
            val maxY = (newScale - 1f) * containerSize.height / 2f
            offset = Offset(
                (offset.x + pan.x).coerceIn(-maxX, maxX),
                (offset.y + pan.y).coerceIn(-maxY, maxY)
            )
        } else {
            offset = Offset.Zero
        }
    }

    // Desaturates and dims: white pages → dark warm grey (~77,71,51),
    // black text → near-black warm (~8,7,5). Sepia tint reduces blue light.
    val nightModeColorFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    0.08f, 0.16f, 0.03f, 0f, 8f,
                    0.07f, 0.15f, 0.03f, 0f, 7f,
                    0.05f, 0.11f, 0.02f, 0f, 5f,
                    0f,    0f,    0f,    1f, 0f
                )
            )
        )
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { containerSize = it }
            .pointerInput(onTap) { detectTapGestures(onTap = { onTap() }) }
            .transformable(state = transformableState, canPan = { scale > 1f }),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            PdfPageBitmapState.Idle,
            PdfPageBitmapState.Loading -> CircularProgressIndicator()

            is PdfPageBitmapState.Rendered -> Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (isNightMode) nightModeColorFilter else null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )

            is PdfPageBitmapState.Error -> Text(
                text = "Page unavailable",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isBookmarked) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
            )
        }

        if (hasQuote) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = Color(0xFFFFBF00),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(22.dp)
            )
        }
    }
}
