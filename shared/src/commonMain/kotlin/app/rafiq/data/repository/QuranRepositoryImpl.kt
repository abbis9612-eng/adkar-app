package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.*
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class QuranRepositoryImpl(private val db: RafiqDatabase) : QuranRepository {

    override fun getAllSurahs(): Flow<List<SurahInfo>> =
        db.surahQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { s -> s.toDomain() } }

    override fun getAyahsBySurah(surahNumber: Int): Flow<List<AyahInfo>> =
        db.ayahQueries.getBySurah(surahNumber.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { a -> a.toDomain() } }

    override fun getAyahsByPage(page: Int): Flow<List<AyahInfo>> =
        db.ayahQueries.getByPage(page.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { a -> a.toDomain() } }

    override suspend fun getAyah(surah: Int, ayah: Int): AyahInfo? =
        withContext(Dispatchers.IO) {
            db.ayahQueries.getAyah(surah.toLong(), ayah.toLong())
                .executeAsOneOrNull()?.toDomain()
        }

    override fun searchAyahs(query: String): Flow<List<AyahInfo>> =
        db.ayahQueries.searchSimple(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { a -> a.toDomain() } }

    override fun getLastRead(): Flow<LastReadPosition?> =
        db.quranLastReadQueries.get()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun saveLastRead(surah: Int, ayah: Int, page: Int, scrollY: Float) =
        withContext(Dispatchers.IO) {
            db.quranLastReadQueries.upsert(
                surah     = surah.toLong(),
                ayah      = ayah.toLong(),
                page      = page.toLong(),
                scroll_y  = scrollY.toDouble(),
                updated_at = Clock.System.now().toEpochMilliseconds()
            )
        }

    override fun getBookmarks(): Flow<List<QuranBookmark>> =
        db.quranBookmarkQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { b -> b.toDomain() } }

    override suspend fun addBookmark(surah: Int, ayah: Int, page: Int) =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.insert(
                surah      = surah.toLong(),
                ayah       = ayah.toLong(),
                page       = page.toLong(),
                created_at = Clock.System.now().toEpochMilliseconds(),
                note       = null,
                kind       = "mark",
            )
        }

    override suspend fun removeBookmark(id: Long) =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.delete(id)
        }

    override suspend fun removeBookmarkByPosition(surah: Int, ayah: Int) =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.deleteByPosition(surah.toLong(), ayah.toLong())
        }

    override suspend fun isBookmarked(surah: Int, ayah: Int): Boolean =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.exists(surah.toLong(), ayah.toLong())
                .executeAsOne() > 0L
        }

    override suspend fun ayahNote(surah: Int, ayah: Int): String? =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.getNote(surah.toLong(), ayah.toLong())
                .executeAsOneOrNull()?.note?.takeIf { it.isNotBlank() }
        }

    /*  الملاحظةُ تُكتب على آيةٍ قد لا تكون مُعلَّمةً بعد.
     *
     *  فيُنشَأ لها صفٌّ أوّلاً (`INSERT OR IGNORE` فلا يُمسّ صفٌّ قائم)
     *  ثمّ تُحدَّث. ولولا ذلك ضاعت كتابةُ من كتب ملاحظةً قبل أن يُعلّم.
     *
     *  والفارغةُ تُكتب null لا سلسلةً فارغة: `note` حقلٌ يحتمل الغياب،
     *  والفراغُ غيابٌ لا قيمة. */
    override suspend fun setAyahNote(surah: Int, ayah: Int, page: Int, note: String?) =
        withContext(Dispatchers.IO) {
            db.quranBookmarkQueries.upsertNote(
                surah      = surah.toLong(),
                ayah       = ayah.toLong(),
                page       = page.toLong(),
                created_at = Clock.System.now().toEpochMilliseconds(),
            )
            db.quranBookmarkQueries.updateNote(
                note  = note?.trim()?.takeIf { it.isNotEmpty() },
                surah = surah.toLong(),
                ayah  = ayah.toLong(),
            )
        }

    /*  موضعُ الوقوف **واحدٌ لا يتعدّد**.
     *
     *  فوضعُ موضعٍ جديد يمحو القديمَ أوّلاً. ولو تُرك يتراكم لصار قائمةَ
     *  مواضعَ لا موضعاً — وذاك عملُ «العلامة» لا عملُه.
     *
     *  وإن كانت الآيةُ معلَّمةً أصلاً فلا تُحوَّل إلى موضعِ وقوف: علامتُها
     *  مقصودةٌ لصاحبها، ونقلُها إلى ما يُزاح غداً إتلافٌ لها. فيُترك
     *  الصفُّ كما هو ولا يُوضع موضعٌ على تلك الآية.
     */
    override fun stopMark(): Flow<QuranBookmark?> =
        db.quranBookmarkQueries.getStop()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun setStop(surah: Int, ayah: Int, page: Int) =
        withContext(Dispatchers.IO) {
            db.transaction {
                /*  الصفُّ قائمٌ على هذه الآية؟ لا يُلمَس **ولا يُمحى
                 *  الموضعُ القديم**.
                 *
                 *  وكان `clearStops()` يسبق الإدخال: فإن كانت الآيةُ
                 *  معلَّمةً من قبلُ منع `UNIQUE(surah, ayah)` الإدخالَ
                 *  بـ`OR IGNORE`، فيضيع الموضعُ القديمُ ولا يُكتب الجديد
                 *  — ويُقال لصاحبه «وقفتَ هنا» وما وقف شيءٌ.
                 *
                 *  والعلامةُ لا تُحوَّل موضعاً: قد يكون عليها حاشيةٌ
                 *  كتبها، وتحويلُها إتلافٌ لها. */
                if (db.quranBookmarkQueries.exists(surah.toLong(), ayah.toLong())
                        .executeAsOne() > 0L
                ) {
                    return@transaction
                }
                db.quranBookmarkQueries.clearStops()
                db.quranBookmarkQueries.insert(
                    surah      = surah.toLong(),
                    ayah       = ayah.toLong(),
                    page       = page.toLong(),
                    created_at = Clock.System.now().toEpochMilliseconds(),
                    note       = null,
                    kind       = "stop",
                )
                //  `INSERT OR IGNORE` لا يفعل شيئاً إن كان الصفُّ قائماً
                //  علامةً — فتُترك علامتُه ولا تُحوَّل.
            }
        }

    override suspend fun clearStop() =
        withContext(Dispatchers.IO) { db.quranBookmarkQueries.clearStops() }

    override suspend fun getTafsir(surah: Int, ayah: Int): String? =
        withContext(Dispatchers.IO) {
            db.tafsirQueries.getByAyah(surah.toLong(), ayah.toLong())
                .executeAsOneOrNull()
        }
}
