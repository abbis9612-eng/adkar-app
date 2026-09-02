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
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<app.rafiq.data.db.DatabaseSeeder>().seedIfNeeded()
            } catch (e: Exception) {
                android.util.Log.e("RafiqSeeder", "فشل تعبئة قاعدة البيانات", e)
            }
        }

        // ═══ جدولة إشعارات الأذان عند كل فتح للتطبيق ═══
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (_: Exception) {
                // offline — ستُعاد المحاولة من شاشة المواقيت أو عند الإقلاع
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
