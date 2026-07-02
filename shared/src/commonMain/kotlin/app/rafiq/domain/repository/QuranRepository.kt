package app.rafiq.domain.repository

import app.rafiq.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<SurahInfo>>
    fun getAyahsBySurah(surahNumber: Int): Flow<List<AyahInfo>>
    fun getAyahsByPage(page: Int): Flow<List<AyahInfo>>
    fun searchAyahs(query: String): Flow<List<AyahInfo>>
    fun getLastRead(): Flow<LastReadPosition?>
    suspend fun saveLastRead(surah: Int, ayah: Int, page: Int, scrollY: Float)
    fun getBookmarks(): Flow<List<QuranBookmark>>
    suspend fun addBookmark(surah: Int, ayah: Int, page: Int)
    suspend fun removeBookmark(id: Long)
    suspend fun removeBookmarkByPosition(surah: Int, ayah: Int)
    suspend fun isBookmarked(surah: Int, ayah: Int): Boolean
    suspend fun getTafsir(surah: Int, ayah: Int): String?
}
