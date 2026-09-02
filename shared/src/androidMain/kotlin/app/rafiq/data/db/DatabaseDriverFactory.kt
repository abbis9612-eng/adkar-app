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

        /*  ترحيلٌ مرقَّمٌ يُنفَّذ مرّةً واحدة — انظر [RafiqMigrations].
         *
         *  كان هنا سردٌ من `ALTER TABLE` يُنفَّذ في كل إقلاعٍ ويُبتلع
         *  فشلُه، ويغطّي ثلاثةَ أعمدةٍ وثلاثةَ جداول بينما المخطّط راكم
         *  خمسةَ عشرَ عموداً وجدولين لم تُذكر فيه. وكان فيه كذلك
         *  `UPDATE UserPrefs SET prayer_method='mwl' WHERE 'umm_al_qura'`
         *  يُصفّر اختيارَ المستخدم في كل تشغيل.
         */
        RafiqMigrations.runOn(driver)
        return driver
    }
}
