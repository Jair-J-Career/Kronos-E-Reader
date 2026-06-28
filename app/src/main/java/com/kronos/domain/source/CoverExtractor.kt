package com.kronos.domain.source

import android.net.Uri

interface CoverExtractor {
    suspend fun extract(fileUri: Uri): String?
}
