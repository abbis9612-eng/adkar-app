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
     * ١. يضيف الأعمدة الجديدة (elevation, madhab) إذا لم تكن موجودة
     * ٢. يحدّث طريقة الحساب من umm_al_qura إلى mwl إذا لم تتغير يدوياً
     */
    private fun migrateIfNeeded(driver: SqlDriver) {
        // إضافة عمود الارتفاع
        try {
            driver.execute(null,
                "ALTER TABLE UserPrefs ADD COLUMN elevation REAL NOT NULL DEFAULT 0.0", 0)
        } catch (_: Exception) { /* موجود مسبقاً */ }

        // إضافة عمود المذهب
        try {
            driver.execute(null,
                "ALTER TABLE UserPrefs ADD COLUMN madhab TEXT NOT NULL DEFAULT 'shafi'", 0)
        } catch (_: Exception) { /* موجود مسبقاً */ }

        // ═══ تحديث الطريقة من umm_al_qura → mwl للمستخدمين الحاليين ═══
        // فقط إذا كانت لا تزال umm_al_qura (لم يُغيّرها المستخدم يدوياً)
        try {
            driver.execute(null,
                "UPDATE UserPrefs SET prayer_method = 'mwl' WHERE id = 1 AND prayer_method = 'umm_al_qura'", 0)
        } catch (_: Exception) { /* لا بأس */ }
    }
}
