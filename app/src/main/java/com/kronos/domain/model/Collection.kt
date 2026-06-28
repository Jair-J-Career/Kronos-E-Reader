package com.kronos.domain.model

data class Collection(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int = 0
)
