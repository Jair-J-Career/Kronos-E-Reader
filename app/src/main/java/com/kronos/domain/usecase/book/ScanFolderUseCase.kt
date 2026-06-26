package com.kronos.domain.usecase.book

import android.net.Uri
import com.kronos.domain.source.PdfDocumentScanner
import javax.inject.Inject

class ScanFolderUseCase @Inject constructor(
    private val scanner: PdfDocumentScanner,
    private val addBook: AddBookUseCase
) {
    suspend operator fun invoke(treeUri: Uri) {
        scanner.scanTree(treeUri).forEach { addBook(it) }
    }
}
