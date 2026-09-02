# PROJECT_DOCS.md — RafiqAlDhikr

## 1. Project Overview

- **App Name**: RafiqAlDhikr (رفيق الذكر)
- **App ID/Application ID**: `app.rafiqaldhikr`
- **Purpose**: A comprehensive Islamic companion app providing daily adhkar (remembrance of Allah), Quran reading, prayer times, Qibla compass, duas, tasbeeh counter, and spiritual content.
- **Target Platform**: Android (primary), with Kotlin Multiplatform support for iOS
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Architecture**: Kotlin Multiplatform (KMP) with shared business logic via `:shared` module and Android-specific UI via `:androidApp`

---

## 2. Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                  androidApp (UI Layer)               │
│  Compose UI / Screens / ViewModels / Navigation     │
├─────────────────────────────────────────────────────┤
│                  shared (Business Logic)              │
│  Domain Models / Repositories / Use Cases / DI      │
├─────────────────────────────────────────────────────┤
│                  shared (Data Layer)                 │
│  SQLDelight Database / Ktor HTTP Client / JSON      │
└─────────────────────────────────────────────────────┘
```

### Layers

- **UI Layer** (`androidApp`): Jetpack Compose screens, ViewModels (Android), navigation, theme
- **Domain Layer** (`shared/commonMain`): Domain models, repository interfaces, use cases
- **Data Layer** (`shared/commonMain`): Repository implementations, SQLDelight queries, Ktor API client
- **Platform Layer** (`shared/androidMain`, `shared/iosMain`): Platform-specific implementations (e.g., `PrayerTimeCalculator`, `JsonResourceReader`)

### Communication Patterns

- **Local Database**: SQLDelight with SQLite for persistent local storage
- **Remote API**: Ktor HTTP client for fetching remote content (Khatira, AppConfig)
- **Dependency Injection**: Koin for both `shared` and `androidApp` modules
- **Coroutines/Flows**: Reactive data streams between repository and UI layers
- **State Management**: ViewModels with `StateFlow`/`MutableStateFlow` per screen; Compose `collectAsStateWithLifecycle`

---

## 3. Folder Structure

```
rafiq-aldhikr/
├── androidApp/                          # Android application module
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # App manifest (permissions, components)
│   │   ├── assets/                      # JSON seed data (Quran, Adhkar, Duas, Khatira)
│   │   │   ├── adhkar_morning.json
│   │   │   ├── adhkar_evening.json
│   │   │   ├── adhkar_sleep.json
│   │   │   ├── adhkar_prayer.json
│   │   │   ├── adhkar_misc.json
│   │   │   ├── duas.json
│   │   │   ├── khatira_366.json
│   │   │   ├── quran_uthmani.json
│   │   │   └── surah_metadata.json
│   │   ├── kotlin/app/rafiqaldhikr/
│   │   │   ├── MainActivity.kt          # Single activity, Compose entry point
│   │   │   ├── RafiqApplication.kt      # Application class (Koin setup)
│   │   │   ├── di/                      # Android DI modules
│   │   │   │   ├── AppModule.kt         # ViewModel bindings
│   │   │   │   ├── ServiceModule.kt     # PrayerAlarmManager, CompassManager
│   │   │   │   ├── SecurityModule.kt    # EncryptedSharedPreferences
│   │   │   │   └── ViewModelModule.kt   # All ViewModels via viewModelOf()
│   │   │   ├── service/                 # Android services & receivers
│   │   │   │   ├── BootReceiver.kt      # Reschedules alarms on boot
│   │   │   │   ├── CompassManager.kt     # SensorManager for Qibla compass
│   │   │   │   ├── PrayerAlarmManager.kt # AlarmManager for prayer notifs
│   │   │   │   ├── PrayerAlarmReceiver.kt
│   │   │   │   └── QuranAudioService.kt # Media3 MediaSessionService
│   │   │   ├── ui/
│   │   │   │   ├── navigation/          # NavHost, Routes, BottomBar
│   │   │   │   ├── screens/             # All Compose screens (35+)
│   │   │   │   │   ├── adhkar/           # Adhkar categories, reading, celebration
│   │   │   │   │   ├── dua/              # Dua categories, list, emotional
│   │   │   │   │   ├── garden/           # Gamification garden
│   │   │   │   │   ├── home/             # Home screen + ViewModel
│   │   │   │   │   ├── khatira/          # Daily khatira reflection
│   │   │   │   │   ├── onboarding/       # First-launch onboarding
│   │   │   │   │   ├── prayer/           # Prayer times, method, tracking
│   │   │   │   │   ├── premium/          # Monetization screen
│   │   │   │   │   ├── profile/          # User profile
│   │   │   │   │   ├── qibla/            # Qibla compass
│   │   │   │   │   ├── quran/            # Quran list, reading, search, bookmarks, audio
│   │   │   │   │   ├── settings/         # Settings, theme, font, notifications, accessibility
│   │   │   │   │   ├── statistics/      # Weekly/overall stats
│   │   │   │   │   ├── tasbeeh/          # Tasbeeh counter
│   │   │   │   │   └── [other]/          # About, help, legal, export, widget, etc.
│   │   │   │   ├── components/           # Reusable UI components
│   │   │   │   ├── theme/                # AppColors, RafiqTheme, Typography, Shapes
│   │   │   │   ├── animations/          # IslamicAnimations, MicroAnimations
│   │   │   │   └── utils/                # ConnectivityObserver, PermissionHandler
│   │   │   └── widget/
│   │   │       └── PrayerWidget.kt       # Glance home-screen widget
│   │   └── res/                          # Android resources (themes, strings, drawables)
│   └── build.gradle.kts
│
├── shared/                             # Kotlin Multiplatform shared module
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/app/rafiq/
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/              # DatabaseSeeder, DatabaseDriverFactory
│   │   │   │   │   ├── model/           # JSON DTOs (JsonModels.kt)
│   │   │   │   │   ├── remote/          # RafiqApi (Ktor), HttpClientFactory
│   │   │   │   │   └── repository/      # 9 repository implementations
│   │   │   │   ├── di/
│   │   │   │   │   └── SharedModule.kt  # Koin module for shared deps
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # DomainModels, PrayerTimesResult, RafiqResult
│   │   │   │   │   ├── repository/      # 9 repository interfaces
│   │   │   │   │   └── usecase/         # 6 use cases
│   │   │   │   └── platform/            # PlatformContext, PlatformCrypto, JsonResourceReader
│   │   │   └── sqldelight/app/rafiq/db/ # 12 .sq migration files
│   │   ├── androidMain/                 # Android platform implementations
│   │   │   └── kotlin/app/rafiq/
│   │   │       ├── domain/model/
│   │   │       │   └── PrayerTimeCalculator.kt  # Actual impl using Aladhan API
│   │   │       └── data/db/
│   │   │           └── DatabaseDriverFactory.kt  # Android SQLDelight driver
│   │   └── iosMain/                     # iOS platform implementations (stub)
│   │       └── kotlin/app/rafiq/
│   │           ├── domain/model/PrayerTimeCalculator.kt
│   │           └── data/db/DatabaseDriverFactory.kt
│   └── build.gradle.kts
│
├── gradle/
│   ├── wrapper/                         # Gradle wrapper
│   └── libs.versions.toml              # Centralized version catalog
├── build.gradle.kts                    # Root build file
├── settings.gradle.kts                 # Project settings
├── gradle.properties                   # Gradle/Android properties
├── local.properties                    # SDK/local paths (not committed)
└── build_error.log / build_output.log  # Build artifacts
```

---

## 4. Tech Stack & Dependencies

### Framework & Language

| Category | Name | Version |
|----------|------|---------|
| Language | Kotlin | 2.2.10 |
| Android Gradle Plugin | AGP | 9.1.0 |
| Multiplatform | Kotlin Multiplatform (KMP) | 2.2.10 |
| Compose Compiler | kotlin.plugin.compose | 2.2.10 |
| Compose BOM | androidx.compose:compose-bom | 2024.12.01 |

### UI

| Library | Version |
|---------|---------|
| Jetpack Compose (UI, Graphics, Foundation) | BOM-managed |
| Compose Material3 | BOM-managed |
| Compose Material Icons Extended | BOM-managed |
| Compose Animation | BOM-managed |
| Navigation Compose | 2.8.5 |
| Lifecycle ViewModel Compose | 2.8.7 |
| Lifecycle Runtime Compose | 2.8.7 |
| Activity Compose | 1.9.3 |
| Glance AppWidget | 1.1.1 |

### Database & Storage

| Library | Version |
|---------|---------|
| SQLDelight Runtime | 2.0.2 |
| SQLDelight Coroutines | 2.0.2 |
| SQLDelight Android Driver | 2.0.2 |
| SQLDelight Native Driver | 2.0.2 |
| DataStore Preferences | 1.1.1 |
| Security Crypto | 1.1.0-alpha06 |

### Networking

| Library | Version |
|---------|---------|
| Ktor Client Core | 2.3.13 |
| Ktor Client Content Negotiation | 2.3.13 |
| Ktor Serialization (Kotlinx JSON) | 2.3.13 |
| Ktor Client OkHttp | 2.3.13 |
| Ktor Client Darwin | 2.3.13 |

### DI

| Library | Version |
|---------|---------|
| Koin Core | 4.0.0 |
| Koin Android | 4.0.0 |
| Koin AndroidX Compose | 4.0.0 |

### Utilities

| Library | Version |
|---------|---------|
| Kotlinx Coroutines | 1.9.0 |
| Kotlinx Serialization JSON | 1.7.3 |
| Kotlinx DateTime | 0.6.1 |

### Platform / Device

| Library | Version |
|---------|---------|
| Play Services Location | 21.3.0 |
| Media3 ExoPlayer | 1.5.1 |
| Media3 Session | 1.5.1 |
| Media3 UI | 1.5.1 |
| Work Manager | 2.10.0 |
| SplashScreen | 1.0.1 |
| AppCompat | 1.7.0 |
| Core KTX | 1.15.0 |

### Prayer / Location

| Library | Version |
|---------|---------|
| Adhan2 (Prayer time library) | 0.0.5 |

### Monetization (TODO — commented out)

| Library | Version |
|---------|---------|
| RevenueCat Purchases | 8.12.1 |
| RevenueCat Purchases UI | 8.12.1 |

### Analytics/Crash (TODO — commented out)

| Library | Version |
|---------|---------|
| Firebase Crashlytics | 3.0.2 |
| Google Services Gradle Plugin | 4.4.2 |

### In-App Updates

| Library | Version |
|---------|---------|
| Play App Update KTX | 2.1.0 |
| Play Review KTX | 2.0.2 |

### Testing

| Library | Version |
|---------|---------|
| JUnit | 4.13.2 |
| Kotlin Test | 2.2.10 |
| Kotlinx Coroutines Test | 1.9.0 |
| Turbine | 1.2.0 |
| MockK | 1.13.13 |
| Compose UI Test JUnit4 | BOM-managed |

---

## 5. Environment & Configuration

### Environment Variables (for CI/CD Signing)

| Variable | Description |
|----------|-------------|
| `KEYSTORE_FILE` | Path to Java keystore for release signing |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias name |
| `KEY_PASSWORD` | Key password |

### Gradle Properties (`gradle.properties`)

| Property | Value |
|----------|-------|
| `org.gradle.jvmargs` | `-Xmx4096m -Dfile.encoding=UTF-8` |
| `android.useAndroidX` | `true` |
| `kotlin.code.style` | `official` |
| `android.nonTransitiveRClass` | `true` |
| `kotlin.mpp.stability.nowarn` | `true` |
| `android.enableEdgeToEdge` | `true` |
| `org.gradle.java.home` | JDK 21 path |
| `android.defaults.buildfeatures.resvalues` | `true` |
| `android.sdk.defaultTargetSdkToCompileSdkIfUnset` | `false` |

### Config Files

| File | Purpose |
|------|---------|
| `gradle/libs.versions.toml` | Centralized version catalog |
| `settings.gradle.kts` | Project includes and repository mode |
| `build.gradle.kts` (root) | Plugin declarations |
| `androidApp/build.gradle.kts` | Android app dependencies and build config |
| `shared/build.gradle.kts` | KMP module config, SQLDelight database config |
| `proguard-rules.pro` | R8/ProGuard rules |
| `androidApp/src/main/res/xml/network_security_config.xml` | Network security (allows cleartext for debug?) |
| `androidApp/src/main/res/xml/locales_config.xml` | Supported locales |
| `androidApp/src/main/res/xml/prayer_widget_info.xml` | Widget configuration |

### TODO Configuration Items

The following are commented out in `RafiqApplication.kt` and `androidApp/build.gradle.kts` pending configuration:

1. **Firebase Crashlytics** — requires `google-services.json`
2. **RevenueCat** — requires API key in `local.properties` as `REVENUECAT_API_KEY`
3. **Google Play Services** — Google Services plugin commented out

---

## 6. Database & Data Models

### Database: `RafiqDatabase` (SQLDelight)

**Package**: `app.rafiq.db`

#### Tables

| Table | Key Fields | Description |
|-------|------------|-------------|
| `UserPrefs` | `id (PK, DEFAULT 1)` | App preferences (theme, prayer method, location, offsets, accessibility) |
| `Surah` | `number (PK)` | Quran surah metadata (114 surahs) |
| `Ayah` | `id (PK)` (surah*1000+ayah) | Quran verse text in Uthmani & simple script |
| `Adhkar` | `id (PK, AUTOINCREMENT)` | Islamic remembrances categorized (morning, evening, sleep, prayer, misc) |
| `Dua` | `id (PK, AUTOINCREMENT)` | Supplications with categories, occasions, favorites |
| `Khatira` | `id (PK, AUTOINCREMENT)` | 366 daily spiritual reflections (verses/hadiths) |
| `PrayerLog` | `(date, prayer_name) UNIQUE` | Daily prayer tracking (prayed, in_masjid, sunnah) |
| `DailyProgress` | `date (PK, UNIQUE)` | Aggregated daily stats (morning/evening adhkar, Quran pages, tasbeeh, prayers, minutes) |
| `StreakData` | `id (PK, DEFAULT 1)` | Current/longest streak counters |
| `StreakHistory` | `date (PK, UNIQUE)` | Dates with recorded activity |
| `QuranBookmark` | `id (PK, AUTOINCREMENT)` | Bookmarked ayahs with notes |
| `QuranLastRead` | `id (PK, DEFAULT 1)` | Last reading position (surah, ayah, page, scrollY) |
| `TasbeehSession` | `id (PK, AUTOINCREMENT)` | Individual tasbeeh sessions with count, target, duration |

### Domain Models

| Model | Fields |
|-------|--------|
| `Dhikr` | id, category, textAr, source, sourceGrade, virtue, count, audioFile, sortOrder |
| `Khatira` | id, dayOfYear, verseOrHadith, source, reflection, season, reviewed, remoteVersion |
| `SurahInfo` | number, nameAr, nameEn, nameTranslit, revelation, ayahCount, juzStart, pageStart |
| `AyahInfo` | id, surah, ayahNumber, textUthmani, textSimple, juz, hizb, page |
| `DuaItem` | id, category, occasion, textAr, source, sourceGrade, isFavorite, sortOrder |
| `PrayerEntry` | id, date, prayerName, prayed, inMasjid, sunnahDone |
| `DailyProgressInfo` | date, morningDone, eveningDone, quranPages, tasbeehCount, prayersLogged, totalMinutes |
| `StreakInfo` | current, longest, lastActiveDate |
| `UserPrefsInfo` | 25+ fields: theme, dynamicColor, fontScale, prayerMethod, location, offsets, madhab, accessibility settings |
| `LastReadPosition` | surah, ayah, page, scrollY |
| `QuranBookmark` | id, surah, ayah, page, createdAt, note |

### Database Seeding

On first launch (`DatabaseSeeder.seedIfNeeded()`):
- `Surah` and `Ayah` are seeded from `surah_metadata.json` + `quran_uthmani.json`
- `Adhkar` categories seeded from 5 JSON files (`adhkar_morning.json`, etc.)
- `Dua` seeded from `duas.json`
- `Khatira` seeded from `khatira_366.json`
- `UserPrefs` initialized with defaults via `initIfNeeded`

---

## 7. API Reference

### Base URL

`https://api.rafiqaldhikr.app/v1`

### Endpoints

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/khatira/{dayOfYear}` | Get daily khatira by day of year (1-366) | `KhatiraDto` |
| `GET` | `/config` | Get app configuration (minVersion, maintenanceMode) | `AppConfigDto` |
| `GET` | `/content/version` | Get content version info (adhkarVersion, khatiraVersion) | `ContentVersionDto` |

### DTOs

```kotlin
@Serializable data class KhatiraDto(
    val dayOfYear: Int,
    val text: String,
    val source: String,
    val reflection: String,
    val version: String
)

@Serializable data class AppConfigDto(
    val minVersion: String,
    val maintenanceMode: Boolean,
    val message: String?
)

@Serializable data class ContentVersionDto(
    val adhkarVersion: String,
    val khatiraVersion: String
)
```

### External Prayer Times API

The app also calls **Aladhan.com** API internally for prayer times:

- **URL**: `https://api.aladhan.com/v1/timings/{dateStr}`
- **Params**: latitude, longitude, method (0-14), school (0=hanafi/shafi, 1=hanafi)
- **Used by**: `PrayerTimeCalculator` (androidMain/iosMain actual implementations)

---

## 8. Features & Modules

> **Generated from `RafiqRoute.kt` + `RafiqNavGraph.kt`, not written by hand.**
>
> These tables drifted badly once: they named ten screens that no longer
> existed (`HomeScreen`, `QuranReadingScreen`, `QuranAudioPlayer`,
> `KhatiraScreen`, `CustomDhikrScreen`, `PrayerTrackingScreen`,
> `DeepLinkLandingScreen`, `RamadanHomeScreen`, `WidgetSettingsScreen`,
> `EmotionalDuaScreen`) while omitting several that did (`MushafScreen`,
> `HomeHubScreen`, `WaraqaScreen`, `ColorsScreen`, `ContactScreen`).
> Only three of those ten were deleted in the round that noticed — the
> other seven had been gone for a while with nothing to catch it.
>
> `tools/check_docs_screens.py` now runs in `verify.sh` and fails on any
> screen name here that has no file on disk. Keep the tables in step with
> the route table; do not hand-edit one row at a time.

Every screen below is reachable: `tools/check_orphan_routes.py` proves no
route is an orphan, so this list is also the complete set of destinations.

### Reading & Dhikr

| Feature | Route | Screen(s) | ViewModel |
|---------|-------|-----------|-----------|
| Home | `home` | `HomeHubScreen` | `HomeHubViewModel` |
| Today's page (الورقة) | `day_page` | `WaraqaScreen` | `DayCompanionViewModel` |
| Adhkar | `adhkar_categories`, `dhikr_reading/{category}`, `celebration` | `AdhkarCategoriesScreen`, `DhikrReadingScreen`, `CelebrationScreen` | `DhikrReadingViewModel` |
| Tasbeeh | `tasbeeh` | `TasbeehScreen` | `TasbeehViewModel` |
| Dua | `dua_categories`, `dua_list/{category}` | `DuaCategoriesScreen`, `DuaListScreen` | `DuaViewModel` |

### Quran

| Feature | Route | Screen(s) | ViewModel |
|---------|-------|-----------|-----------|
| Surah list | `quran_list` | `QuranListScreen` | `QuranListViewModel` |
| Mushaf (the only reader) | `mushaf?page={page}&aya={aya}` | `MushafScreen` | `MushafPageViewModel` |
| Search | `quran_search` | `QuranSearchScreen` | — |
| Bookmarks | `quran_bookmarks` | `QuranBookmarksScreen` | — |

`MushafScreen` is the single Quran reader. A second surah-based reading
screen once existed alongside it, which meant the same text looked
different depending on which door you came through; it was removed, and
list/search/bookmarks all open the Mushaf page now.

### Prayer & Qibla

| Feature | Route | Screen(s) | ViewModel |
|---------|-------|-----------|-----------|
| Prayer times | `prayer_times` | `PrayerTimesScreen` | `PrayerTimesViewModel` |
| Calculation method | `prayer_method` | `PrayerMethodScreen` | `SettingsViewModel` |
| Qibla compass | `qibla` | `QiblaScreen` | `QiblaViewModel`, `CompassManager` |

### Progress

| Feature | Route | Screen(s) | ViewModel |
|---------|-------|-----------|-----------|
| Profile (أوراقي) | `profile` | `ProfileScreen` | `ProfileViewModel` |
| Statistics | `statistics` | `StatisticsScreen` | `ProfileViewModel` |
| Achievements | `achievements` | `AchievementsScreen` | `AwraqViewModel` |
| Weekly report | `weekly_report` | `WeeklyReportScreen` | `ProfileViewModel` |
| Garden | `garden` | `GardenScreen` | `ProfileViewModel` |
| Breathing | `breathing` | `BreathingScreen` | — |
| Share card | `share_card` | `ShareCardScreen` | `ProfileViewModel` |

The last four reach the user through quick links at the bottom of
`ProfileScreen`.

### Settings & Info

| Feature | Route | Screen(s) | ViewModel |
|---------|-------|-----------|-----------|
| Settings root | `settings` | `SettingsScreen` | `SettingsViewModel` |
| Theme / Colors / Font | `theme_settings`, `colors`, `font_settings` | `ThemeSettingsScreen`, `ColorsScreen`, `FontSettingsScreen` | `SettingsViewModel` |
| Notifications | `notification_settings` | `NotificationSettingsScreen` | `SettingsViewModel` |
| Accessibility | `accessibility_settings` | `AccessibilitySettingsScreen` | `SettingsViewModel` |
| Language | `language` | `LanguageScreen` | `SettingsViewModel` |
| Export / Import | `export_data` | `ExportDataScreen` | `ExportDataViewModel` |
| About / Help / Contact | `about`, `help`, `contact` | `AboutScreen`, `HelpScreen`, `ContactScreen` | — |
| Privacy / Terms | `privacy_policy`, `terms` | `PrivacyPolicyScreen`, `TermsScreen` | — |
| What's New | `whats_new` | `WhatsNewScreen` | — |
| Onboarding | `onboarding` | `OnboardingScreen` | `SettingsViewModel` |

### Widget

- **`PrayerWidget`** — Glance home-screen widget showing the next prayer.
  Reads `PrayerTimeCalculator` and stored `UserPrefs` for location/method.
- It is the **only** widget. A settings screen once offered switches for
  two more that were never written; it was deleted. The user adds the
  widget from the launcher, so the app only points the way — a hint at
  the bottom of `PrayerTimesScreen`.

### Background Services

| File | Purpose |
|------|---------|
| `PrayerAlarmManager` | Schedules/cancels alarms; falls back to inexact when the exact-alarm permission is absent |
| `PrayerAlarmReceiver` | Fires the notification and chains the next day's reschedule |
| `PrayerRescheduler` | Recomputes and reschedules when settings change |
| `BootReceiver` | Reschedules after reboot |
| `ExactAlarmPermissionReceiver` | Reschedules when the user grants exact-alarm permission |
| `CompassManager` | Sensor plumbing for the qibla compass |

### Navigation (`RafiqNavGraph`)

- **37 routes** in the `RafiqRoute` sealed class — all reachable.
- `NavHost` with `RafiqBottomBar` on most screens; the Mushaf hides it.
- Transitions via `CinematicTransitions`.
- Deep links via intent filters (`https://rafiqaldhikr.app`).

---

## 9. Authentication & Authorization

**No user authentication system is implemented.**

- User data is stored locally only (SQLDelight database)
- No login, signup, or session management
- No role-based access control
- Privacy is managed via local-only data storage

---

## 10. State Management

### Architecture

- **MVVM** with ViewModels holding UI state as `StateFlow`/`MutableStateFlow`
- **Koin** for dependency injection into ViewModels
- **Compose State** consumed via `collectAsStateWithLifecycle`
- **Single Activity** architecture with Compose Navigation

### Key State Flows

| ViewModel | State |
|-----------|-------|
| `SettingsViewModel` | `onboardingCompleted`, `dynamicColor`, all `UserPrefsInfo` fields |
| `HomeViewModel` | Today's adhkar completion, next prayer, streak, daily progress |
| `PrayerTimesViewModel` | Prayer times, next prayer, prayer method |
| `QiblaViewModel` | Compass heading, qibla direction |
| `TasbeehViewModel` | Current count, target, sessions |
| `MushafPageViewModel` | Page ayat, tafsir, bookmarks, last-read position |
| `DhikrReadingViewModel` | Current adhkar category, completed count |

### Persistent State

- `UserPrefs` — stored in SQLDelight `UserPrefs` table (backed by encrypted shared preferences via `SecurityModule`)
- `DailyProgress`, `PrayerLog`, `StreakData`, `TasbeehSession` — stored in SQLDelight
- `QuranLastRead`, `QuranBookmark` — stored in SQLDelight

---

## 11. Third-Party Integrations

| Integration | Purpose | Status |
|-------------|---------|--------|
| **Aladhan.com API** | Prayer time calculations | ✅ Active |
| **RafiqAldhikr API** | Khatira content, app config, content versioning | ✅ Active |
| **RevenueCat** | In-app subscription/purchase management | ❌ TODO (commented out) |
| **Firebase Crashlytics** | Crash reporting | ❌ TODO (commented out) |
| **Google Play App Update** | In-app update prompts | ✅ Available (commented out in deps) |
| **Google Play Review** | In-app review requests | ✅ Available (commented out in deps) |
| **Android Glance** | Home screen widget | ✅ Active |
| **Media3 (ExoPlayer)** | Quran audio playback | ✅ Active |
| **EncryptedSharedPreferences** | Secure storage | ✅ Active |

---

## 12. CI/CD & Deployment

### Build Variants

| Variant | Minify | Shrink | Signing |
|---------|--------|--------|---------|
| Debug | ❌ | ❌ | Default debug keystore |
| Release | ✅ | ✅ | Environment-variable-based keystore or debug fallback |

### Release Signing

Release signing uses environment variables:
- `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
- Falls back to debug keystore if env vars are not set

### ProGuard/R8

ProGuard rules defined in `androidApp/proguard-rules.pro` covering:
- SQLDelight
- Kotlinx Serialization
- Ktor
- Koin
- RevenueCat
- Firebase
- Coroutines

### Build Outputs

- Debug APK: `androidApp/build/intermediates/apk/debug/androidApp-debug.apk`
- Release AAR: `shared/build/outputs/aar/shared-debug.aar`

### Build Tools

- Gradle 9.3.1 (via wrapper)
- JDK 21 (`org.gradle.java.home`)
- Android SDK Compile SDK 36

---

## 13. Testing

### Test Types Present

| Type | Framework | Location |
|------|-----------|----------|
| Unit Tests | Kotlin Test, JUnit, MockK, Turbine | `shared/src/commonTest` (implicit) |
| Android Unit Tests | JUnit4, Compose UI Test | `androidApp/src/test`, `androidApp/src/androidTest` |

### Test Dependencies

| Library | Version |
|---------|---------|
| JUnit | 4.13.2 |
| Kotlin Test | 2.2.10 |
| Kotlinx Coroutines Test | 1.9.0 |
| Turbine | 1.2.0 |
| MockK | 1.13.13 |
| Compose UI Test JUnit4 | BOM-managed |
| Compose UI Test Manifest | BOM-managed |

### Running Tests

No explicit test running instructions found in configuration files. Standard Gradle commands would apply:

```bash
./gradlew test          # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
```

---

## 14. How to Run Locally

### Prerequisites

1. **JDK 21** — Set `JAVA_HOME` or use the bundled JDK (`C:/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot`)
2. **Android SDK** — Set `ANDROID_HOME` or `ANDROID_SDK_ROOT` in `local.properties`
3. **Gradle** — Wrapper is included at `gradle/wrapper/gradle-wrapper.jar` (Gradle 9.3.1)
4. **Kotlin 2.2.10** with Multiplatform support

### Build Setup

```bash
# Clone the repository
cd rafiq-aldhikr

# Ensure local.properties exists with SDK path
echo "sdk.dir=C:/path/to/android/sdk" > local.properties

# Build the debug APK
./gradlew assembleDebug
```

### Run on Device/Emulator

```bash
./gradlew installDebug
```

### Configure Signing for Release Build

Set environment variables before building release:

```bash
export KEYSTORE_FILE="/path/to/keystore.jks"
export KEYSTORE_PASSWORD="..."
export KEY_ALIAS="..."
export KEY_PASSWORD="..."
./gradlew assembleRelease
```

### Optional: Enable Firebase

1. Add `google-services.json` to `androidApp/src/main/`
2. Uncomment Firebase plugin and dependencies in `build.gradle.kts` files

### Optional: Enable RevenueCat

1. Add `REVENUECAT_API_KEY` to `local.properties`
2. Uncomment RevenueCat dependencies in `build.gradle.kts`

---

## 15. Known Issues / TODOs

### Hardcoded TODOs Found in Code

1. **`RafiqApplication.kt`**: Firebase Crashlytics commented out — "TODO: Firebase Crashlytics — add google-services.json first"
2. **`RafiqApplication.kt`**: RevenueCat commented out — "TODO: RevenueCat — add API key in local.properties"
3. **`androidApp/build.gradle.kts`**: Google Services and Firebase Crashlytics plugins commented out — "TODO: Uncomment when google-services.json is added"
4. **`androidApp/build.gradle.kts`**: RevenueCat dependencies commented out — "TODO: Uncomment when RevenueCat API key is configured"
5. **`MushafScreen.kt`**: `TafsirSheet` and `AyahSheet` are `ModalBottomSheet`s opened directly from the Mushaf page rather than as navigation routes

### Configuration Gaps

1. **No `google-services.json`** — Firebase features cannot be enabled
2. **No RevenueCat API key** — Monetization features cannot be enabled
3. **API base URL** (`https://api.rafiqaldhikr.app/v1`) is hardcoded in `RafiqApi.kt` — not configurable via environment

### Known Runtime Considerations

1. **Exact Alarm Permission** (Android 12+): `PrayerAlarmManager` checks `canScheduleExactAlarms()` before scheduling; fallback behavior if permission denied is not implemented
2. **Location Permission**: Required for Qibla compass and prayer times; graceful degradation with default coordinates (35.5558, 45.4436 — appears to be a placeholder Iraq location)
3. **RTL Layout**: `RafiqTheme` forces `LayoutDirection.RTL` via `LocalLayoutDirection`
4. **EncryptedSharedPreferences**: Uses AES256_GCM encryption for secure prefs; no fallback for devices without hardware encryption

### Build Artifacts

- `build_error.log` and `build_output.log` present in root — may contain historical build information
- `test_methods.py` and `replace.py` Python scripts in root — purpose unclear; may be utility scripts

### Platform Support

- **iOS**: KMP configuration present (`iosX64`, `iosArm64`, `iosSimulatorArm64`) but `iosMain` implementations are stubs
- **Desktop**: Not configured
- **Web**: Not configured
