package com.kronos.domain.usecase.book

import android.net.Uri
import com.kronos.domain.source.CoverExtractor
import com.kronos.domain.source.PdfDocumentScanner
import javax.inject.Inject

class AddSpecificFilesUseCase @Inject constructor(
    private val scanner: PdfDocumentScanner,
    private val coverExtractor: CoverExtractor,
    private val addBook: AddBookUseCase
) {
    suspend operator fun invoke(fileUris: List<Uri>) {
        scanner.scanFiles(fileUris).forEach { book ->
            val coverPath = coverExtractor.extract(Uri.parse(book.fileUri))
            addBook(book.copy(coverImagePath = coverPath))
        }
    }
}
