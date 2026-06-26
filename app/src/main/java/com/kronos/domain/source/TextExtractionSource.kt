package com.kronos.domain.source

import com.kronos.domain.model.PageTextChunk
import kotlinx.coroutines.flow.Flow

interface TextExtractionSource {
    suspend fun extractPageText(bookId: Long, pageNumber: Int): String
    fun extractAllPages(bookId: Long): Flow<PageTextChunk>
}
