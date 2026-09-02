package app.rafiq.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.rafiq.db.RafiqDatabase

actual class DatabaseDriverFactory(private val context: Context) {

    private companion object {
        const val DB = "rafiq.db"
    }

    /**
     * يُصلح `PRAGMA user_version` قبل أن يفتح المحرِّكُ القاعدة.
     *
     * على الأجهزة التي شغّلت نسخةً كتبت رقمَ ترحيلها في `user_version`
     * (انظر [RafiqMigrations]) يبقى الحقلُ **٢** والمخطّطُ يطلب **١**.
     * فيرى `SQLiteOpenHelper` تنزيلَ نسخةٍ فيرمي، والرميُ يقع **عند بناء
     * المحرِّك** — أي قبل أيّ كودٍ لنا وقبل أن تُرسم شاشة. فالتطبيق يفتح
     * ويُغلق فوراً في كل مرّة، ولا شيءَ داخل التطبيق يستطيع إنقاذه.
     *
     * فالإصلاحُ يسبق المحرِّك: تُفتح القاعدةُ بواجهة أندرويد المباشرة،
     * ويُنقل الرقمُ إلى جدولنا، ويُعاد `user_version` إلى نسخة المخطّط.
     *
     * ولا يُنشئ ملفّاً: تثبيتٌ جديدٌ لا قاعدةَ له، فلا شيءَ يُصلَح.
     */
    private fun repairSchemaVersion() {
        val file = context.getDatabasePath(DB)
        if (!file.exists()) return

        runCatching {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
                val schemaVersion = RafiqDatabase.Schema.version.toInt()
                val stored = db.version
                if (stored <= schemaVersion) return@use

                Log.w(
                    "RafiqMigration",
                    "user_version = $stored والمخطّط $schemaVersion — يُصلَح قبل فتح المحرّك",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS RafiqMigration (" +
                        "id INTEGER NOT NULL PRIMARY KEY, version INTEGER NOT NULL)"
                )
                //  الرقمُ المحفوظ هو رقمُ ترحيلنا فعلاً — يُنقل كما هو
                //  فلا يُعاد تنفيذُ ترحيلٍ نُفِّذ.
                db.execSQL("INSERT OR REPLACE INTO RafiqMigration(id, version) VALUES (1, $stored)")
                db.version = schemaVersion
            }
        }.onFailure {
            //  إن تعذّر الإصلاحُ فالمحرِّكُ سيرمي كما كان — لكنّ السببَ
            //  مسجَّلٌ، ولا نُضيف انهياراً ثانياً فوق الأوّل.
            Log.e("RafiqMigration", "تعذّر إصلاح نسخة المخطّط", it)
        }
    }

    actual fun createDriver(): SqlDriver {
        repairSchemaVersion()

        val driver = AndroidSqliteDriver(
            schema  = RafiqDatabase.Schema,
            context = context,
            name    = DB
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
