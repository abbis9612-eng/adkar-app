package app.rafiq.data.repository

import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.repository.ImportResult
import app.rafiq.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ExportedPrefs(
    val theme: String,
    val prayerMethod: String,
    val madhab: String,
    val locale: String,
    val hijriOffset: Long,
    val fajrOffset: Long,
    val dhuhrOffset: Long,
    val asrOffset: Long,
    val maghribOffset: Long,
    val ishaOffset: Long,
    val elevation: Double,
)

@Serializable
private data class ExportedStreak(
    val currentStreak: Long,
    val longestStreak: Long,
    val lastActiveDate: String,
)

@Serializable
private data class ExportedDailyProgress(
    val date: String,
    val morningDone: Boolean,
    val eveningDone: Boolean,
    val quranPages: Long,
    val tasbeehCount: Long,
    val prayersLogged: Long,
    val totalMinutes: Long,
)

@Serializable
private data class ExportedTasbeehSession(
    val dhikrText: String,
    val count: Long,
    val target: Long,
    val completed: Boolean,
    val durationSeconds: Long,
    val date: String,
    val createdAt: Long,
)

@Serializable
private data class ExportedPrayerLog(
    val date: String,
    val prayerName: String,
    val prayed: Boolean,
    val inMasjid: Boolean,
    val sunnahDone: Boolean,
)

@Serializable
private data class ExportedBookmark(
    val surah: Long,
    val ayah: Long,
    val page: Long,
    val note: String?,
    val createdAt: Long,
)

@Serializable
private data class ExportedCustomDhikr(
    val dhikrText: String,
    val target: Long,
    val createdAt: Long,
)

@Serializable
private data class ExportedLastRead(
    val surah: Long,
    val ayah: Long,
    val page: Long,
)

@Serializable
private data class RafiqExport(
    val formatVersion: Int,
    val exportedAt: String,
    val prefs: ExportedPrefs?,
    val streak: ExportedStreak?,
    val dailyProgress: List<ExportedDailyProgress>,
    val tasbeehSessions: List<ExportedTasbeehSession>,
    val prayerLogs: List<ExportedPrayerLog>,
    val quranBookmarks: List<ExportedBookmark>,
    val customAdhkar: List<ExportedCustomDhikr>,
    val quranLastRead: ExportedLastRead?,
    val favoriteDuas: List<String>,
    val unlockedAchievements: List<String> = emptyList(),
)

class UserDataRepositoryImpl(private val db: RafiqDatabase) : UserDataRepository {

    private val json = Json { prettyPrint = true }

    /*  قارئٌ متسامحٌ للاستيراد وحدَه.
     *
     *  `ignoreUnknownKeys` كي يُقرأ ملفٌّ صدّرته نسخةٌ أحدثُ فيها حقولٌ
     *  لا نعرفها: نأخذ ما نفهم ونترك الباقي، بدل أن نرفض الملفَّ كلَّه.
     *  والعكسُ محميٌّ بـ`formatVersion` أدناه. */
    private val reader = Json { ignoreUnknownKeys = true }

    /** أحدثُ صيغةِ تصديرٍ يفهمها هذا الإصدار. */
    private val supportedFormat = 1

    override suspend fun exportAsJson(): String = withContext(Dispatchers.IO) {
        val prefs = db.userPrefsQueries.get().executeAsOneOrNull()?.let {
            ExportedPrefs(
                theme         = it.theme,
                prayerMethod  = it.prayer_method,
                madhab        = it.madhab,
                locale        = it.locale,
                hijriOffset   = it.hijri_offset,
                fajrOffset    = it.fajr_offset,
                dhuhrOffset   = it.dhuhr_offset,
                asrOffset     = it.asr_offset,
                maghribOffset = it.maghrib_offset,
                ishaOffset    = it.isha_offset,
                elevation     = it.elevation,
            )
        }

        val export = RafiqExport(
            formatVersion = 1,
            exportedAt    = Clock.System.now().toString(),
            prefs         = prefs,
            streak        = db.streakDataQueries.get().executeAsOneOrNull()?.let {
                ExportedStreak(it.current_streak, it.longest_streak, it.last_active_date)
            },
            dailyProgress = db.dailyProgressQueries.getAll().executeAsList().map {
                ExportedDailyProgress(
                    date          = it.date,
                    morningDone   = it.morning_done == 1L,
                    eveningDone   = it.evening_done == 1L,
                    quranPages    = it.quran_pages,
                    tasbeehCount  = it.tasbeeh_count,
                    prayersLogged = it.prayers_logged,
                    totalMinutes  = it.total_minutes,
                )
            },
            tasbeehSessions = db.tasbeehSessionQueries.getAll().executeAsList().map {
                ExportedTasbeehSession(
                    dhikrText       = it.dhikr_text,
                    count           = it.count,
                    target          = it.target,
                    completed       = it.completed == 1L,
                    durationSeconds = it.duration_seconds,
                    date            = it.date,
                    createdAt       = it.created_at,
                )
            },
            prayerLogs = db.prayerLogQueries.getAll().executeAsList().map {
                ExportedPrayerLog(
                    date       = it.date,
                    prayerName = it.prayer_name,
                    prayed     = it.prayed == 1L,
                    inMasjid   = it.in_masjid == 1L,
                    sunnahDone = it.sunnah_done == 1L,
                )
            },
            quranBookmarks = db.quranBookmarkQueries.getAll().executeAsList().map {
                ExportedBookmark(
                    surah     = it.surah,
                    ayah      = it.ayah,
                    page      = it.page,
                    note      = it.note,
                    createdAt = it.created_at,
                )
            },
            customAdhkar = db.customDhikrQueries.getAll().executeAsList().map {
                ExportedCustomDhikr(
                    dhikrText = it.dhikr_text,
                    target    = it.target,
                    createdAt = it.created_at,
                )
            },
            quranLastRead = db.quranLastReadQueries.get().executeAsOneOrNull()?.let {
                ExportedLastRead(it.surah, it.ayah, it.page)
            },
            favoriteDuas = db.duaQueries.getFavorites().executeAsList().map { it.text_ar },
            unlockedAchievements = db.achievementQueries.getAll().executeAsList().map { it.key },
        )

        json.encodeToString(RafiqExport.serializer(), export)
    }

    override suspend fun importFromJson(jsonText: String): ImportResult = withContext(Dispatchers.IO) {
        val data = runCatching {
            reader.decodeFromString(RafiqExport.serializer(), jsonText)
        }.getOrElse {
            /*  الفرقُ بين «ليس JSON» و«ليس تصديرَ رفيق» يهمّ المستخدم:
             *  الأوّلُ ملفٌّ تالف، والثاني ملفٌّ صحيحٌ من تطبيقٍ آخر. */
            val looksJson = jsonText.trimStart().startsWith("{")
            return@withContext ImportResult.Invalid(
                if (looksJson) ImportResult.Reason.NOT_RAFIQ
                else ImportResult.Reason.NOT_JSON
            )
        }

        /*  صيغةٌ أحدثُ ممّا نفهم لا تُستورَد **جزئياً**.
         *
         *  فيها حقولٌ قد تُغيّر معنى ما نعرفه، واستيرادُ نصفها يعطي
         *  المستخدم بياناتٍ يظنّها تامّةً وليست كذلك. والرفضُ الصريح
         *  خيرٌ من استعادةٍ صامتةٍ ناقصة. */
        if (data.formatVersion > supportedFormat) {
            return@withContext ImportResult.Invalid(ImportResult.Reason.FUTURE_VERSION)
        }

        /*  معاملةٌ واحدة: إمّا استيرادٌ تامّ وإمّا لا شيء.
         *
         *  ولا يُمسح شيءٌ قبلها — الاستيرادُ **دمجٌ لا استبدال**:
         *  `INSERT OR IGNORE` في الأيّام وسجلّ الصلاة والعلامات، فلا
         *  يدهس المستوردُ يوماً أفضلَ ممّا في الملفّ. ومن أراد البدءَ
         *  من الملفّ وحدَه يمسح بياناتِه أوّلاً — وذاك زرٌّ ظاهرٌ في
         *  الشاشة نفسِها، لا أثرٌ خفيٌّ لزرِّ الاستيراد. */
        db.transaction {
            data.prefs?.let { p ->
                db.userPrefsQueries.initIfNeeded()
                db.userPrefsQueries.updatePrayerMethod(p.prayerMethod)
                db.userPrefsQueries.updateMadhab(p.madhab)
                db.userPrefsQueries.updateLocale(p.locale)
                db.userPrefsQueries.updateHijriOffset(p.hijriOffset)
                db.userPrefsQueries.updateElevation(p.elevation)
                db.userPrefsQueries.updatePrayerOffsets(
                    p.fajrOffset, p.dhuhrOffset, p.asrOffset, p.maghribOffset, p.ishaOffset,
                )
                /*  السمةُ تُكتب بلونٍ ديناميكيٍّ افتراضيّ: التصديرُ لا
                 *  يحمل `dynamic_color`، وتخمينُه خطأٌ في نصف الحالات.
                 *  فيبقى ما عند الجهاز ولا يُبدَّل إلّا اسمُ السمة. */
                val dynamic = db.userPrefsQueries.get().executeAsOneOrNull()?.dynamic_color == 1L
                db.userPrefsQueries.updateTheme(p.theme, if (dynamic) 1L else 0L)
            }

            data.streak?.let {
                db.streakDataQueries.upsert(it.currentStreak, it.longestStreak, it.lastActiveDate)
            }

            data.dailyProgress.forEach { d ->
                db.dailyProgressQueries.insertNew(d.date)
                db.dailyProgressQueries.updateMorning(if (d.morningDone) 1L else 0L, d.date)
                db.dailyProgressQueries.updateEvening(if (d.eveningDone) 1L else 0L, d.date)
                db.dailyProgressQueries.updateQuranPages(d.quranPages, d.date)
                db.dailyProgressQueries.updateTasbeeh(d.tasbeehCount, d.date)
                db.dailyProgressQueries.updatePrayers(d.prayersLogged, d.date)
                db.dailyProgressQueries.updateMinutes(d.totalMinutes, d.date)
                db.streakDataQueries.insertHistory(d.date)
            }

            data.tasbeehSessions.forEach {
                db.tasbeehSessionQueries.insert(
                    it.dhikrText, it.count, it.target,
                    if (it.completed) 1L else 0L, it.durationSeconds, it.date, it.createdAt,
                )
            }

            data.prayerLogs.forEach {
                db.prayerLogQueries.insertNew(
                    it.date, it.prayerName, if (it.prayed) 1L else 0L, 0L,
                    if (it.inMasjid) 1L else 0L, if (it.sunnahDone) 1L else 0L,
                )
            }

            data.quranBookmarks.forEach {
                db.quranBookmarkQueries.insert(it.surah, it.ayah, it.page, it.createdAt, it.note)
            }

            data.customAdhkar.forEach {
                db.customDhikrQueries.insert(it.dhikrText, it.target, it.createdAt)
            }

            data.quranLastRead?.let {
                db.quranLastReadQueries.upsert(it.surah, it.ayah, it.page, 0.0, 0L)
            }

            data.unlockedAchievements.forEach { db.achievementQueries.unlock(it, 0L) }

            /*  المفضّلةُ تُطابَق بالنصّ لا بالمعرّف.
             *
             *  المعرّفاتُ أرقامُ صفوفٍ تُولَّد عند البذر، فتختلف بين
             *  جهازٍ وجهاز — واستعادتُها بالرقم تُفضّل دعاءً غير الذي
             *  فضّله صاحبُه. والنصُّ العربيُّ هو الثابت. */
            if (data.favoriteDuas.isNotEmpty()) {
                val wanted = data.favoriteDuas.toSet()
                db.duaQueries.getCategories().executeAsList().forEach { cat ->
                    db.duaQueries.getAllByCategory(cat).executeAsList().forEach { dua ->
                        if (dua.text_ar in wanted) db.duaQueries.toggleFavorite(1L, dua.id)
                    }
                }
            }
        }

        ImportResult.Success(
            days      = data.dailyProgress.size,
            sessions  = data.tasbeehSessions.size,
            prayers   = data.prayerLogs.size,
            bookmarks = data.quranBookmarks.size,
            adhkar    = data.customAdhkar.size,
        )
    }

    override suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        db.transaction {
            db.dailyProgressQueries.deleteAll()
            db.streakDataQueries.deleteAllStreak()
            db.streakDataQueries.deleteAllHistory()
            db.tasbeehSessionQueries.deleteAll()
            db.prayerLogQueries.deleteAll()
            db.quranBookmarkQueries.deleteAll()
            db.quranLastReadQueries.deleteAll()
            db.customDhikrQueries.deleteAll()
            db.achievementQueries.deleteAll()
            db.dayStationLogQueries.deleteAll()
            db.duaQueries.clearFavorites()
            db.userPrefsQueries.resetRow()
            db.userPrefsQueries.initIfNeeded()
        }
    }
}
