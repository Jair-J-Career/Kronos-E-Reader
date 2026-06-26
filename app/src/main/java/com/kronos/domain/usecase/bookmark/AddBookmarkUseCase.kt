package com.kronos.domain.usecase.bookmark

import com.kronos.domain.model.Bookmark
import com.kronos.domain.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark): Long = repository.addBookmark(bookmark)
}
