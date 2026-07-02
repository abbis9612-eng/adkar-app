package app.rafiq.domain.model

import app.rafiq.db.Adhkar         as AdhkarEntity
import app.rafiq.db.Khatira        as KhatiraEntity
import app.rafiq.db.QuranBookmark  as QuranBookmarkEntity
import app.rafiq.db.Surah          as SurahEntity
import app.rafiq.db.Ayah           as AyahEntity
import app.rafiq.db.Dua            as DuaEntity
import app.rafiq.db.PrayerLog      as PrayerLogEntity
import app.rafiq.db.DailyProgress  as DailyProgressEntity
import app.rafiq.db.StreakData      as StreakDataEntity
import app.rafiq.db.UserPrefs      as UserPrefsEntity
import app.rafiq.db.QuranLastRead  as QuranLastReadEntity

// ═══ Domain Models ═══

data class Dhikr(
    val id:          Long,
    val category:    String,
    val textAr:      String,
    val source:      String,
    val sourceGrade: String,
    val virtue:      String,
    val count:       Int,
    val audioFile:   String?,
    val sortOrder:   Int
)

data class Khatira(
    val id:             Long,
    val dayOfYear:      Int,
    val verseOrHadith:  String,
    val source:         String,
    val reflection:     String,
    val season:         String,
    val reviewed:       Boolean,
    val remoteVersion:  String?
)

data class SurahInfo(
    val number:      Int,
    val nameAr:      String,
    val nameEn:      String,
    val nameTranslit: String,
    val revelation:  String,
    val ayahCount:   Int,
    val juzStart:    Int,
    val pageStart:   Int
)

data class AyahInfo(
    val id:          Long,
    val surah:       Int,
    val ayahNumber:  Int,
    val textUthmani: String,
    val textSimple:  String,
    val juz:         Int,
    val hizb:        Int,
    val page:        Int
)

data class DuaItem(
    val id:          Long,
    val category:    String,
    val occasion:    String,
    val textAr:      String,
    val source:      String,
    val sourceGrade: String,
    val isFavorite:  Boolean,
    val sortOrder:   Int
)

data class PrayerEntry(
    val id:          Long,
    val date:        String,
    val prayerName:  String,
    val prayed:      Boolean,
    val inMasjid:    Boolean,
    val sunnahDone:  Boolean
)

data class DailyProgressInfo(
    val date:          String,
    val morningDone:   Boolean,
    val eveningDone:   Boolean,
    val quranPages:    Long,
    val tasbeehCount:  Long,
    val prayersLogged: Long,
    val totalMinutes:  Long
)

data class StreakInfo(
    val current:       Long,
    val longest:       Long,
    val lastActiveDate: String
)

data class UserPrefsInfo(
    val theme:                String,
    val dynamicColor:         Boolean,
    val fontScale:            Double,
    val notificationsEnabled: Boolean,
    val glassLevel:           String,
    val gamificationVisible:  Boolean,
    val prayerMethod:         String,
    val locale:               String,
    val hijriOffset:          Long,
    val reducedMotion:        Boolean,
    val highContrast:         Boolean,
    val soundProfile:         String,
    val hapticsEnabled:       Boolean,
    val lastKnownCity:        String,
    val lastKnownLat:         Double,
    val lastKnownLng:         Double,
    val onboardingCompleted:  Boolean,
    val fajrOffset:           Int,
    val dhuhrOffset:          Int,
    val asrOffset:            Int,
    val maghribOffset:        Int,
    val ishaOffset:           Int,
    val elevation:            Double,
    val madhab:               String,
    val numerals:             String
)

data class LastReadPosition(
    val surah:   Int,
    val ayah:    Int,
    val page:    Int,
    val scrollY: Float
)

data class QuranBookmark(
    val id:        Long,
    val surah:     Int,
    val ayah:      Int,
    val page:      Int,
    val createdAt: Long,
    val note:      String?
)

// ═══ Mapping Extensions: DB Entity → Domain Model ═══

fun AdhkarEntity.toDomain() = Dhikr(
    id          = id,
    category    = category,
    textAr      = text_ar,
    source      = source,
    sourceGrade = source_grade,
    virtue      = virtue,
    count       = count.toInt(),
    audioFile   = audio_file,
    sortOrder   = sort_order.toInt()
)

fun KhatiraEntity.toDomain() = Khatira(
    id            = id,
    dayOfYear     = day_of_year.toInt(),
    verseOrHadith = verse_or_hadith,
    source        = source,
    reflection    = reflection,
    season        = season,
    reviewed      = reviewed == 1L,
    remoteVersion = remote_version
)

fun SurahEntity.toDomain() = SurahInfo(
    number       = number.toInt(),
    nameAr       = name_ar,
    nameEn       = name_en,
    nameTranslit = name_translit,
    revelation   = revelation,
    ayahCount    = ayah_count.toInt(),
    juzStart     = juz_start.toInt(),
    pageStart    = page_start.toInt()
)

fun AyahEntity.toDomain() = AyahInfo(
    id          = id,
    surah       = surah.toInt(),
    ayahNumber  = ayah_number.toInt(),
    textUthmani = text_uthmani,
    textSimple  = text_simple,
    juz         = juz.toInt(),
    hizb        = hizb.toInt(),
    page        = page.toInt()
)

fun DuaEntity.toDomain() = DuaItem(
    id          = id,
    category    = category,
    occasion    = occasion,
    textAr      = text_ar,
    source      = source,
    sourceGrade = source_grade,
    isFavorite  = is_favorite == 1L,
    sortOrder   = sort_order.toInt()
)

fun PrayerLogEntity.toDomain() = PrayerEntry(
    id          = id,
    date        = date,
    prayerName  = prayer_name,
    prayed      = prayed == 1L,
    inMasjid    = in_masjid == 1L,
    sunnahDone  = sunnah_done == 1L
)

fun DailyProgressEntity.toDomain() = DailyProgressInfo(
    date          = date,
    morningDone   = morning_done == 1L,
    eveningDone   = evening_done == 1L,
    quranPages    = quran_pages,
    tasbeehCount  = tasbeeh_count,
    prayersLogged = prayers_logged,
    totalMinutes  = total_minutes
)

fun StreakDataEntity.toDomain() = StreakInfo(
    current        = current_streak,
    longest        = longest_streak,
    lastActiveDate = last_active_date
)

fun UserPrefsEntity.toDomain() = UserPrefsInfo(
    theme                = theme,
    dynamicColor         = dynamic_color == 1L,
    fontScale            = font_scale,
    notificationsEnabled = notifications_enabled == 1L,
    glassLevel           = glass_level,
    gamificationVisible  = gamification_visible == 1L,
    prayerMethod         = prayer_method,
    locale               = locale,
    hijriOffset          = hijri_offset,
    reducedMotion        = reduced_motion == 1L,
    highContrast         = high_contrast == 1L,
    soundProfile         = sound_profile,
    hapticsEnabled       = haptics_enabled == 1L,
    lastKnownCity        = last_known_city,
    lastKnownLat         = last_known_lat,
    lastKnownLng         = last_known_lng,
    onboardingCompleted  = onboarding_completed == 1L,
    fajrOffset           = fajr_offset.toInt(),
    dhuhrOffset          = dhuhr_offset.toInt(),
    asrOffset            = asr_offset.toInt(),
    maghribOffset        = maghrib_offset.toInt(),
    ishaOffset           = isha_offset.toInt(),
    elevation            = elevation,
    madhab               = madhab,
    numerals             = numerals
)

fun QuranLastReadEntity.toDomain() = LastReadPosition(
    surah   = surah.toInt(),
    ayah    = ayah.toInt(),
    page    = page.toInt(),
    scrollY = scroll_y.toFloat()
)

fun QuranBookmarkEntity.toDomain() = QuranBookmark(
    id        = id,
    surah     = surah.toInt(),
    ayah      = ayah.toInt(),
    page      = page.toInt(),
    createdAt = created_at,
    note      = note
)
