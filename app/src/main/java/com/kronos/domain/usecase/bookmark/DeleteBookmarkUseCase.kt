package com.kronos.domain.usecase.bookmark

import com.kronos.domain.repository.BookmarkRepository
import javax.inject.Inject

class DeleteBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteBookmark(id)
}
