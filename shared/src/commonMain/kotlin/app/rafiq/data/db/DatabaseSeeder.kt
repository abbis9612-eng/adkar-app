package app.rafiq.data.db

import app.rafiq.data.model.*
import app.rafiq.db.RafiqDatabase
import app.rafiq.platform.JsonResourceReader
import kotlinx.serialization.json.Json

class DatabaseSeeder(
    private val db: RafiqDatabase,
    private val jsonReader: JsonResourceReader
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfNeeded() {
        db.transaction {
            db.userPrefsQueries.initIfNeeded()
        }

        val surahCount = db.surahQueries.count().executeAsOne()
        if (surahCount == 0L) {
            seedSurahs()
            seedAyahs()
        }

        val adhkarCount = db.adhkarQueries.count().executeAsOne()
        if (adhkarCount == 0L) {
            seedAdhkar("morning", "adhkar_morning.json")
            seedAdhkar("evening", "adhkar_evening.json")
            seedAdhkar("sleep",   "adhkar_sleep.json")
            seedAdhkar("prayer",  "adhkar_prayer.json")
            seedAdhkar("misc",    "adhkar_misc.json")
        }

        val duaCount = db.duaQueries.count().executeAsOne()
        if (duaCount == 0L) { seedDuas() }

        val khatiraCount = db.khatiraQueries.count().executeAsOne()
        if (khatiraCount == 0L) { seedKhatira() }
    }

    private suspend fun seedSurahs() {
        val rawJson = jsonReader.readAsset("surah_metadata.json")
        val surahs: List<SurahJson> = json.decodeFromString(rawJson)
        db.transaction {
            surahs.forEach { s ->
                db.surahQueries.insertFull(
                    number        = s.number.toLong(),
                    name_ar       = s.nameAr,
                    name_en       = s.nameEn,
                    name_translit = s.nameTranslit,
                    revelation    = s.revelation,
                    ayah_count    = s.ayahCount.toLong(),
                    juz_start     = s.juzStart.toLong(),
                    page_start    = s.pageStart.toLong()
                )
            }
        }
    }

    private suspend fun seedAyahs() {
        val rawJson = jsonReader.readAsset("quran_uthmani.json")
        val ayahs: List<AyahJson> = json.decodeFromString(rawJson)
        ayahs.chunked(500).forEach { chunk ->
            db.transaction {
                chunk.forEach { a ->
                    db.ayahQueries.insertFull(
                        id           = (a.surah * 1000 + a.ayah).toLong(),
                        surah        = a.surah.toLong(),
                        ayah_number  = a.ayah.toLong(),
                        text_uthmani = a.textUthmani,
                        text_simple  = a.textSimple,
                        juz          = a.juz.toLong(),
                        hizb         = a.hizb.toLong(),
                        page         = a.page.toLong()
                    )
                }
            }
        }
    }

    private suspend fun seedAdhkar(category: String, fileName: String) {
        val rawJson = jsonReader.readAsset(fileName)
        val list: List<DhikrJson> = json.decodeFromString(rawJson)
        db.transaction {
            list.forEach { d ->
                db.adhkarQueries.insertFull(
                    category     = category,
                    text_ar      = d.textAr,
                    source       = d.source,
                    source_grade = d.sourceGrade,
                    virtue       = d.virtue,
                    count        = d.count.toLong(),
                    audio_file   = d.audioFile,
                    sort_order   = d.sortOrder.toLong()
                )
            }
        }
    }

    private suspend fun seedDuas() {
        val rawJson = jsonReader.readAsset("duas.json")
        val list: List<DuaJson> = json.decodeFromString(rawJson)
        db.transaction {
            list.forEach { d ->
                db.duaQueries.insertFull(
                    category     = d.category,
                    occasion     = d.occasion,
                    text_ar      = d.textAr,
                    source       = d.source,
                    source_grade = d.sourceGrade,
                    is_favorite  = 0L,
                    sort_order   = d.sortOrder.toLong()
                )
            }
        }
    }

    private suspend fun seedKhatira() {
        val rawJson = jsonReader.readAsset("khatira_366.json")
        val list: List<KhatiraJson> = json.decodeFromString(rawJson)
        db.transaction {
            list.forEach { k ->
                db.khatiraQueries.insertFull(
                    day_of_year     = k.dayOfYear.toLong(),
                    verse_or_hadith = k.verseOrHadith,
                    source          = k.source,
                    reflection      = k.reflection,
                    season          = k.season,
                    reviewed        = if (k.reviewed) 1L else 0L,
                    reviewer        = k.reviewer
                )
            }
        }
    }
}
