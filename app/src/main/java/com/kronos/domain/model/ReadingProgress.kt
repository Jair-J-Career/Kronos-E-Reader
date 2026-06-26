package com.kronos.domain.model

data class ReadingProgress(
    val id: Long = 0,
    val bookId: Long,
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val currentPage: Int = 0,
    val readPercentage: Double = 0.0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
