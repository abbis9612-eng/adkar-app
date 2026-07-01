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
                note       = null
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
}
