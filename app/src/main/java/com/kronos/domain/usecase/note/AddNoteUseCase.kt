package com.kronos.domain.usecase.note

import com.kronos.domain.model.Note
import com.kronos.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long = repository.addNote(note)
}
