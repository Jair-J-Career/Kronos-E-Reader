# Kronos — Offline-First Android PDF Reader: Technical Blueprint

**Project codename:** Kronos v1.0
**Platform:** Android (minSdk 23, targetSdk 36)
**Primary language:** Kotlin
**Status:** Greenfield — this document is the authoritative architecture reference

---

## Table of Contents

1. [Module Layout](#1-module-layout)
2. [Full Directory Tree](#2-full-directory-tree)
3. [Room Database Schema](#3-room-database-schema)
4. [Key Architecture Patterns](#4-key-architecture-patterns)
5. [Coding Standards](#5-coding-standards)
6. [Dependency Catalog Reference](#6-dependency-catalog-reference)

---

## 1. Module Layout

The project uses a **single `:app` module**. All source is organized by package under `com.kronos` rather than across separate Gradle modules. This eliminates multi-module build overhead and the need to maintain inter-module dependency declarations while the architecture is still evolving.

Package-level boundaries enforce the same layering rules that Gradle modules would enforce — they just rely on code review and Detekt rather than the build system.

```
Kronos_v10/
├── build.gradle.kts          ← single app build script
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── app/
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   └── java/com/kronos/   ← all source lives here
        ├── test/
        └── androidTest/
```

**Dependency direction (enforced by package discipline):**

```
feature.* → ui.* → domain.* ← data.*
                            ↑
                         common.*
```

- `feature.*` packages import from `domain.*` and `ui.*`; they never import from `data.*` directly
- `data.*` imports from `domain.*` (implements its interfaces) and `common.*`
- `ui.*` imports domain models for Composable parameters only
- Feature packages never share ViewModels or internal state with each other

---

## 2. Full Directory Tree

```
app/src/main/java/com/kronos/
│
├── KronosApp.kt
├── MainActivity.kt
│
├── navigation/
│   ├── KronosNavGraph.kt
│   └── NavRoutes.kt
│
├── common/
│   ├── AppConstants.kt
│   ├── KronosResult.kt
│   ├── DateUtils.kt
│   ├── FileUtils.kt
│   └── HashUtils.kt
│
├── data/
│   ├── database/
│   │   ├── KronosDatabase.kt
│   │   ├── DatabaseMigrations.kt
│   │   ├── converter/
│   │   │   └── RoomTypeConverters.kt
│   │   ├── dao/
│   │   │   ├── AuthorDao.kt
│   │   │   ├── BookDao.kt
│   │   │   ├── BookmarkDao.kt
│   │   │   ├── CollectionDao.kt
│   │   │   ├── NoteDao.kt
│   │   │   ├── QuoteDao.kt
│   │   │   ├── ReadingProgressDao.kt
│   │   │   ├── SearchHistoryDao.kt
│   │   │   └── SeriesDao.kt
│   │   └── entity/
│   │       ├── AuthorEntity.kt
│   │       ├── BookAuthorCrossRef.kt
│   │       ├── BookCollectionCrossRef.kt
│   │       ├── BookEntity.kt
│   │       ├── BookmarkEntity.kt
│   │       ├── CollectionEntity.kt
│   │       ├── NoteEntity.kt
│   │       ├── QuoteEntity.kt
│   │       ├── ReadingProgressEntity.kt
│   │       ├── SearchHistoryEntity.kt
│   │       └── SeriesEntity.kt
│   ├── di/
│   │   ├── DatabaseModule.kt
│   │   ├── PdfModule.kt
│   │   └── RepositoryModule.kt
│   ├── pdf/
│   │   ├── PdfDocumentSession.kt
│   │   ├── PdfPageCache.kt
│   │   ├── PdfRenderWorker.kt
│   │   └── TextExtractionSourceImpl.kt
│   └── repository/
│       ├── AuthorRepositoryImpl.kt
│       ├── BookRepositoryImpl.kt
│       ├── BookmarkRepositoryImpl.kt
│       ├── CollectionRepositoryImpl.kt
│       ├── NoteRepositoryImpl.kt
│       ├── QuoteRepositoryImpl.kt
│       ├── ReadingProgressRepositoryImpl.kt
│       ├── SearchHistoryRepositoryImpl.kt
│       └── SeriesRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Author.kt
│   │   ├── Book.kt
│   │   ├── Bookmark.kt
│   │   ├── Collection.kt
│   │   ├── Note.kt
│   │   ├── PageTextChunk.kt
│   │   ├── PdfPageBitmapState.kt
│   │   ├── Quote.kt
│   │   ├── ReadingProgress.kt
│   │   ├── ReadingStatus.kt
│   │   ├── SearchHistoryItem.kt
│   │   └── Series.kt
│   ├── repository/
│   │   ├── AuthorRepository.kt
│   │   ├── BookRepository.kt
│   │   ├── BookmarkRepository.kt
│   │   ├── CollectionRepository.kt
│   │   ├── NoteRepository.kt
│   │   ├── QuoteRepository.kt
│   │   ├── ReadingProgressRepository.kt
│   │   ├── SearchHistoryRepository.kt
│   │   └── SeriesRepository.kt
│   ├── source/
│   │   └── TextExtractionSource.kt
│   └── usecase/
│       ├── book/
│       │   ├── AddBookUseCase.kt
│       │   ├── DeleteBookUseCase.kt
│       │   ├── GetAllBooksUseCase.kt
│       │   ├── GetBookByIdUseCase.kt
│       │   ├── GetFavoriteBooksUseCase.kt
│       │   ├── GetTrashedBooksUseCase.kt
│       │   ├── MoveToTrashUseCase.kt
│       │   ├── RestoreFromTrashUseCase.kt
│       │   └── ToggleFavoriteUseCase.kt
│       ├── bookmark/
│       │   ├── AddBookmarkUseCase.kt
│       │   ├── DeleteBookmarkUseCase.kt
│       │   └── GetBookmarksForBookUseCase.kt
│       ├── collection/
│       │   ├── AddBookToCollectionUseCase.kt
│       │   ├── CreateCollectionUseCase.kt
│       │   ├── GetCollectionsUseCase.kt
│       │   └── RemoveBookFromCollectionUseCase.kt
│       ├── note/
│       │   ├── AddNoteUseCase.kt
│       │   ├── DeleteNoteUseCase.kt
│       │   ├── GetNotesForBookUseCase.kt
│       │   └── UpdateNoteUseCase.kt
│       ├── progress/
│       │   ├── GetReadingProgressUseCase.kt
│       │   ├── UpdateReadingProgressUseCase.kt
│       │   └── UpdateReadingStatusUseCase.kt
│       ├── quote/
│       │   ├── AddQuoteUseCase.kt
│       │   ├── DeleteQuoteUseCase.kt
│       │   └── GetQuotesForBookUseCase.kt
│       └── search/
│           ├── ClearSearchHistoryUseCase.kt
│           ├── GetSearchHistoryUseCase.kt
│           ├── SaveSearchQueryUseCase.kt
│           └── SearchBooksUseCase.kt
│
├── ui/
│   ├── component/
│   │   ├── AnnotationChip.kt
│   │   ├── BookCoverCard.kt
│   │   ├── ConfirmationDialog.kt
│   │   ├── EmptyState.kt
│   │   ├── ErrorState.kt
│   │   ├── KronosTopBar.kt
│   │   └── LoadingIndicator.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Shape.kt
│   │   ├── Theme.kt
│   │   └── Typography.kt
│   └── util/
│       ├── ComposeExtensions.kt
│       └── WindowSizeClass.kt
│
└── feature/
    ├── library/
    │   ├── LibraryScreen.kt
    │   ├── LibraryUiState.kt
    │   ├── LibraryViewModel.kt
    │   ├── collections/
    │   │   ├── CollectionDetailScreen.kt
    │   │   ├── CollectionDetailViewModel.kt
    │   │   ├── CollectionsScreen.kt
    │   │   └── CollectionsViewModel.kt
    │   ├── component/
    │   │   ├── BookGrid.kt
    │   │   ├── BookListItem.kt
    │   │   ├── LibraryFilterBar.kt
    │   │   └── SortMenuSheet.kt
    │   ├── favorites/
    │   │   ├── FavoritesScreen.kt
    │   │   └── FavoritesViewModel.kt
    │   └── trash/
    │       ├── TrashScreen.kt
    │       └── TrashViewModel.kt
    │
    ├── reader/
    │   ├── ReaderScreen.kt
    │   ├── ReaderUiState.kt
    │   ├── ReaderViewModel.kt
    │   ├── component/
    │   │   ├── PageSlider.kt
    │   │   ├── PdfPageView.kt
    │   │   ├── ReaderBottomBar.kt
    │   │   ├── ReaderOverlay.kt
    │   │   └── ReaderTopBar.kt
    │   └── panel/
    │       ├── BookmarkPanel.kt
    │       ├── NotePanel.kt
    │       └── QuotePanel.kt
    │
    ├── search/
    │   ├── SearchScreen.kt
    │   ├── SearchUiState.kt
    │   ├── SearchViewModel.kt
    │   └── component/
    │       ├── SearchBar.kt
    │       ├── SearchHistoryList.kt
    │       └── SearchResultItem.kt
    │
    └── settings/
        ├── SettingsScreen.kt
        ├── SettingsUiState.kt
        ├── SettingsViewModel.kt
        └── component/
            ├── SettingsSection.kt
            └── ThemeSettingItem.kt
```

Test files mirror this structure under `app/src/test/java/com/kronos/` and `app/src/androidTest/java/com/kronos/`.

---

## 3. Room Database Schema

**Database name:** `kronos.db`
**Current version:** 1

All timestamp columns store Unix epoch milliseconds as `INTEGER NOT NULL` unless noted nullable. `BOOLEAN` columns are `INTEGER` (0/1). Enum columns are `TEXT` via `@TypeConverter`.

---

### 3.1 `books`

| Column | SQLite Type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT | |
| `title` | TEXT | NO | | |
| `file_path` | TEXT | NO | UNIQUE | Absolute path on device |
| `file_uri` | TEXT | NO | | Content URI for persistence |
| `file_size_bytes` | INTEGER | NO | | |
| `page_count` | INTEGER | NO | | Set after first open |
| `cover_image_path` | TEXT | YES | | Extracted thumbnail cache path |
| `added_at` | INTEGER | NO | | Epoch ms |
| `last_opened_at` | INTEGER | YES | | |
| `is_favorite` | INTEGER | NO | DEFAULT 0 | |
| `is_in_trash` | INTEGER | NO | DEFAULT 0 | |
| `trashed_at` | INTEGER | YES | | |
| `series_id` | INTEGER | YES | FK → `series(id)` ON DELETE SET NULL | |
| `series_position` | REAL | YES | | Supports half-positions (e.g. 1.5) |
| `embedding_id` | TEXT | YES | | Future: vector store reference |
| `source_text_hash` | TEXT | YES | | SHA-256 of full extracted text |

**Indices:** `(is_in_trash)`, `(is_favorite)`, `(series_id)`, `(last_opened_at DESC)`

---

### 3.2 `authors`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `name` | TEXT | NO | UNIQUE |
| `biography` | TEXT | YES | |
| `embedding_id` | TEXT | YES | |

---

### 3.3 `series`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `name` | TEXT | NO | UNIQUE |
| `description` | TEXT | YES | |
| `embedding_id` | TEXT | YES | |

---

### 3.4 `collections`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `name` | TEXT | NO | |
| `description` | TEXT | YES | |
| `created_at` | INTEGER | NO | |
| `updated_at` | INTEGER | NO | |
| `sort_order` | INTEGER | NO | DEFAULT 0 |

---

### 3.5 `book_author_cross_ref` (Junction)

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `book_id` | INTEGER | NO | PK (composite), FK → `books(id)` ON DELETE CASCADE |
| `author_id` | INTEGER | NO | PK (composite), FK → `authors(id)` ON DELETE CASCADE |

**Composite PK:** `(book_id, author_id)`
**Index:** `(author_id)`

---

### 3.6 `book_collection_cross_ref` (Junction)

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `book_id` | INTEGER | NO | PK (composite), FK → `books(id)` ON DELETE CASCADE |
| `collection_id` | INTEGER | NO | PK (composite), FK → `collections(id)` ON DELETE CASCADE |
| `added_at` | INTEGER | NO | |

**Composite PK:** `(book_id, collection_id)`
**Index:** `(collection_id)`

---

### 3.7 `reading_progress`

| Column | SQLite Type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT | |
| `book_id` | INTEGER | NO | UNIQUE, FK → `books(id)` ON DELETE CASCADE | One record per book |
| `status` | TEXT | NO | DEFAULT 'TO_READ' | Enum: `TO_READ`, `READING`, `HAVE_READ` |
| `current_page` | INTEGER | NO | DEFAULT 0 | 0-indexed |
| `read_percentage` | REAL | NO | DEFAULT 0.0 | 0.0 – 100.0 |
| `started_at` | INTEGER | YES | | Set when status → `READING` |
| `completed_at` | INTEGER | YES | | Set when status → `HAVE_READ` |
| `updated_at` | INTEGER | NO | | |

---

### 3.8 `bookmarks`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `book_id` | INTEGER | NO | FK → `books(id)` ON DELETE CASCADE |
| `page_number` | INTEGER | NO | |
| `label` | TEXT | YES | |
| `created_at` | INTEGER | NO | |
| `embedding_id` | TEXT | YES | |
| `source_text_hash` | TEXT | YES | |

**Index:** `(book_id)`

---

### 3.9 `quotes`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `book_id` | INTEGER | NO | FK → `books(id)` ON DELETE CASCADE |
| `page_number` | INTEGER | NO | |
| `text` | TEXT | NO | |
| `created_at` | INTEGER | NO | |
| `updated_at` | INTEGER | NO | |
| `embedding_id` | TEXT | YES | |
| `source_text_hash` | TEXT | YES | SHA-256 of `text` column |

**Index:** `(book_id)`

---

### 3.10 `notes`

| Column | SQLite Type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT | |
| `book_id` | INTEGER | NO | FK → `books(id)` ON DELETE CASCADE | |
| `page_number` | INTEGER | YES | | NULL = book-level note |
| `text` | TEXT | NO | | |
| `created_at` | INTEGER | NO | | |
| `updated_at` | INTEGER | NO | | |
| `linked_quote_id` | INTEGER | YES | FK → `quotes(id)` ON DELETE SET NULL | |
| `embedding_id` | TEXT | YES | | |
| `source_text_hash` | TEXT | YES | | |

**Indices:** `(book_id)`, `(linked_quote_id)`

---

### 3.11 `search_history`

| Column | SQLite Type | Nullable | Constraints |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `query` | TEXT | NO | |
| `searched_at` | INTEGER | NO | |
| `result_count` | INTEGER | YES | |

**Indices:** `(query)`, `(searched_at DESC)`

---

### 3.12 Entity Relationship Summary

```
series ──< books >── book_author_cross_ref >── authors
                │
                └── book_collection_cross_ref >── collections
                │
                └── reading_progress (1:1)
                │
                └── bookmarks (1:many)
                │
                └── quotes (1:many)
                        │
                        └── notes (linked via linked_quote_id, optional)

books ──< notes (1:many, book-level notes have null page_number)
```

---

### 3.13 Embedding-Ready Fields

These nullable columns are v1 placeholders. No embedding logic is implemented in this version. They exist to avoid a schema migration when local NLP is added.

| Entity | `embedding_id` | `source_text_hash` | Purpose |
|---|---|---|---|
| `books` | YES | YES | Embed full-text; hash detects file changes |
| `authors` | YES | — | Embed biography for profile generation |
| `series` | YES | — | Embed description for thematic search |
| `bookmarks` | YES | YES | Embed surrounding page context |
| `quotes` | YES | YES | Embed quote text directly; hash detects edits |
| `notes` | YES | YES | Embed note text; hash detects edits |

`embedding_id` is a `TEXT` reference to a future external or local vector store. It is never a FK to another Room table.

---

## 4. Key Architecture Patterns

### 4.1 PdfRenderer Sliding Window

**Problem:** A 500-page document at 1080×1920 px per page would need ~4 GB of `Bitmap` memory if all pages were held simultaneously. `PdfRenderer` is also not thread-safe and requires serial page access.

**Conceptual pipeline:**

```
User scrolls ──► visible page index changes
                         │
                         ▼
              PdfRenderWorker (coroutine, IO dispatcher,
              limitedParallelism = 1)
                         │
             ┌───────────┴───────────┐
             │  Compute window:      │
             │  [index-N .. index+N] │
             │  default N = 3        │
             └───────────┬───────────┘
                         │
          ┌──────────────┼───────────────┐
          ▼              ▼               ▼
    Already in      In window,       Outside window
    PdfPageCache    not cached       → evict + recycle()
    (no-op)         → render & store
```

**Component responsibilities:**

`PdfDocumentSession` — owns the open `PdfRenderer` and its `ParcelFileDescriptor`. Opened when the reader screen enters composition, closed on exit. All page access routes through this object. Exposes `suspend fun renderPage(pageIndex: Int, width: Int, height: Int): Bitmap` that opens the page, renders, and closes within a `Mutex`-guarded critical section. The `TextExtractionSourceImpl` also acquires this same mutex when extracting text — the two pipelines are serialized but unaware of each other.

`PdfPageCache` — `LinkedHashMap<Int, Bitmap>` with fixed capacity. Evicts the entry farthest from the current page when capacity is exceeded, calling `bitmap.recycle()` on eviction.

`PdfRenderWorker` — watches a `StateFlow<Int>` (current visible page). On each emission: evicts out-of-window pages, then iterates render priority `[current, current+1, current-1, current+2, current-2, ...]`, and for each uncached page launches a coroutine that calls `PdfDocumentSession.renderPage` and emits a `PdfPageBitmapState` update into the shared `MutableStateFlow<Map<Int, PdfPageBitmapState>>`.

`PdfPageBitmapState` (sealed class in `domain.model`):

```
object Idle
object Loading
data class Rendered(val bitmap: Bitmap)
data class Error(val cause: Throwable)
```

**Thread model:**

```
UI thread                IO dispatcher (limitedParallelism = 1)
    │                                     │
    │  currentPage = 47                   │
    ├────────────────────────────────────►│
    │                                     │  renderPage(47) → (48) → (46) → ...
    │  pageStates updates                 │
    │◄────────────────────────────────────┤
```

`limitedParallelism(1)` enforces serial `PdfRenderer` access without a dedicated thread.

---

### 4.2 Decoupled Reader State Flows

**Problem:** If `pageStates: Map<Int, PdfPageBitmapState>` lives inside `ReaderUiState.Success`, every bitmap update rebuilds the entire sealed class instance. Compose sees a new `uiState` value and recomposes `ReaderScreen`, which recomposes all children — including the top/bottom bars and progress slider — on every single page render. At typical window sizes of N=3 this triggers 6+ full-screen recompositions per page turn.

**Solution:** `ReaderViewModel` exposes two independent `StateFlow` properties. `ReaderScreen` collects them separately so Compose can scope recomposition to only the Composable that actually changed.

```
ReaderViewModel
│
├── val uiState: StateFlow<ReaderUiState>
│       Contains: book metadata, total page count, reading progress,
│                 bookmarks, overlay visibility, annotation panel state.
│       Changes on: navigation events, bookmark adds/removes,
│                   reading status changes, overlay toggle.
│
└── val pageStates: StateFlow<Map<Int, PdfPageBitmapState>>
        Contains: render state keyed by page index.
        Changes on: every render cycle (potentially several times per second
                    while scrolling).
```

**`ReaderUiState` shape (revised):**

```kotlin
sealed class ReaderUiState {
    object Loading : ReaderUiState()
    data class Success(
        val book: Book,
        val totalPages: Int,
        val currentPage: Int,
        val readingProgress: ReadingProgress,
        val bookmarks: List<Bookmark>,
        val isOverlayVisible: Boolean
    ) : ReaderUiState()
    data class Error(val message: String) : ReaderUiState()
}
```

`pageStates` is no longer a field on `Success`.

**Consumption in `ReaderScreen`:**

```
ReaderScreen
│
├── val uiState by viewModel.uiState.collectAsStateWithLifecycle()
│       → drives ReaderTopBar, ReaderBottomBar, PageSlider, panels
│
└── val pageStates by viewModel.pageStates.collectAsStateWithLifecycle()
        → passed only into PdfPageView(state = pageStates[currentPage])
```

Because `pageStates` is collected into a separate `State<Map<Int, PdfPageBitmapState>>`, only `PdfPageView` (and the `LazyPagerState` column it lives in) recomposes when a bitmap arrives. The top bar, bottom bar, and progress slider are stable across page renders.

**Recomposition scope diagram:**

```
ReaderScreen
├── ReaderTopBar          ← recomposes only on uiState changes
├── PdfPager
│   └── PdfPageView(pageStates[index])  ← recomposes on each bitmap update
├── ReaderBottomBar       ← recomposes only on uiState changes
└── PageSlider            ← recomposes only on currentPage changes
```

---

### 4.3 Text Extraction — API 33 Fallback Strategy

**Primary target device:** Android 13 (API 33). The native `PdfRenderer.Page.textContent` API requires API 35 and cannot be used as the sole extraction path.

**Two-tier extraction strategy in `TextExtractionSourceImpl`:**

```
fun extractPageText(bookId, pageNumber):
    if (Build.VERSION.SDK_INT >= 35)
        use PdfRenderer.Page.textContent        ← zero-dependency, fast
    else
        use PdfBoxAndroid (PDDocument)          ← Apache 2.0, local only
```

**Fallback library: PdfBox-Android** (`com.tom-roush:pdfbox-android`)

PdfBox-Android is a port of Apache PDFBox to Android. It parses the PDF content stream directly to extract text and runs entirely on-device with no network access. It is licensed under Apache 2.0.

Extraction via PdfBox-Android opens the PDF file independently from `PdfDocumentSession` — PdfBox does not use `PdfRenderer` and manages its own file handle. The `Mutex` in `PdfDocumentSession` governs `PdfRenderer` access only; PdfBox extraction is a separate I/O operation and does not compete for that lock.

**`TextExtractionSource` interface** (in `domain.source`):

```kotlin
interface TextExtractionSource {
    suspend fun extractPageText(bookId: Long, pageNumber: Int): String
    fun extractAllPages(bookId: Long): Flow<PageTextChunk>
}
```

**`PageTextChunk` domain model:**

| Field | Type | Notes |
|---|---|---|
| `bookId` | Long | |
| `pageNumber` | Int | |
| `text` | String | |
| `hash` | String | SHA-256 of `text`; matches `source_text_hash` in annotation entities |

**Decoupling guarantee:** Nothing in `feature.reader` calls `TextExtractionSource` directly. Features use use cases. Future embedding logic will be a new use case in `domain.usecase` consuming `TextExtractionSource` — the render pipeline has zero knowledge of text extraction.

---

### 4.4 Repository Interface Contract

Interfaces live in `domain.repository`; implementations in `data.repository`. Feature packages never import implementations directly — Hilt provides them.

```kotlin
interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getFavoriteBooks(): Flow<List<Book>>
    fun getTrashedBooks(): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun moveToTrash(id: Long)
    suspend fun restoreFromTrash(id: Long)
    suspend fun permanentlyDelete(id: Long)
    suspend fun toggleFavorite(id: Long)
}
```

`Flow`-returning functions map directly to Room DAO `@Query` methods — Room handles reactive emission. `suspend` functions wrap single-shot DAO operations. Entity-to-domain mappers live in `data.repository` alongside implementations; domain models never import Room annotations.

---

### 4.5 ViewModel State Flow (general pattern)

Every ViewModel exposes a single `uiState: StateFlow<T>` (plus `pageStates` in `ReaderViewModel` as described above). All data flows from repository `Flow` sources via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`. No `LiveData`. Internal mutations use `MutableStateFlow`; only `StateFlow` is exposed.

One-time events (navigation, toasts) use `Channel<UiEvent>` exposed as `receiveAsFlow()`.

---

### 4.6 Layered Flow (end-to-end example)

```
ReaderScreen (Composable)
    │  collects uiState + pageStates
    ▼
ReaderViewModel
    │  calls GetReadingProgressUseCase, drives PdfRenderWorker
    ▼
GetReadingProgressUseCase
    │  calls ReadingProgressRepository.getProgressForBook(bookId)
    ▼
ReadingProgressRepositoryImpl
    │  calls ReadingProgressDao.observeByBookId(bookId)
    ▼
Room (SQLite)  ──► Flow ──► propagates up to Composable
```

No layer skips another. Composables never call repositories. Use cases never import Room entities. DAOs never import domain models.

---

## 5. Coding Standards

### 5.1 Comment Policy

**Banned — section separator comments:**

```kotlin
// ========================================
// Public API
// ========================================
```

Use blank lines and logical grouping. `region` tags only in files exceeding 300 lines.

**Banned — self-evident docstrings.** A function named `getBookById` does not need `/** Returns the book with the given id */`.

**Banned — robotic inline comments:**

```kotlin
val book = Book(...)  // Create the book object
return book           // Return the result
```

Acceptable comment uses: non-obvious algorithm, platform bug workaround, spec/requirement citation by name.

---

### 5.2 Function Design

- One function, one responsibility. If you cannot describe a function in a single short phrase without "and", split it.
- Recommended maximum: 40 lines. Longer functions require review justification.
- No god objects. A class managing rendering, caching, progress, and annotation state simultaneously must be decomposed.

---

### 5.3 Architecture Boundaries

| Concern | Owner |
|---|---|
| Business rules, state transitions | Use cases in `domain.usecase` |
| Data access, entity mapping | Repositories in `data.repository` |
| UI state translation | ViewModels in `feature.*` |
| Rendering | Composables in `feature.*` |

Composables contain no `if` logic representing business rules. Conditional rendering based on `UiState` variants is permitted.

---

### 5.4 Dependency Injection

All dependencies provided by Hilt. Manual instantiation forbidden outside test fakes.

Each logical grouping has a corresponding `@Module` in `data.di`: `DatabaseModule`, `RepositoryModule`, `PdfModule`.

- `@Singleton`: DAOs, database, repositories, `PdfDocumentSession`
- `@HiltViewModel`: ViewModels (implicit)
- `@ViewModelScoped`: dependencies that must not outlive their ViewModel

---

### 5.5 Async and Threading

- Coroutines + `Flow` only. No RxJava.
- `Flow` for data streams; `suspend fun` for single-shot operations.
- `withContext(Dispatchers.IO)` wraps all blocking I/O, including PdfBox-Android extraction.
- `Dispatchers` are injected as `CoroutineDispatcher` constructor parameters for testability.

---

### 5.6 UI State Modeling

```kotlin
sealed class ExampleUiState {
    object Loading : ExampleUiState()
    data class Success(/* ... */) : ExampleUiState()
    data class Error(val message: String) : ExampleUiState()
}
```

`Loading` is always `object`. `Success` carries a complete UI data model (not raw domain models). `Error` carries a user-facing message string or resource ID — never a raw `Throwable`.

---

### 5.7 Naming Conventions

| Concept | Convention | Example |
|---|---|---|
| Room entity | `*Entity` | `BookEntity` |
| Domain model | no suffix | `Book` |
| Repository interface | `*Repository` | `BookRepository` |
| Repository implementation | `*RepositoryImpl` | `BookRepositoryImpl` |
| Use case | `*UseCase` | `ToggleFavoriteUseCase` |
| ViewModel | `*ViewModel` | `ReaderViewModel` |
| UI state sealed class | `*UiState` | `ReaderUiState` |
| Hilt module | `*Module` | `DatabaseModule` |
| Composable (screen) | `*Screen` | `ReaderScreen` |
| Composable (component) | `PascalCase`, no suffix | `BookCoverCard` |
| DAO | `*Dao` | `BookDao` |

---

### 5.8 Compose-Specific Rules

- Screen Composables (`*Screen`) accept a ViewModel via `hiltViewModel()` plus navigation lambdas. Nothing else.
- Component Composables accept data and callbacks as parameters only — no ViewModel references.
- `remember { mutableStateOf(...) }` only for purely local, ephemeral UI state (e.g., a dropdown flag). State surviving navigation belongs in the ViewModel.
- `LaunchedEffect` is for side-effects triggered by state changes. It is not a substitute for use cases.
- `ReaderScreen` must collect `uiState` and `pageStates` as separate `State` values, as described in section 4.2.

---

## 6. Dependency Catalog Reference

**`gradle/libs.versions.toml`**

```toml
[versions]
kotlin            = "2.1.0"
agp               = "8.9.2"
compose-bom       = "2025.05.00"
hilt              = "2.56.1"
room              = "2.7.1"
lifecycle         = "2.9.1"
navigation        = "2.9.0"
coroutines        = "1.10.2"
coil              = "2.7.0"
pdfbox            = "2.0.27.0"

[libraries]
androidx-compose-bom                 = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-compose-ui                  = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3           = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-ui-tooling-preview  = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-navigation-compose          = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
hilt-android                         = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler                        = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose              = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
room-runtime                         = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx                             = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler                        = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
kotlinx-coroutines-android           = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test              = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
coil-compose                         = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
pdfbox-android                       = { group = "com.tom-roush", name = "pdfbox-android", version.ref = "pdfbox" }
junit                                = { group = "junit", name = "junit", version = "4.13.2" }
mockk                                = { group = "io.mockk", name = "mockk", version = "1.13.13" }
turbine                              = { group = "app.cash.turbine", name = "turbine", version = "1.2.0" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android      = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose      = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt                = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp                 = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
room                = { id = "androidx.room", version.ref = "room" }
```

**Implementation notes:**

- KSP replaces kapt for both Room and Hilt. Do not use `annotationProcessor` or `kapt`.
- `PdfRenderer` is part of the Android framework — no additional dependency. Available from API 21.
- `pdfbox-android` is used only in `TextExtractionSourceImpl` behind the `Build.VERSION.SDK_INT < 35` branch. It opens its own file handle via `PDDocument.load(file)` and is independent of `PdfDocumentSession`.
- `PdfRenderer.Page.textContent` (text-layer extraction without rendering) requires API 35. The `if/else` branch in `TextExtractionSourceImpl` is the only place this check appears.
- `RoomTypeConverters.kt` handles `ReadingStatus` enum → `TEXT` storage. Add converters for any future collection-typed columns there.
- `series_position` is `REAL` to support inserting a book between positions without renumbering the sequence.
- `notes.page_number` nullable: `NULL` means a book-level note. The DAO query for book-level notes is `WHERE book_id = ? AND page_number IS NULL`.
