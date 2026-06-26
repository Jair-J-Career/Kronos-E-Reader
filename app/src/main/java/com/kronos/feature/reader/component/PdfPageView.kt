package com.kronos.feature.reader.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.kronos.domain.model.PdfPageBitmapState

@Composable
fun PdfPageView(
    state: PdfPageBitmapState,
    onIsZoomedChange: (Boolean) -> Unit,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoom, pan, _ ->
        val newScale = (scale * zoom).coerceIn(1f, 5f)
        if (newScale != scale) {
            scale = newScale
            onIsZoomedChange(newScale > 1f)
        }
        offset = if (scale > 1f) offset + pan else Offset.Zero
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(onTap) { detectTapGestures(onTap = { onTap() }) }
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            PdfPageBitmapState.Idle,
            PdfPageBitmapState.Loading -> CircularProgressIndicator()

            is PdfPageBitmapState.Rendered -> Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
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
    }
}
