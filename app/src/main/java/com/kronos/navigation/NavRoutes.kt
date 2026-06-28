package com.kronos.navigation

object NavRoutes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val READER = "reader/{bookId}"
    const val INFO = "info/{bookId}"
    const val PROGRESS = "progress/{bookId}"

    fun reader(bookId: Long) = "reader/$bookId"
    fun info(bookId: Long) = "info/$bookId"
    fun progress(bookId: Long) = "progress/$bookId"
}
