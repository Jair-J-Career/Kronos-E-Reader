package com.kronos.domain.usecase.bookmark

import com.kronos.domain.model.Bookmark
import com.kronos.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksForBookUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Bookmark>> = repository.getBookmarksForBook(bookId)
}
