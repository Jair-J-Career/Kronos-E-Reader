package com.kronos.navigation

object NavRoutes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val COLLECTIONS = "collections"
    const val READER = "reader/{bookId}"
    const val INFO = "info/{bookId}"
    const val PROGRESS = "progress/{bookId}"
    const val COLLECTION_DETAIL = "collection/{collectionId}"
    const val QUOTES = "quotes"
    const val TRASH = "trash"
    const val ANNOTATION_DETAIL = "annotation/{bookId}/{type}"

    fun reader(bookId: Long) = "reader/$bookId"
    fun info(bookId: Long) = "info/$bookId"
    fun progress(bookId: Long) = "progress/$bookId"
    fun collectionDetail(collectionId: Long) = "collection/$collectionId"
    fun annotationDetail(bookId: Long, type: String) = "annotation/$bookId/$type"
}
