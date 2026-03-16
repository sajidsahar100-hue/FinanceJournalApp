# Finance Journal App — Android

A double-entry finance journal for Android with vendor tracking and export.

## Features
- **Daily Journal** — Add entries with date, description, amount (Credit / Debit)
- **Vendor Journals** — Tag any entry to a vendor; view per-vendor ledger separately
- **Export** — Share the main journal *or* any vendor journal as **.xls** or **.pdf**
- **Edit / Delete** — Long-press actions on any entry
- **Balance Summary** — Running credit, debit, and net balance shown at a glance

## Tech Stack
| Layer       | Library                                 |
|-------------|----------------------------------------|
| UI          | Jetpack Compose + Material 3           |
| Navigation  | Navigation Compose                     |
| Database    | Room (SQLite)                          |
| Architecture| MVVM (ViewModel + LiveData/Flow)        |
| Excel       | Apache POI 3.17 (.xls)                 |
| PDF         | Android built-in `PdfDocument`         |

## How to Build
1. Open the project in **Android Studio Hedgehog** (2023.1.1) or newer.
2. Let Gradle sync.
3. Run on any device / emulator running **Android 8.0+ (API 26+)**.

## ProGuard (release builds)
The `app/proguard-rules.pro` already includes rules for Apache POI.

## Project Structure
```
app/src/main/java/com/financeapp/
  MainActivity.kt
  data/           — Room entities & DAOs
  repository/     — JournalRepository
  viewmodel/      — JournalViewModel
  ui/
    navigation/   — AppNavigation (NavHost)
    screens/      — MainJournal, AddEntry, Vendors, VendorJournal, Export
  utils/          — ExcelExporter, PdfExporter, FormatUtils
```
