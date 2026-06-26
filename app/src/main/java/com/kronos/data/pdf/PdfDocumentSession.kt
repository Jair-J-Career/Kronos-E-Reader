package com.kronos.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.kronos.common.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfDocumentSession @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    val mutex = Mutex()

    private var renderer: PdfRenderer? = null
    private var pfd: ParcelFileDescriptor? = null

    val pageCount: Int get() = renderer?.pageCount ?: 0

    suspend fun open(fileUri: String) = withContext(ioDispatcher) {
        mutex.withLock {
            closeInternal()
            val newPfd = context.contentResolver.openFileDescriptor(Uri.parse(fileUri), "r")
                ?: throw IOException("Cannot open: $fileUri")
            pfd = newPfd
            renderer = PdfRenderer(newPfd)
        }
    }

    suspend fun renderPage(pageIndex: Int, width: Int, height: Int): Bitmap =
        withContext(ioDispatcher) {
            mutex.withLock {
                val r = checkNotNull(renderer) { "No document open" }
                r.openPage(pageIndex).use { page ->
                    val scale = width.toFloat() / page.width
                    val bitmapH = (page.height * scale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, bitmapH, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        }

    fun close() {
        closeInternal()
    }

    private fun closeInternal() {
        renderer?.close()
        renderer = null
        pfd?.close()
        pfd = null
    }
}
