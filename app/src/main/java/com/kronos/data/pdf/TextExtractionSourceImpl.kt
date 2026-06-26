package com.kronos.data.pdf

import android.content.Context
import android.net.Uri
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.PageTextChunk
import com.kronos.domain.repository.BookRepository
import com.kronos.domain.source.TextExtractionSource
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextExtractionSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookRepository: BookRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TextExtractionSource {

    init {
        PDFBoxResourceLoader.init(context)
    }

    override suspend fun extractPageText(bookId: Long, pageNumber: Int): String {
        val book = bookRepository.getBookById(bookId) ?: return ""
        return extractPage(book.fileUri, pageNumber)
    }

    override fun extractAllPages(bookId: Long): Flow<PageTextChunk> = flow {
        val book = bookRepository.getBookById(bookId) ?: return@flow
        emitAll(extractAll(book.fileUri, bookId))
    }

    private suspend fun extractPage(fileUri: String, pageNumber: Int): String =
        withContext(ioDispatcher) {
            val inputStream = context.contentResolver.openInputStream(Uri.parse(fileUri))
                ?: return@withContext ""
            try {
                val document = PDDocument.load(inputStream)
                try {
                    val stripper = PDFTextStripper()
                    stripper.startPage = pageNumber + 1
                    stripper.endPage = pageNumber + 1
                    stripper.getText(document).trim()
                } finally {
                    document.close()
                }
            } catch (_: IOException) {
                ""
            } finally {
                inputStream.close()
            }
        }

    private fun extractAll(fileUri: String, bookId: Long): Flow<PageTextChunk> = flow {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(fileUri))
            ?: return@flow
        try {
            val document = PDDocument.load(inputStream)
            try {
                val stripper = PDFTextStripper()
                for (pageIndex in 0 until document.numberOfPages) {
                    stripper.startPage = pageIndex + 1
                    stripper.endPage = pageIndex + 1
                    val text = stripper.getText(document).trim()
                    emit(PageTextChunk(bookId, pageIndex, text, sha256(text)))
                }
            } finally {
                document.close()
            }
        } catch (_: IOException) {
        } finally {
            inputStream.close()
        }
    }.flowOn(ioDispatcher)

    private fun sha256(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
