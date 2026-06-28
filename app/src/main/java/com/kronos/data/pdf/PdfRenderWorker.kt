package com.kronos.data.pdf

import com.kronos.domain.model.PdfPageBitmapState
import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PdfRenderWorker(
    private val session: PdfDocumentSession,
    private val cache: PdfPageCache,
    private val pageStates: MutableStateFlow<Map<Int, PdfPageBitmapState>>,
    private val renderWidth: Int,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val renderDispatcher = ioDispatcher.limitedParallelism(1)
    private var job: Job? = null
    private var lastPage = -1

    fun start(currentPageFlow: StateFlow<Int>, scope: CoroutineScope) {
        job = scope.launch(renderDispatcher) {
            currentPageFlow.collectLatest { current ->
                val total = session.pageCount
                if (total <= 0) return@collectLatest
                if (lastPage >= 0 && abs(current - lastPage) > HALF_WINDOW * 2) {
                    pageStates.update { states ->
                        states.filterKeys { abs(it - current) <= HALF_WINDOW }
                    }
                }
                lastPage = current
                cache.evictOutsideWindow(current, HALF_WINDOW)
                renderWindow(current, total)
            }
        }
    }

    private suspend fun renderWindow(current: Int, pageCount: Int) {
        for (index in buildPriority(current, pageCount)) {
            if (cache[index] != null) continue
            emit(index, PdfPageBitmapState.Loading)
            try {
                val bitmap = session.renderPage(index, renderWidth, 0)
                cache[index] = bitmap
                emit(index, PdfPageBitmapState.Rendered(bitmap))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(index, PdfPageBitmapState.Error(e))
            }
        }
    }

    private fun buildPriority(current: Int, pageCount: Int): List<Int> {
        val result = mutableListOf(current)
        repeat(HALF_WINDOW) { step ->
            val offset = step + 1
            if (current + offset < pageCount) result.add(current + offset)
            if (current - offset >= 0) result.add(current - offset)
        }
        return result
    }

    private fun emit(index: Int, state: PdfPageBitmapState) {
        pageStates.update { it + (index to state) }
    }

    fun cancel() {
        job?.cancel()
    }

    companion object {
        private const val HALF_WINDOW = 2
    }
}
