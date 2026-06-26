package com.kronos.domain.model

data class Book(
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val fileUri: String,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val coverImagePath: String? = null,
    val addedAt: Long,
    val lastOpenedAt: Long? = null,
    val isFavorite: Boolean = false,
    val isInTrash: Boolean = false,
    val trashedAt: Long? = null,
    val seriesId: Long? = null,
    val seriesPosition: Double? = null,
    val embeddingId: String? = null,
    val sourceTextHash: String? = null
)
