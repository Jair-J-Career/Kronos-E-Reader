# Kronos

Your PDFs, your reading habits, your data — on your device, fully under your control.

Kronos is an Android PDF reader built for people who take their reading seriously. It automatically tracks what you're reading, how far you've gotten, and everything you've marked along the way, without any cloud accounts, subscriptions, or data leaving your phone.

---

## Features

### Smart Library

Kronos organizes your PDFs into three automatic queues the moment you import them: **In Progress**, **To Do**, and **Completed**. No manual sorting required. Each tab remembers your preferred view and sort independently — switch between a visual cover grid and a compact list, or sort by most recently read versus alphabetically, and Kronos keeps those preferences per tab across sessions.

Books sort by reading activity, not import date. The book you picked up last night sits at the top of your In Progress list, not buried under whatever you imported months ago.

### Collections

Group any set of PDFs into a custom collection — a course reading list, a research topic, a series. Collections are visual: when a collection has four or more books with cover art, it automatically displays a 2×2 cover mosaic instead of a plain label, so your library feels like a shelf rather than a file browser.

### Annotations Hub

Every quote you highlight and note you write in the reader is collected in a dedicated Annotations view, organized by book. Quotes and notes are mapped to the exact page they came from, so you can jump back to context without digging through the PDF manually. The hub sorts by most recently annotated or by annotation density — useful when you want to return to the books you engaged with most.

### Insights & Reviews

Open any book's detail screen to see a live reading progress breakdown, an annotation density chart across eight equal sections of the book, and all your bookmarks in one place. When you finish a book, leave a personal rating out of 10 stars and a written review. Ratings save to the same local database as everything else — no account required.

### Privacy First

Kronos is 100% offline. There are no analytics, no tracking, no cloud sync, and no account. Everything — your library, your progress, your annotations, your reviews — lives in a single SQLite database on your device. Uninstall the app and the data goes with it.

---

## Technical

- **Android** — minimum API 23 (Android 6.0), targets API 36
- **Local storage** — Room (SQLite), no external database or sync
- **PDF rendering** — Android's native `PdfRenderer`, with PdfBox-Android for text extraction on API 34 and below
- **No network permissions**

---

## Status

Active development. Core reading, tracking, annotation, collection, and review features are complete.
