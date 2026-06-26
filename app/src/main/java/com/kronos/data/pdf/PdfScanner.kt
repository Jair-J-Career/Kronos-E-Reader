package com.kronos.data.pdf

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.kronos.common.IoDispatcher
import com.kronos.domain.model.Book
import com.kronos.domain.source.PdfDocumentScanner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PdfDocumentScanner {

    private val externalCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Files.getContentUri("external")
    }

    @Suppress("DEPRECATION")
    private val mediaStoreProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.SIZE
    )

    private val mimeSelection = buildString {
        append("${MediaStore.Files.FileColumns.MIME_TYPE} = ?")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            append(" AND ${MediaStore.Files.FileColumns.IS_PENDING} = 0")
        }
    }

    override suspend fun scanDevice(): List<Book> = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val results = mutableListOf<Book>()

        context.contentResolver.query(
            externalCollection,
            mediaStoreProjection,
            mimeSelection,
            arrayOf("application/pdf"),
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            @Suppress("DEPRECATION")
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor.moveToNext()) {
                val rawName = cursor.getString(nameCol) ?: continue
                val contentUri = ContentUris.withAppendedId(externalCollection, cursor.getLong(idCol))
                results.add(
                    Book(
                        title = rawName.substringBeforeLast('.').ifBlank { rawName },
                        filePath = cursor.getString(dataCol) ?: contentUri.toString(),
                        fileUri = contentUri.toString(),
                        fileSizeBytes = cursor.getLong(sizeCol),
                        pageCount = 0,
                        addedAt = now
                    )
                )
            }
        }

        results
    }

    override suspend fun scanTree(treeUri: Uri): List<Book> = withContext(ioDispatcher) {
        val results = mutableListOf<Book>()
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootId)
        collectFromChildren(childrenUri, treeUri, results, System.currentTimeMillis(), depth = 0)
        results
    }

    private fun collectFromChildren(
        childrenUri: Uri,
        treeUri: Uri,
        results: MutableList<Book>,
        addedAt: Long,
        depth: Int
    ) {
        if (depth > 4) return
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idCol) ?: continue
                val mime = cursor.getString(mimeCol) ?: continue
                val name = cursor.getString(nameCol) ?: continue

                when (mime) {
                    DocumentsContract.Document.MIME_TYPE_DIR -> {
                        val nestedChildren = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
                        collectFromChildren(nestedChildren, treeUri, results, addedAt, depth + 1)
                    }
                    "application/pdf" -> {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        results.add(
                            Book(
                                title = name.substringBeforeLast('.').ifBlank { name },
                                filePath = docUri.toString(),
                                fileUri = docUri.toString(),
                                fileSizeBytes = cursor.getLong(sizeCol),
                                pageCount = 0,
                                addedAt = addedAt
                            )
                        )
                    }
                }
            }
        }
    }
}
