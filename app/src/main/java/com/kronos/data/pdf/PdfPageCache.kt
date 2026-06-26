package com.kronos.data.pdf

import android.graphics.Bitmap

class PdfPageCache(private val capacity: Int = 7) {

    private val store = object : LinkedHashMap<Int, Bitmap>(capacity + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<Int, Bitmap>): Boolean {
            val overflow = size > capacity
            if (overflow) eldest.value.recycle()
            return overflow
        }
    }

    operator fun get(index: Int): Bitmap? = store[index]

    operator fun set(index: Int, bitmap: Bitmap) {
        store[index] = bitmap
    }

    fun evictOutsideWindow(currentPage: Int, halfWindow: Int) {
        store.keys
            .filter { kotlin.math.abs(it - currentPage) > halfWindow }
            .forEach { key -> store.remove(key)?.recycle() }
    }

    fun clear() {
        store.values.forEach { it.recycle() }
        store.clear()
    }
}
