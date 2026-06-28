package com.kronos.navigation

object NavRoutes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val READER = "reader/{bookId}"

    fun reader(bookId: Long) = "reader/$bookId"
}
