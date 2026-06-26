package com.kronos.domain.usecase.note

import com.kronos.domain.model.Note
import com.kronos.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesForBookUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(bookId: Long): Flow<List<Note>> = repository.getNotesForBook(bookId)
}
