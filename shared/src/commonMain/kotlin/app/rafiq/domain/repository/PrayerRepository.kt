package app.rafiq.domain.repository

import app.rafiq.domain.model.PrayerEntry
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    fun getByDate(date: String): Flow<List<PrayerEntry>>
    /** صلواتُ مدىً من الأيّام، [start] و[end] شاملين — أسبوعُ المحاسبة. */
    fun getRange(start: String, end: String): Flow<List<PrayerEntry>>
    suspend fun logPrayer(date: String, prayerName: String, prayed: Boolean)
    suspend fun updateSunnah(date: String, prayerName: String, done: Boolean)
    /**
     * صلّاها في المسجد — أو لا. ولا تكون في المسجد إلّا وقد صُلّيت: فتعليمُها
     * يُثبت الصلاةَ معها، ومحوُها يُبقي الصلاةَ كما هي.
     */
    suspend fun setInMasjid(date: String, prayerName: String, inMasjid: Boolean)
    suspend fun getPrayedCount(start: String, end: String): Long
}
