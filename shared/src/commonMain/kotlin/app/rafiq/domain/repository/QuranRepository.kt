package app.rafiq.domain.repository

import app.rafiq.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<SurahInfo>>
    fun getAyahsBySurah(surahNumber: Int): Flow<List<AyahInfo>>
    fun getAyahsByPage(page: Int): Flow<List<AyahInfo>>

    /**
     * آيةٌ بعينها بسورتها ورقمها.
     *
     * كانت ورقةُ الآية تلتقطها من **صفحة التخطيط** الحاضرة، والقاعدةُ
     * تخزّن للآية صفحتَها هي. وهما يفترقان في ٥٦ آيةً على ٢٥ صفحة —
     * كلُّ آيةٍ تبدأ في صفحةٍ وتُتِمّ في التي بعدها. فكانت الورقةُ تفتح
     * فارغةً على تلك الصفحات، والنسخُ والمشاركةُ يُخرجان العنوانَ بلا نصّ.
     */
    suspend fun getAyah(surah: Int, ayah: Int): AyahInfo?

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
