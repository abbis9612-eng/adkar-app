package app.rafiq.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.rafiq.db.RafiqDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(
            schema  = RafiqDatabase.Schema,
            context = context,
            name    = "rafiq.db"
        )
        // ═══ تصحيح الترقية: إضافة أعمدة جديدة + تحديث القيم للمستخدمين الحاليين ═══
        migrateIfNeeded(driver)
        return driver
    }

    /**
     * ترقية يدوية آمنة للمستخدمين الذين ثبّتوا فوق نسخة قديمة:
     * تضيف كل الأعمدة الجديدة وتنشئ كل الجداول الجديدة إن لم توجد —
     * فلا تنهار الشاشات باستعلام جدول/عمود غير موجود.
     * كل عملية مغلّفة بـ try لأن SQLite لا يدعم "ADD COLUMN IF NOT EXISTS".
     */
    private fun migrateIfNeeded(driver: SqlDriver) {
        /**
         * كان هذا `catch (_: Exception) {}` صامتاً تماماً: عمودٌ يفشل إنشاؤه
         * لسببٍ حقيقي يمرّ بلا أثر، فتنهار شاشةٌ بعد أسابيع باستعلام عمودٍ
         * غير موجود ولا شيء في السجل يدلّ على السبب.
         *
         * "duplicate column name" وحده متوقَّع — SQLite لا يدعم
         * ADD COLUMN IF NOT EXISTS، فهو الطريقة الوحيدة لمعرفة أن العمود
         * موجود أصلاً. أي خطأ آخر يُسجَّل بصوت عالٍ.
         */
        fun exec(sql: String) {
            try {
                driver.execute(null, sql, 0)
            } catch (e: Exception) {
                val expected =
                    e.message?.contains("duplicate column name", ignoreCase = true) == true
                if (!expected) {
                    android.util.Log.e(
                        "RafiqMigration",
                        "فشل ترحيل غير متوقَّع: ${sql.lineSequence().first().trim()}",
                        e,
                    )
                }
            }
        }

        // ═══ أعمدة UserPrefs الجديدة ═══
        exec("ALTER TABLE UserPrefs ADD COLUMN elevation REAL NOT NULL DEFAULT 0.0")
        exec("ALTER TABLE UserPrefs ADD COLUMN madhab TEXT NOT NULL DEFAULT 'shafi'")
        exec("ALTER TABLE UserPrefs ADD COLUMN numerals TEXT NOT NULL DEFAULT 'arabic'")

        // ═══ الجداول الجديدة (رفيق اليوم، التفسير، الإنجازات) ═══
        exec("""CREATE TABLE IF NOT EXISTS Tafsir (
                    surah INTEGER NOT NULL,
                    ayah  INTEGER NOT NULL,
                    text  TEXT    NOT NULL,
                    PRIMARY KEY (surah, ayah)
                )""")
        exec("""CREATE TABLE IF NOT EXISTS Achievement (
                    key         TEXT    NOT NULL PRIMARY KEY,
                    unlocked_at INTEGER NOT NULL
                )""")
        exec("""CREATE TABLE IF NOT EXISTS DayStationLog (
                    date         TEXT    NOT NULL,
                    station      TEXT    NOT NULL,
                    completed_at INTEGER NOT NULL,
                    PRIMARY KEY (date, station)
                )""")

        /*  حُذف هنا: `UPDATE UserPrefs SET prayer_method='mwl' WHERE
         *  prayer_method='umm_al_qura'`.
         *
         *  كان يُنفَّذ في **كل إقلاع** لا مرّةً واحدة — ولا سبيل لجعله
         *  لمرّةٍ بلا جدول نسخة. و«أم القرى» طريقةٌ معروضةٌ للاختيار في
         *  `PrayerMethodScreen`. فمن اختارها — وهو في السعودية غالباً —
         *  عاد إلى MWL عند أوّل إعادة تشغيل، ويصلّي على مواقيتَ ليست التي
         *  اختار، ولا شيء يخبره.
         *
         *  وتصحيحُ اختيارٍ صريحٍ للمستخدم ليس ترحيلاً.  */
    }
}
