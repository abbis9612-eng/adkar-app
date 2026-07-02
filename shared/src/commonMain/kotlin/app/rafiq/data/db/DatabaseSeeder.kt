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

    companion object {
        // أعداد المحتوى المتوقعة — أي نقص عنها يعني بذراً ناقصاً فيُعاد
        private const val EXPECTED_SURAHS = 114L
        private const val EXPECTED_AYAHS  = 6236L
        private const val EXPECTED_TAFSIR = 6236L
    }

    /**
     * تعبئة ذاتية الإصلاح: إن قُتل التطبيق أثناء التعبئة الأولى تبقى الجداول
     * ناقصة — لذلك نقارن بالعدد المتوقع لا بالصفر، ونعيد تعبئة الناقص فقط.
     */
    suspend fun seedIfNeeded() {
        db.transaction {
            db.userPrefsQueries.initIfNeeded()
        }

        if (db.surahQueries.count().executeAsOne() < EXPECTED_SURAHS) {
            db.transaction { db.surahQueries.deleteAll() }
            seedSurahs()
        }
        if (db.ayahQueries.count().executeAsOne() < EXPECTED_AYAHS) {
            db.transaction { db.ayahQueries.deleteAll() }
            seedAyahs()
        }

        seedAdhkarIfIncomplete()

        seedDuasIfIncomplete()

        val khatiraCount = db.khatiraQueries.count().executeAsOne()
        if (khatiraCount == 0L) { seedKhatira() }

        if (db.tafsirQueries.count().executeAsOne() < EXPECTED_TAFSIR) {
            // insertFull يستخدم REPLACE — لا حاجة لحذف مسبق
            seedTafsir()
        }
    }

    private suspend fun seedAdhkarIfIncomplete() {
        val files = listOf(
            "morning" to "adhkar_morning.json",
            "evening" to "adhkar_evening.json",
            "sleep"   to "adhkar_sleep.json",
            "prayer"  to "adhkar_prayer.json",
            "misc"    to "adhkar_misc.json",
        )
        val lists = files.map { (cat, file) ->
            cat to json.decodeFromString<List<DhikrJson>>(jsonReader.readAsset(file))
        }
        val expected = lists.sumOf { it.second.size }.toLong()
        if (db.adhkarQueries.count().executeAsOne() >= expected) return

        db.transaction { db.adhkarQueries.deleteAll() }
        lists.forEach { (category, list) -> insertAdhkar(category, list) }
    }

    private suspend fun seedDuasIfIncomplete() {
        val rawJson = jsonReader.readAsset("duas.json")
        val list: List<DuaJson> = json.decodeFromString(rawJson)
        if (db.duaQueries.count().executeAsOne() >= list.size.toLong()) return

        db.transaction { db.duaQueries.deleteAll() }
        insertDuas(list)
    }

    private suspend fun seedTafsir() {
        val rawJson = jsonReader.readAsset("tafsir_muyassar.json")
        val entries: List<TafsirJson> = json.decodeFromString(rawJson)
        entries.chunked(500).forEach { chunk ->
            db.transaction {
                chunk.forEach { t ->
                    db.tafsirQueries.insertFull(
                        surah = t.surah.toLong(),
                        ayah  = t.ayah.toLong(),
                        text  = t.text
                    )
                }
            }
        }
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

    private fun insertAdhkar(category: String, list: List<DhikrJson>) {
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

    private fun insertDuas(list: List<DuaJson>) {
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
