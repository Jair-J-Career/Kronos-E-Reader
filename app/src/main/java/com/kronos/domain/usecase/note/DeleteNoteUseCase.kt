package com.kronos.domain.usecase.note

import com.kronos.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteNote(id)
}
