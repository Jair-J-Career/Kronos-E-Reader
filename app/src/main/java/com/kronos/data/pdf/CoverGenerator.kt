package com.kronos.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.kronos.common.IoDispatcher
import com.kronos.domain.source.CoverExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoverExtractor {

    override suspend fun extract(fileUri: Uri): String? = withContext(ioDispatcher) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(fileUri, "r")
                ?: return@withContext null
            pfd.use { fd ->
                PdfRenderer(fd).use { renderer ->
                    if (renderer.pageCount == 0) return@withContext null
                    renderer.openPage(0).use { page ->
                        val targetHeight = 400
                        val scale = targetHeight.toFloat() / page.height
                        val width = (page.width * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, targetHeight, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val coversDir = File(context.cacheDir, "covers")
                        coversDir.mkdirs()
                        val coverFile = File(coversDir, "${fileUri.hashCode()}.jpg")
                        coverFile.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                        bitmap.recycle()
                        coverFile.absolutePath
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
