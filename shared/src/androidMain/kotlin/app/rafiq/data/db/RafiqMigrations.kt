package app.rafiq.data.db

import android.util.Log
import app.cash.sqldelight.db.SqlDriver

/**
 * ترحيلاتُ القاعدة — مرقَّمةٌ، تُنفَّذ مرّةً واحدة، ولا تبتلع خطأً.
 *
 * ═══ ما كان قبلها ═══
 *
 * لا ملفَّ `.sqm` واحدٌ في المستودع، و`verifyMigrations = true` في
 * `shared/build.gradle.kts` لا يتحقّق من شيءٍ لأنّه لا شيء ليتحقّق منه.
 * وبدلاً منها قائمةٌ يدويّةٌ من `ALTER TABLE` تُنفَّذ **في كل إقلاع**
 * ويُبتلع فشلُها، وتغطّي ثلاثةَ أعمدةٍ وثلاثةَ جداول — بينما المخطّط في
 * ملفّات `.sq` راكم منذها نحوَ خمسةَ عشرَ عموداً وجدولين لم تُذكر فيها:
 * ‏`sound_profile` و`haptics_enabled` و`reduced_motion` و`high_contrast`
 * و`hijri_offset` وإزاحاتُ الصلوات الخمس و`onboarding_completed`
 * و`last_known_*`، و`QuranBookmark.page` و`.note`، و`PrayerLog.in_masjid`
 * و`.sunnah_done`، و`DailyProgress.total_minutes`، و`QuranLastRead.scroll_y`،
 * وجدولا `StreakHistory` و`CustomDhikr`.
 *
 * وأيُّ واحدٍ من هذه ينقص على جهازٍ رقّى تطبيقَه = `no such column` وانهيارُ
 * شاشة. ولا يظهر في اختبارٍ ولا في CI لأنّ كلَّ تشغيلٍ هناك تثبيتٌ نظيف.
 *
 * ═══ ما صارت ═══
 *
 * ‏`PRAGMA user_version` يحمل رقمَ آخرِ ترحيلٍ نُفِّذ، فلا يُعاد تنفيذُ
 * شيءٍ في كل إقلاع. والترحيلُ الأوّل يُصالح أيَّ قاعدةٍ قديمةٍ مع المخطّط
 * الحاضر كاملاً: كلُّ عمودٍ يُضاف، وكلُّ جدولٍ يُنشأ إن لم يوجد.
 *
 * وإضافةُ عمودٍ أو جدولٍ إلى `.sq` بعد اليوم توجب ترحيلاً جديداً في
 * [MIGRATIONS] — و`tools/check_schema.py` يفشل في CI إن نُسي.
 */
internal object RafiqMigrations {

    /**
     * كلُّ ترحيلٍ قائمةُ أوامرٍ تُنفَّذ بالترتيب.
     *
     * الفهرسُ صفرٌ = الترحيل رقم ١. لا يُحذف ترحيلٌ ولا يُعاد ترتيبُه —
     * أرقامُها محفوظةٌ في أجهزة الناس.
     */
    val MIGRATIONS: List<List<String>> = listOf(

        // ═══ ١ — مصالحةُ كل قاعدةٍ سابقةٍ مع المخطّط الحاضر ═══
        //
        // تجمع ما كانت تفعله `migrateIfNeeded` وما فاتها. وكلُّها آمنةٌ
        // على قاعدةٍ حديثةٍ: العمودُ الموجود يفشل بـ«duplicate column»
        // وهو متوقَّع، والجدولُ الموجود يمرّ بـ IF NOT EXISTS.
        listOf(
            // UserPrefs — نمت أكثر من غيرها
            "ALTER TABLE UserPrefs ADD COLUMN elevation REAL NOT NULL DEFAULT 0.0",
            "ALTER TABLE UserPrefs ADD COLUMN madhab TEXT NOT NULL DEFAULT 'shafi'",
            "ALTER TABLE UserPrefs ADD COLUMN numerals TEXT NOT NULL DEFAULT 'arabic'",
            "ALTER TABLE UserPrefs ADD COLUMN hijri_offset INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN reduced_motion INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN high_contrast INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN sound_profile TEXT NOT NULL DEFAULT 'beads'",
            "ALTER TABLE UserPrefs ADD COLUMN haptics_enabled INTEGER NOT NULL DEFAULT 1",
            "ALTER TABLE UserPrefs ADD COLUMN glass_level TEXT NOT NULL DEFAULT 'fake'",
            "ALTER TABLE UserPrefs ADD COLUMN gamification_visible INTEGER NOT NULL DEFAULT 1",
            "ALTER TABLE UserPrefs ADD COLUMN locale TEXT NOT NULL DEFAULT 'ar'",
            "ALTER TABLE UserPrefs ADD COLUMN last_known_city TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE UserPrefs ADD COLUMN last_known_lat REAL NOT NULL DEFAULT 0.0",
            "ALTER TABLE UserPrefs ADD COLUMN last_known_lng REAL NOT NULL DEFAULT 0.0",
            "ALTER TABLE UserPrefs ADD COLUMN onboarding_completed INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN fajr_offset INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN dhuhr_offset INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN asr_offset INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN maghrib_offset INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE UserPrefs ADD COLUMN isha_offset INTEGER NOT NULL DEFAULT 0",

            // أعمدةٌ فاتت القائمةَ اليدوية كلَّها
            "ALTER TABLE QuranBookmark ADD COLUMN page INTEGER NOT NULL DEFAULT 1",
            "ALTER TABLE QuranBookmark ADD COLUMN note TEXT",
            "ALTER TABLE PrayerLog ADD COLUMN in_masjid INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE PrayerLog ADD COLUMN sunnah_done INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE DailyProgress ADD COLUMN total_minutes INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE QuranLastRead ADD COLUMN scroll_y REAL NOT NULL DEFAULT 0.0",
            "ALTER TABLE Adhkar ADD COLUMN virtue TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE Adhkar ADD COLUMN audio_file TEXT",
            "ALTER TABLE Dua ADD COLUMN occasion TEXT NOT NULL DEFAULT ''",

            // جداولُ أُضيفت بعد الإصدار الأوّل
            """CREATE TABLE IF NOT EXISTS Tafsir (
                   surah INTEGER NOT NULL,
                   ayah  INTEGER NOT NULL,
                   text  TEXT    NOT NULL,
                   PRIMARY KEY (surah, ayah)
               )""",
            """CREATE TABLE IF NOT EXISTS Achievement (
                   key         TEXT    NOT NULL PRIMARY KEY,
                   unlocked_at INTEGER NOT NULL
               )""",
            """CREATE TABLE IF NOT EXISTS DayStationLog (
                   date         TEXT    NOT NULL,
                   station      TEXT    NOT NULL,
                   completed_at INTEGER NOT NULL,
                   PRIMARY KEY (date, station)
               )""",
            """CREATE TABLE IF NOT EXISTS StreakHistory (
                   id   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                   date TEXT    NOT NULL UNIQUE
               )""",
            """CREATE TABLE IF NOT EXISTS CustomDhikr (
                   id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                   dhikr_text  TEXT    NOT NULL,
                   target      INTEGER NOT NULL,
                   created_at  INTEGER NOT NULL
               )""",
            """CREATE TABLE IF NOT EXISTS QuranLastRead (
                   id         INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
                   surah      INTEGER NOT NULL DEFAULT 1,
                   ayah       INTEGER NOT NULL DEFAULT 1,
                   page       INTEGER NOT NULL DEFAULT 1,
                   scroll_y   REAL    NOT NULL DEFAULT 0.0,
                   updated_at INTEGER NOT NULL
               )""",

            // فهارسُ الأداء — IF NOT EXISTS فلا تُكرَّر
            "CREATE INDEX IF NOT EXISTS idx_ayah_surah ON Ayah(surah)",
            "CREATE INDEX IF NOT EXISTS idx_ayah_page ON Ayah(page)",
            "CREATE INDEX IF NOT EXISTS idx_ayah_juz ON Ayah(juz)",
            "CREATE INDEX IF NOT EXISTS idx_dua_category ON Dua(category)",
        ),
    )

    /** رقمُ آخرِ ترحيلٍ معروف. تُقارَن به `PRAGMA user_version`. */
    val LATEST: Int get() = MIGRATIONS.size

    /**
     * أخطاءٌ متوقَّعةٌ لا تدلّ على عطب.
     *
     * SQLite لا يعرف `ADD COLUMN IF NOT EXISTS`، فمحاولةُ إضافة عمودٍ
     * موجودٍ هي الطريقةُ الوحيدة لمعرفة أنّه موجود. وما عداها يُسجَّل
     * بصوتٍ عالٍ: عمودٌ يفشل إنشاؤه لسببٍ حقيقيّ كان يمرّ بلا أثرٍ فتنهار
     * شاشةٌ بعد أسابيعَ ولا شيء في السجلّ يدلّ على السبب.
     */
    private fun isExpected(e: Exception): Boolean {
        val m = e.message.orEmpty().lowercase()
        return "duplicate column name" in m || "already exists" in m
    }

    /**
     * ينفّذ ما لم يُنفَّذ من الترحيلات.
     *
     * @return رقمُ النسخة بعد التنفيذ.
     */
    fun runOn(driver: SqlDriver): Int {
        val from = readUserVersion(driver)
        if (from >= LATEST) return from

        for (index in from until LATEST) {
            val number = index + 1
            var failures = 0
            MIGRATIONS[index].forEach { sql ->
                try {
                    driver.execute(null, sql, 0)
                } catch (e: Exception) {
                    if (!isExpected(e)) {
                        failures++
                        Log.e(
                            TAG,
                            "ترحيل $number: فشل غيرُ متوقَّع في «${sql.lineSequence().first().trim()}»",
                            e,
                        )
                    }
                }
            }
            /*  النسخةُ تُرفع حتى مع فشلٍ جزئيّ.
             *
             *  البديلُ إعادةُ المحاولة في كل إقلاعٍ إلى الأبد — وهو ما كان
             *  يقع فعلاً، بلا فائدةٍ ولا أثر. والفشلُ هنا مسجَّلٌ بصوتٍ
             *  عالٍ، والبذرُ ذاتيُّ الإصلاح يلتقط ما يقدر عليه.  */
            if (failures > 0) {
                Log.e(TAG, "ترحيل $number: $failures أمرٍ فشل — القاعدةُ قد تكون ناقصة")
            }
            writeUserVersion(driver, number)
        }
        return LATEST
    }

    private fun readUserVersion(driver: SqlDriver): Int =
        driver.executeQuery(null, "PRAGMA user_version", { cursor ->
            app.cash.sqldelight.db.QueryResult.Value(
                if (cursor.next().value) cursor.getLong(0)?.toInt() ?: 0 else 0
            )
        }, 0).value

    private fun writeUserVersion(driver: SqlDriver, version: Int) {
        driver.execute(null, "PRAGMA user_version = $version", 0)
    }

    private const val TAG = "RafiqMigration"
}
