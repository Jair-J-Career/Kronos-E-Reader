package com.kronos.feature.reader.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.domain.model.Bookmark
import com.kronos.ui.component.EmptyState
import com.kronos.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Progress") }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProgressUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(paddingValues))
            is ProgressUiState.Error -> EmptyState(
                title = "Error",
                subtitle = state.message,
                modifier = Modifier.padding(paddingValues),
                action = { TextButton(onClick = onNavigateBack) { Text("Go Back") } }
            )
            is ProgressUiState.Success -> ProgressContent(
                state = state,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ProgressContent(state: ProgressUiState.Success, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "${state.readPercentage.toInt()}%",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Read",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${state.currentPage + 1} / ${state.totalPages}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Page",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        AnnotationChart(
            octantCounts = state.octantCounts,
            maxAnnotations = state.maxAnnotations,
            bookmarks = state.bookmarks,
            totalPages = state.totalPages,
            currentPage = state.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun AnnotationChart(
    octantCounts: List<Int>,
    maxAnnotations: Int,
    bookmarks: List<Bookmark>,
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val barColorAlpha = barColor.copy(alpha = 0.2f)
    val axisColor = MaterialTheme.colorScheme.outline
    val bookmarkColor = MaterialTheme.colorScheme.tertiary
    val safeTotal = totalPages.coerceAtLeast(1).toFloat()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val axisY = h * 0.65f
        val maxBarH = axisY - 16.dp.toPx()
        val slotW = w / 8f
        val barPad = 6.dp.toPx()

        drawLine(
            color = axisColor,
            start = Offset(0f, axisY),
            end = Offset(w, axisY),
            strokeWidth = 1.5.dp.toPx()
        )

        repeat(8) { i ->
            drawRect(
                color = barColorAlpha,
                topLeft = Offset(i * slotW + barPad, 16.dp.toPx()),
                size = Size(slotW - barPad * 2, maxBarH)
            )
        }

        octantCounts.forEachIndexed { i, count ->
            if (count > 0) {
                val barH = (count.toFloat() / maxAnnotations) * maxBarH
                drawRect(
                    color = barColor,
                    topLeft = Offset(i * slotW + barPad, axisY - barH),
                    size = Size(slotW - barPad * 2, barH)
                )
            }
        }

        val belowH = h - axisY
        val tickCenterY = axisY + belowH * 0.55f
        bookmarks.forEach { bookmark ->
            val x = (bookmark.pageNumber.toFloat() / safeTotal) * w
            drawLine(
                color = bookmarkColor,
                start = Offset(x, axisY + 4.dp.toPx()),
                end = Offset(x, axisY + 10.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = bookmarkColor,
                radius = 3.dp.toPx(),
                center = Offset(x, tickCenterY)
            )
        }

        val pageX = (currentPage.toFloat() / safeTotal) * w
        drawLine(
            color = Color.Red,
            start = Offset(pageX, 0f),
            end = Offset(pageX, h),
            strokeWidth = 2.dp.toPx()
        )
    }
}
