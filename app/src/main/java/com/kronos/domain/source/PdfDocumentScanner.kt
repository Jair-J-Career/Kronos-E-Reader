package com.kronos.domain.source

import android.net.Uri
import com.kronos.domain.model.Book

interface PdfDocumentScanner {
    suspend fun scanDevice(): List<Book>
    suspend fun scanTree(treeUri: Uri): List<Book>
}
