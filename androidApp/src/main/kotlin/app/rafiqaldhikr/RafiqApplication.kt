package app.rafiqaldhikr

import android.app.Application
import app.rafiq.di.sharedModule
import app.rafiqaldhikr.R
import app.rafiqaldhikr.di.androidModule
import app.rafiqaldhikr.di.serviceModule
import app.rafiqaldhikr.di.viewModelModule
import app.rafiqaldhikr.service.PrayerRescheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.GlobalContext

class RafiqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        /*  أوّلُ شيء — قبل أيّ عملٍ قد يسقط.
         *
         *  «يفتح ويُغلق فوراً» ليست معلومةً يُشخَّص بها شيء: لا سجلَّ يصل
         *  من جهاز صاحبِه، فيبقى السببُ تخميناً. وهذا يكتب أثرَ الانهيار
         *  في ملفٍّ يعرضه الفتحُ التالي. */
        app.rafiqaldhikr.util.CrashLog.install(this)

        // ═══ لغة التطبيق الافتراضية: العربية (المحتوى عربي أولاً) ═══
        if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().isEmpty) {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags("ar")
            )
        }

        // ═══ Koin DI ═══
        startKoin {
            androidContext(this@RafiqApplication)
            modules(
                sharedModule,
                androidModule,
                viewModelModule,
                serviceModule
            )
        }

        /*  قنواتُ الإشعار تُنشأ عند الإقلاع لا عند أوّل أذان.
         *
         *  كانتا تُنشآن داخل `onReceive` — فقبل أن يفرغ أوّلُ تنبيهٍ لا
         *  وجودَ لهما في النظام: صفحةُ إعدادات الإشعارات في أندرويد تظهر
         *  بلا فئاتٍ، ولا سبيل للمستخدم أن يضبط صوتاً أو أهميّةً لشيء.
         */
        createNotificationChannels()

        // ═══ تعبئة قاعدة البيانات (القرآن والأذكار والأدعية والتفسير) ═══
        // الشاشات تراقب Flows فتمتلئ تلقائياً فور اكتمال التعبئة
        /*  `Throwable` لا `Exception`.
         *
         *  التعبئةُ تُحلّل ملفَّي JSON مجموعُهما خمسةُ ميغابايت إلى ١٢٤٧٢
         *  صفّاً. وما يقع هنا على جهازٍ ضيّق الذاكرة `OutOfMemoryError`،
         *  وما يقع على أندرويد قديمٍ `NoSuchMethodError` — وكلاهما
         *  **`Error` لا `Exception`**، فلا يلتقطهما `catch (e: Exception)`.
         *  ونازلٌ غيرُ ملتقَطٍ في كوروتين يقتل العملية.
         *
         *  وقتلُها هنا يعني أنّ التطبيق يُفتح ويُغلق فوراً، في كل مرّة،
         *  ما دامت التعبئةُ ناقصة — وهي تُعاد عند كل إقلاع. أي حلقةُ
         *  انهيارٍ لا تنكسر بإعادة الفتح.
         *
         *  والتطبيقُ يعمل بمحتوًى ناقصٍ خيرٌ من تطبيقٍ لا يُفتح. */
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<app.rafiq.data.db.DatabaseSeeder>().seedIfNeeded()
            } catch (t: Throwable) {
                android.util.Log.e("RafiqSeeder", "فشل تعبئة قاعدة البيانات", t)
            }
        }

        // ═══ جدولة إشعارات الأذان عند كل فتح للتطبيق ═══
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (_: Throwable) {
                // كسابقه: جدولةُ تنبيهٍ لا تستحقّ إسقاطَ التطبيق.
            }
        }

    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val manager = getSystemService(android.app.NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            android.app.NotificationChannel(
                "prayer_channel",
                getString(R.string.prayer_times_title),
                android.app.NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                setSound(
                    android.media.RingtoneManager
                        .getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION),
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build(),
                )
            }
        )
        manager.createNotificationChannel(
            android.app.NotificationChannel(
                "adhkar_channel",
                getString(R.string.notif_channel_adhkar),
                android.app.NotificationManager.IMPORTANCE_DEFAULT,
            )
        )

    }
}
