package com.kronos.data.repository

import com.kronos.data.database.dao.NoteDao
import com.kronos.data.database.entity.NoteEntity
import com.kronos.domain.model.Note
import com.kronos.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun getNotesForBook(bookId: Long): Flow<List<Note>> =
        dao.observeAllForBook(bookId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addNote(note: Note): Long = dao.insert(note.toEntity())

    override suspend fun updateNote(note: Note) = dao.update(note.toEntity())

    override suspend fun deleteNote(id: Long) = dao.deleteById(id)

    private fun NoteEntity.toDomain() = Note(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
        linkedQuoteId = linkedQuoteId,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )

    private fun Note.toEntity() = NoteEntity(
        id = id,
        bookId = bookId,
        pageNumber = pageNumber,
        text = text,
        createdAt = createdAt,
        updatedAt = updatedAt,
        linkedQuoteId = linkedQuoteId,
        embeddingId = embeddingId,
        sourceTextHash = sourceTextHash
    )
}
