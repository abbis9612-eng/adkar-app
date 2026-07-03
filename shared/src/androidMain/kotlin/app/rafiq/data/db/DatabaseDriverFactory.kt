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
        fun exec(sql: String) = try { driver.execute(null, sql, 0) } catch (_: Exception) {}

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

        // ═══ تحديث الطريقة من umm_al_qura → mwl للمستخدمين الحاليين ═══
        exec("UPDATE UserPrefs SET prayer_method = 'mwl' WHERE id = 1 AND prayer_method = 'umm_al_qura'")
    }
}
