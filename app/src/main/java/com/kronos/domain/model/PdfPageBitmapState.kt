package com.kronos.domain.model

import android.graphics.Bitmap

sealed class PdfPageBitmapState {
    object Idle : PdfPageBitmapState()
    object Loading : PdfPageBitmapState()
    data class Rendered(val bitmap: Bitmap) : PdfPageBitmapState()
    data class Error(val cause: Throwable) : PdfPageBitmapState()
}
