# Kronos — Architecture Reference

**Platform:** Android (minSdk 23, targetSdk 36)  
**Language:** Kotlin  
**UI toolkit:** Jetpack Compose + Material3  
**Database:** Room v3  
**DI:** Hilt/Dagger  

---

## 1. Overview

Kronos is a local-first Android PDF management application. All data is stored on-device in a single SQLite database managed by Room. There is no backend, no sync service, and no network dependency at runtime.

The project uses a **single `:app` module**. Package-based discipline enforces the same layering rules that separate Gradle modules would enforce:

```
feature.* → domain.* ← data.*
                      ↑
                   common.*
```

- `feature.*` imports from `domain.*` only — never from `data.*`
- `data.*` implements `domain.repository` interfaces and imports `domain.model` for mapping
- `domain.*` has zero Android framework dependencies
- Feature packages never share ViewModels or internal state with each other

---

## 2. Domain Layer

### 2.1 Enums

**`ReadingStatus`** — tracks where a book sits in the reading workflow

| Value | Meaning |
|---|---|
| `TO_READ` | Added to library, not started |
| `READING` | Actively in progress |
| `HAVE_READ` | Finished |

**`ViewMode`** — controls how the library list renders books

| Value | Layout |
|---|---|
| `COMPLETE` | Large cards with cover, title, progress bar |
| `QUICK` | Compact rows |
| `LIST` | `LazyColumn` with `ListItem` |
| `GRID` | `LazyVerticalGrid`, 3 columns |

**`SortMode`** — the active sort applied to any given library tab

| Value | `ORDER BY` logic |
|---|---|
| `RECENT` | `COALESCE(rp.updated_at, b.added_at) DESC` via LEFT JOIN on `reading_progress` |
| `TITLE` | `b.title ASC` |
| `AUTHOR` | Author name, joined through `book_author_cross_ref` |

**`CollectionViewMode`** — controls how the collections screen renders

| Value | Layout |
|---|---|
| `COVERS` | 2-column `LazyVerticalGrid`, taller aspect ratio cards |
| `GRID` | 3-column `LazyVerticalGrid`, square cards |
| `LIST` | `LazyColumn` |

---

### 2.2 Domain Models

All domain models are plain Kotlin data classes with no Room or Android annotations.

| Model | Key fields |
|---|---|
| `Book` | `id`, `title`, `filePath`, `fileUri`, `pageCount`, `coverImagePath`, `addedAt`, `isInTrash` |
| `ReadingProgress` | `bookId`, `status: ReadingStatus`, `currentPage`, `readPercentage`, `rating: Int?` (1–10), `review: String?`, `updatedAt` |
| `Collection` | `id`, `name`, `description`, `createdAt` |
| `Quote` | `id`, `bookId`, `pageNumber`, `text`, `createdAt` |
| `Note` | `id`, `bookId`, `pageNumber?`, `text` — nullable `pageNumber` means book-level note |
| `Bookmark` | `id`, `bookId`, `pageNumber`, `label?` |
| `BookAnnotationSummary` | `bookId`, `title`, `quoteCount`, `noteCount`, `latestAnnotationAt` — flattened read model for the Annotations Hub |
| `QuoteSummary` | Flattened quote + book title for the global quotes list |

---

### 2.3 Repository Interfaces

Interfaces live in `domain.repository`. Feature packages depend on these — never on implementations.

`Flow`-returning functions map to Room DAO reactive queries. `suspend` functions wrap single-shot DAO operations.

```kotlin
interface BookRepository {
    fun observeAll(sort: SortMode): Flow<List<Book>>
    fun observeByStatus(status: ReadingStatus, sort: SortMode): Flow<List<Book>>
    fun observeTrashed(): Flow<List<Book>>
    suspend fun getById(id: Long): Book?
    suspend fun add(book: Book): Long
    suspend fun moveToTrash(id: Long)
    suspend fun restoreFromTrash(id: Long)
    suspend fun permanentlyDelete(id: Long)
}

interface CollectionRepository {
    fun observeAll(): Flow<List<Collection>>
    fun observeBookCounts(): Flow<Map<Long, Int>>
    fun observeCollectionCovers(): Flow<Map<Long, List<String>>>
    suspend fun create(name: String, description: String?)
}

interface ReadingProgressRepository {
    fun observeByBookId(bookId: Long): Flow<ReadingProgress?>
    fun observeAll(): Flow<List<ReadingProgress>>
    suspend fun get(bookId: Long): ReadingProgress?
    suspend fun upsert(progress: ReadingProgress)
    suspend fun updateStatus(bookId: Long, status: ReadingStatus)
    suspend fun saveReview(bookId: Long, rating: Int?, review: String?)
}
```

---

### 2.4 Use Cases

One class, one operation. Use cases are the sole entry point for features into the domain.

```
domain/usecase/
├── book/
│   ├── GetAllBooksUseCase
│   ├── GetBooksByStatusUseCase
│   ├── GetBookByIdUseCase
│   ├── GetTrashedBooksUseCase
│   ├── GetBooksWithAnnotationsUseCase
│   ├── AddSpecificFilesUseCase
│   ├── ScanFolderUseCase
│   ├── MoveToTrashUseCase
│   ├── RestoreFromTrashUseCase
│   └── EmptyTrashUseCase
├── collection/
│   ├── GetCollectionsUseCase
│   ├── GetCollectionBookCountsUseCase
│   ├── GetCollectionCoversUseCase
│   ├── CreateCollectionUseCase
│   ├── AddBookToCollectionUseCase
│   └── RemoveBookFromCollectionUseCase
├── readingprogress/
│   ├── GetReadingProgressUseCase
│   ├── ObserveAllReadingProgressUseCase
│   ├── UpdateReadingProgressUseCase
│   ├── UpdateReadingStatusUseCase
│   └── SaveBookReviewUseCase
├── quote/
│   ├── GetAllQuotesUseCase
│   ├── GetQuotesForBookUseCase
│   ├── AddQuoteUseCase
│   └── DeleteQuoteUseCase
├── note/
│   ├── GetNotesForBookUseCase
│   ├── AddNoteUseCase
│   ├── UpdateNoteUseCase
│   └── DeleteNoteUseCase
└── bookmark/
    ├── GetBookmarksForBookUseCase
    ├── AddBookmarkUseCase
    └── DeleteBookmarkUseCase
```

---

## 3. Data Layer

### 3.1 Room Database

**Database name:** `kronos.db`  
**Current version:** 3

All timestamps are Unix epoch milliseconds stored as `INTEGER`. Enum columns are `TEXT` via `RoomTypeConverters`. `BOOLEAN` columns are `INTEGER` (0/1).

---

#### `books`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `title` | TEXT | NO | |
| `file_path` | TEXT | NO | UNIQUE |
| `file_uri` | TEXT | NO | Persistable content URI |
| `file_size_bytes` | INTEGER | NO | |
| `page_count` | INTEGER | NO | Set on first open |
| `cover_image_path` | TEXT | YES | Extracted thumbnail; used for collection cover grid |
| `added_at` | INTEGER | NO | |
| `last_opened_at` | INTEGER | YES | |
| `is_favorite` | INTEGER | NO | DEFAULT 0 |
| `is_in_trash` | INTEGER | NO | DEFAULT 0 |
| `trashed_at` | INTEGER | YES | |
| `series_id` | INTEGER | YES | FK → `series(id)` ON DELETE SET NULL |
| `series_position` | REAL | YES | Supports half-positions (e.g. 1.5) |
| `embedding_id` | TEXT | YES | Future NLP hook |
| `source_text_hash` | TEXT | YES | SHA-256 of full extracted text |

**Indices:** `(is_in_trash)`, `(is_favorite)`, `(added_at DESC)`

---

#### `reading_progress`

One row per book. `book_id` is UNIQUE.

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `book_id` | INTEGER | NO | UNIQUE, FK → `books(id)` ON DELETE CASCADE |
| `status` | TEXT | NO | DEFAULT `'TO_READ'` — enum `ReadingStatus` |
| `current_page` | INTEGER | NO | DEFAULT 0, 0-indexed |
| `read_percentage` | REAL | NO | DEFAULT 0.0, range 0.0–100.0 |
| `started_at` | INTEGER | YES | Set when status transitions to `READING` |
| `completed_at` | INTEGER | YES | Set when status transitions to `HAVE_READ` |
| `updated_at` | INTEGER | NO | Written on every progress save |
| `rating` | INTEGER | YES | **Added v3.** 1–10 star rating, null if unrated |
| `review` | TEXT | YES | **Added v3.** Free-text personal review |

The RECENT sort in `BookDao` uses `COALESCE(rp.updated_at, b.added_at) DESC` via a LEFT JOIN so books with no progress row still sort by import date.

---

#### `collections`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | PK, AUTOINCREMENT |
| `name` | TEXT | NO | |
| `description` | TEXT | YES | |
| `created_at` | INTEGER | NO | |
| `updated_at` | INTEGER | NO | |
| `sort_order` | INTEGER | NO | DEFAULT 0 |

---

#### `book_collection_cross_ref`

| Column | Type | Notes |
|---|---|---|
| `book_id` | INTEGER | PK (composite), FK → `books(id)` ON DELETE CASCADE |
| `collection_id` | INTEGER | PK (composite), FK → `collections(id)` ON DELETE CASCADE |
| `added_at` | INTEGER | ORDER BY column for cover selection |

**Index:** `(collection_id)`

---

#### `quotes`

| Column | Type | Null |
|---|---|---|
| `id` | INTEGER | NO |
| `book_id` | INTEGER | NO — FK → `books(id)` ON DELETE CASCADE |
| `page_number` | INTEGER | NO |
| `text` | TEXT | NO |
| `created_at` | INTEGER | NO |
| `updated_at` | INTEGER | NO |
| `embedding_id` | TEXT | YES |
| `source_text_hash` | TEXT | YES |

---

#### `notes`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | INTEGER | NO | |
| `book_id` | INTEGER | NO | FK → `books(id)` ON DELETE CASCADE |
| `page_number` | INTEGER | YES | NULL = book-level note |
| `text` | TEXT | NO | |
| `created_at` | INTEGER | NO | |
| `updated_at` | INTEGER | NO | |
| `linked_quote_id` | INTEGER | YES | FK → `quotes(id)` ON DELETE SET NULL |
| `embedding_id` | TEXT | YES | |
| `source_text_hash` | TEXT | YES | |

---

#### `bookmarks`

| Column | Type | Null |
|---|---|---|
| `id` | INTEGER | NO |
| `book_id` | INTEGER | NO — FK → `books(id)` ON DELETE CASCADE |
| `page_number` | INTEGER | NO |
| `label` | TEXT | YES |
| `created_at` | INTEGER | NO |

---

### 3.2 DAOs

**`BookDao`**

RECENT sort requires a LEFT JOIN so books with no `reading_progress` row are still returned:

```sql
SELECT b.* FROM books b
LEFT JOIN reading_progress rp ON b.id = rp.book_id
WHERE b.is_in_trash = 0
ORDER BY COALESCE(rp.updated_at, b.added_at) DESC
```

Status-filtered variant appends:
```sql
AND COALESCE(rp.status, 'TO_READ') = :status
```

**`CollectionDao`**

`observeCollectionCovers()` returns `Flow<List<CollectionCoverRow>>` — a projection of `(collection_id, cover_image_path)` for all non-trashed books with a non-null cover, ordered by `ref.added_at DESC`. The repository implementation groups by `collection_id` and calls `.take(4)` in Kotlin, avoiding `ROW_NUMBER()` window functions which require SQLite 3.25+ (Android 9+, minSdk is 23).

```kotlin
data class CollectionCoverRow(
    @ColumnInfo(name = "collection_id") val collectionId: Long,
    @ColumnInfo(name = "cover_image_path") val coverImagePath: String
)
```

**`ReadingProgressDao`**

Uses `@Upsert` for all progress saves. `observeByBookId` returns `Flow<ReadingProgressEntity?>` — nullable because a book has no progress row until it is first opened.

---

### 3.3 Database Migrations

**v2 → v3** — adds 10-star rating and personal review to `reading_progress`:

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE reading_progress ADD COLUMN rating INTEGER")
        db.execSQL("ALTER TABLE reading_progress ADD COLUMN review TEXT")
    }
}
```

Both columns are nullable so existing rows require no backfill.

---

### 3.4 Entity-to-Domain Mapping

Mapper functions live inside each `*RepositoryImpl`. Domain models never import Room annotations; entities never import domain models. This keeps the domain layer independently testable without any Android framework on the classpath.

---

## 4. UI & State Management

### 4.1 ViewModel Conventions

Every ViewModel exposes a single `uiState: StateFlow<T>`. Internal mutation uses `MutableStateFlow`; only `StateFlow` (or `.asStateFlow()`) is ever exposed to the screen. One-time events (navigation triggers, toasts) use `Channel<Event>` exposed via `.receiveAsFlow()`.

All `StateFlow` properties are built with:
```kotlin
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = /* Loading | emptyList() | emptyMap() */
)
```

The 5-second timeout keeps the upstream Room `Flow` alive through brief configuration changes without leaking it when the screen is off-screen longer.

---

### 4.2 SavedStateHandle Persistence

UI state that must survive both configuration changes and process death is backed by `SavedStateHandle`.

**`LibraryViewModel`** — per-tab view mode and sort mode

Stored as flat string keys, one per `ReadingStatus` value plus one for the all-books tab:

```
"view_all"       → ViewMode name for the unfiltered tab
"view_READING"   → ViewMode name for In Progress
"view_TO_READ"   → ViewMode name for To Do
"view_HAVE_READ" → ViewMode name for Completed
"sort_all"       → SortMode name for the unfiltered tab
"sort_READING"   → SortMode name for In Progress
...
```

`restoreViewModes()` and `restoreSortModes()` iterate `ReadingStatus.entries` on construction, reading each key via `runCatching { Enum.valueOf(...) }.getOrNull()` so a missing or invalid key silently falls back to the default. `onViewModeChange` and `onSortModeChange` write to the handle before updating the in-memory `MutableStateFlow`, so the handle is always ahead.

**`CollectionsViewModel`** — collection view mode

Single key `"collections_view_mode"` storing the active `CollectionViewMode` name. Read on init, written on every `onViewModeChange` call.

**`KronosNavGraph`** — active drawer status filter

The `statusFilter: ReadingStatus?` composable state uses `rememberSaveable` with a custom `Saver<ReadingStatus?, String>` that serializes the enum by name and restores via `ReadingStatus.valueOf`:

```kotlin
var statusFilter by rememberSaveable(
    stateSaver = Saver(
        save = { it?.name ?: "" },
        restore = { name -> if (name.isEmpty()) null else ReadingStatus.valueOf(name) }
    )
) { mutableStateOf<ReadingStatus?>(null) }
```

This survives rotation and the back-stack restoration triggered by drawer navigation.

---

### 4.3 Global Flow Safety (.catch)

Every `StateFlow` backed by a use case or Room `Flow` has a `.catch` operator immediately before `stateIn`. A silent Room exception would otherwise terminate the upstream `Flow`, leaving the screen frozen on its initial value.

**List/map flows:**
```kotlin
val books: StateFlow<List<Book>> = getBooks()
    .catch { emit(emptyList()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

**Sealed UiState flows:**
```kotlin
val uiState: StateFlow<LibraryUiState> = combine(...) { ... }
    .catch { e -> emit(LibraryUiState.Error(e.message ?: "Unexpected error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)
```

**Imperative collect (ProgressViewModel):**
```kotlin
combine(getNotes(bookId), getQuotes(bookId), getBookmarks(bookId))
    { notes, quotes, bookmarks -> buildState(...) as ProgressUiState }
    .catch { e -> emit(ProgressUiState.Error(e.message ?: "Unexpected error")) }
    .collect { _uiState.value = it }
```

ViewModels with `.catch` coverage: `LibraryViewModel`, `CollectionsViewModel`, `CollectionDetailViewModel`, `AnnotationsViewModel`, `AnnotationDetailViewModel`, `QuotesViewModel`, `TrashViewModel`, `ProgressViewModel`.

---

### 4.4 UiState Modeling

```kotlin
sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(
        val books: List<Book>,
        val viewMode: ViewMode,
        val sortMode: SortMode
    ) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}
```

`Loading` is always `object`. `Success` carries a complete, pre-filtered UI data model so Composables do no computation. `Error` carries a message string, never a raw `Throwable`. Screen Composables use exhaustive `when`:

```kotlin
when (val state = uiState) {
    is LibraryUiState.Loading -> LoadingIndicator()
    is LibraryUiState.Success -> LibraryContent(state)
    is LibraryUiState.Error   -> ErrorState(state.message)
}
```

---

### 4.5 Star Rating UI

`BookInfoScreen` renders a single row of 10 `Icon` composables for the rating selector. Filled stars (`Icons.Filled.Star`) are tinted `MaterialTheme.colorScheme.primary`; empty stars (`Icons.Outlined.StarBorder`) are tinted `onSurfaceVariant`. Tapping the currently selected star clears the rating (toggle-to-reset):

```kotlin
onSelect = { n -> selectedRating = if (selectedRating == n) null else n }
```

`Arrangement.SpaceEvenly` distributes touch targets across the full row width.

---

### 4.6 Spotify-Style Collection Covers

When a collection has at least 4 books with cover images, `CollectionsScreen` renders a 2×2 `AsyncImage` grid instead of the name-text placeholder. Each `AsyncImage` uses `ContentScale.Crop` and `Modifier.weight(1f)` inside a `Column` of two `Row`s. The collection name always renders below the grid in a `Text` composable, matching the padding and typography of the placeholder. Collections with fewer than 4 covers fall back to the centered name text.

Cover paths come from `GetCollectionCoversUseCase` → `CollectionRepositoryImpl` → `CollectionDao.observeCollectionCovers()`. The DAO returns all eligible cover paths ordered by recency; the repository groups by `collection_id` and calls `.take(4)`.

---

## 5. Navigation

### 5.1 KronosNavGraph

`KronosNavGraph` owns both the `NavController` and the `ModalNavigationDrawer`. It is called directly from `MainActivity`. The `NavHost` is wrapped in `Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background)` so the background is always painted, even if a child composable renders nothing.

### 5.2 Route Definitions

```
LIBRARY                              — main library screen
reader/{bookId}                      — PDF reader
info/{bookId}                        — book detail / review sheet
progress/{bookId}                    — reading progress + annotation density
COLLECTIONS                          — collections grid/list
collection/{collectionId}            — books inside one collection
QUOTES                               — annotations hub (book list)
annotations/{bookId}/{type}          — quotes or notes for one book
TRASH                                — recently deleted books
SETTINGS                             — app settings
```

### 5.3 ModalNavigationDrawer

| Drawer item | Navigates to | `statusFilter` |
|---|---|---|
| Library (All) | `LIBRARY` | `null` |
| In Progress | `LIBRARY` | `ReadingStatus.READING` |
| To Do | `LIBRARY` | `ReadingStatus.TO_READ` |
| Completed | `LIBRARY` | `ReadingStatus.HAVE_READ` |
| Collections | `COLLECTIONS` | — |
| Quotes & Notes | `QUOTES` | — |
| Trash | `TRASH` | — |

### 5.4 Backstack Pattern

Every drawer navigation call uses:

```kotlin
navController.navigate(route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

`saveState = true` preserves the popped destination's scroll position and ViewModel state. `restoreState = true` restores it when revisited. Each drawer tab gets independent, persistent scroll and filter state without multiple ViewModel instances.

### 5.5 Argument Validation

Destinations requiring a `bookId` guard against invalid deep links:

```kotlin
val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
if (bookId <= 0L) {
    LaunchedEffect(Unit) { navController.popBackStack() }
    return@composable
}
```

Applied to `reader/{bookId}`, `info/{bookId}`, and `progress/{bookId}`.

---

## 6. Module Layout & Directory Tree

```
Kronos_v10/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── app/src/main/java/com/kronos/
    ├── KronosApp.kt
    ├── MainActivity.kt
    ├── navigation/
    │   ├── KronosNavGraph.kt
    │   └── NavRoutes.kt
    ├── common/
    │   ├── AppConstants.kt
    │   ├── DateUtils.kt
    │   ├── FileUtils.kt
    │   ├── HashUtils.kt
    │   ├── IoDispatcher.kt
    │   └── KronosResult.kt
    ├── data/
    │   ├── database/
    │   │   ├── KronosDatabase.kt
    │   │   ├── DatabaseMigrations.kt
    │   │   ├── converter/RoomTypeConverters.kt
    │   │   ├── dao/
    │   │   │   ├── BookDao.kt
    │   │   │   ├── BookmarkDao.kt
    │   │   │   ├── CollectionDao.kt
    │   │   │   ├── NoteDao.kt
    │   │   │   ├── QuoteDao.kt
    │   │   │   ├── ReadingProgressDao.kt
    │   │   │   └── SearchHistoryDao.kt
    │   │   └── entity/
    │   │       ├── BookAuthorCrossRef.kt
    │   │       ├── BookCollectionCrossRef.kt
    │   │       ├── BookEntity.kt
    │   │       ├── BookmarkEntity.kt
    │   │       ├── CollectionEntity.kt
    │   │       ├── NoteEntity.kt
    │   │       ├── QuoteEntity.kt
    │   │       ├── ReadingProgressEntity.kt
    │   │       └── SearchHistoryEntity.kt
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
    │       ├── BookRepositoryImpl.kt
    │       ├── BookmarkRepositoryImpl.kt
    │       ├── CollectionRepositoryImpl.kt
    │       ├── NoteRepositoryImpl.kt
    │       ├── QuoteRepositoryImpl.kt
    │       ├── ReadingProgressRepositoryImpl.kt
    │       └── SearchHistoryRepositoryImpl.kt
    ├── domain/
    │   ├── model/
    │   │   ├── Book.kt
    │   │   ├── Bookmark.kt
    │   │   ├── BookAnnotationSummary.kt
    │   │   ├── Collection.kt
    │   │   ├── Note.kt
    │   │   ├── PageTextChunk.kt
    │   │   ├── PdfPageBitmapState.kt
    │   │   ├── Quote.kt
    │   │   ├── QuoteSummary.kt
    │   │   ├── ReadingProgress.kt
    │   │   ├── ReadingStatus.kt
    │   │   ├── SortMode.kt
    │   │   └── ViewMode.kt
    │   ├── repository/
    │   │   ├── BookRepository.kt
    │   │   ├── BookmarkRepository.kt
    │   │   ├── CollectionRepository.kt
    │   │   ├── NoteRepository.kt
    │   │   ├── QuoteRepository.kt
    │   │   ├── ReadingProgressRepository.kt
    │   │   └── SearchHistoryRepository.kt
    │   ├── source/TextExtractionSource.kt
    │   └── usecase/
    │       ├── book/
    │       ├── bookmark/
    │       ├── collection/
    │       ├── note/
    │       ├── quote/
    │       └── readingprogress/
    ├── ui/
    │   ├── component/
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
    │   └── util/ComposeExtensions.kt
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
        │   │   └── SortMenuSheet.kt
        │   ├── info/
        │   │   ├── BookInfoScreen.kt
        │   │   └── BookInfoViewModel.kt
        │   ├── quotes/
        │   │   ├── AnnotationDetailScreen.kt
        │   │   ├── AnnotationDetailViewModel.kt
        │   │   ├── AnnotationsScreen.kt
        │   │   ├── AnnotationsViewModel.kt
        │   │   ├── QuotesScreen.kt
        │   │   └── QuotesViewModel.kt
        │   └── trash/
        │       ├── TrashScreen.kt
        │       └── TrashViewModel.kt
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
        │   ├── panel/
        │   │   ├── BookmarkPanel.kt
        │   │   ├── NotePanel.kt
        │   │   └── QuotePanel.kt
        │   └── progress/
        │       ├── ProgressDetailScreen.kt
        │       ├── ProgressUiState.kt
        │       └── ProgressViewModel.kt
        └── settings/
            ├── SettingsScreen.kt
            ├── SettingsUiState.kt
            └── SettingsViewModel.kt
```

---

## 7. PDF Rendering

### 7.1 Sliding Window Cache

`PdfRenderWorker` watches a `StateFlow<Int>` (current visible page). On each emission it evicts pages outside `[current - N .. current + N]` (default N = 3) from `PdfPageCache`, then renders uncached pages in priority order: `[current, current+1, current-1, current+2, current-2, ...]`.

`PdfDocumentSession` owns the open `PdfRenderer` and its `ParcelFileDescriptor`. All page access routes through a `Mutex`-guarded `suspend fun renderPage(index, width, height): Bitmap`. `limitedParallelism(1)` on the IO dispatcher enforces serial `PdfRenderer` access without a dedicated thread.

`PdfPageCache` is a `LinkedHashMap<Int, Bitmap>` with fixed capacity. Eviction calls `bitmap.recycle()`.

`PdfPageBitmapState` (sealed, in `domain.model`): `Idle`, `Loading`, `Rendered(bitmap)`, `Error(cause)`.

### 7.2 Decoupled ReaderViewModel Flows

`ReaderViewModel` exposes two independent `StateFlow` properties so bitmap updates do not trigger full-screen recomposition:

- `uiState: StateFlow<ReaderUiState>` — book metadata, page count, bookmarks, overlay state
- `pageStates: StateFlow<Map<Int, PdfPageBitmapState>>` — render state keyed by page index

`ReaderScreen` collects them into separate `State` objects. Only the `PdfPageView` subtree recomposes on bitmap updates; `ReaderTopBar`, `ReaderBottomBar`, and `PageSlider` are stable across render cycles.

### 7.3 Text Extraction

`TextExtractionSourceImpl` branches on API level:

- **API 35+** — `PdfRenderer.Page.textContent` (framework, zero dependency)
- **API 23–34** — PdfBox-Android (`com.tom-roush:pdfbox-android`), opens its own file handle via `PDDocument.load(file)`, independent of `PdfDocumentSession`'s mutex

---

## 8. Dependency Injection

| Module | Provides |
|---|---|
| `DatabaseModule` | `KronosDatabase` `@Singleton`, all DAOs |
| `RepositoryModule` | All `*RepositoryImpl` bound to their interfaces `@Singleton` |
| `PdfModule` | `PdfDocumentSession`, `PdfRenderWorker`, `TextExtractionSourceImpl` `@ViewModelScoped` |

KSP handles annotation processing for both Hilt and Room. `kapt` is not used.

`@IoDispatcher` is a Hilt qualifier binding `Dispatchers.IO`, injected into ViewModels and repositories that perform blocking I/O. Tests swap in `UnconfinedTestDispatcher` without subclassing.

---

## 9. Coding Standards

**No AI fingerprints.** No section-separator comment blocks, no self-evident docstrings, no inline comments narrating what the code obviously does. Comments appear only for non-obvious constraints, platform quirks, or spec citations.

**Composables** accept data and callbacks only — no ViewModel references in component Composables. Screen Composables (`*Screen`) receive a ViewModel via `hiltViewModel()` and navigation lambdas.

**`remember { mutableStateOf(...) }` only for ephemeral local UI state.** Any state that must survive navigation or configuration change belongs in the ViewModel backed by `SavedStateHandle`.

**`LaunchedEffect` is for side-effects**, not a substitute for use cases or ViewModel logic.

**No `MutableStateFlow` exposed to the UI.** Always `.asStateFlow()` or a read-only `StateFlow` property.

**No layer skipping.** Composables never call repositories. Use cases never import Room entities. DAOs never import domain models.
