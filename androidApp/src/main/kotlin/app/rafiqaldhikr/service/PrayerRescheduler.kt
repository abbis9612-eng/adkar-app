package app.rafiqaldhikr.service

import android.content.Context
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult
import androidx.glance.appwidget.updateAll
import app.rafiqaldhikr.util.coordsOrNull
import app.rafiqaldhikr.widget.PrayerWidget

/**
 * يعيد جدولة إشعارات الأذان من التفضيلات المخزنة:
 * مواقيت اليوم إن بقي منها شيء، وإلا مواقيت الغد.
 * يُستدعى عند الإقلاع، وعند فتح التطبيق، وبعد إشعار العشاء.
 */
class PrayerRescheduler(
    private val context:    Context,
    private val db:         RafiqDatabase,
    private val calculator: PrayerTimeCalculator
) {
    /**
     * يعيد الجدولة، ثمّ يُحدّث الودجت.
     *
     * ولم يكن شيءٌ في التطبيق كلِّه ينادي `updateAll` — فالودجت لا يتغيّر
     * إلّا بدورة النظام كلَّ نصف ساعة (والنظام يتجاوزها في السبات). فمن
     * حدّد موقعَه ثمّ نظر إلى شاشته الرئيسية بقي يقرأ «حدّد موقعك» نصفَ
     * ساعة. وهذه الدالّة تُنادى من كل موضعٍ يُغيّر الحساب — الإقلاع،
     * وفتحُ التطبيق، وتبديلُ الطريقة أو المذهب أو الإزاحات — فهي موضعُه.
     */
    suspend fun reschedule() {
        try {
            rescheduleAlarms()
        } finally {
            runCatching {
                PrayerWidget().updateAll(context)
            }
        }
    }

    private suspend fun rescheduleAlarms() {
        val alarmManager = PrayerAlarmManager(context)
        val prefs = db.userPrefsQueries.get().executeAsOneOrNull() ?: return

        //  معرّفاتُ المثبَّتة تُمرَّر إلى الإلغاء: وإلّا بقيت تذكيراتُها
        //  تعمل بعد إطفاء التنبيهات أو محو الموقع.
        val pinIds = runCatching {
            db.dhikrPinQueries.getAll().executeAsList().map { it.dhikr_id }
        }.getOrDefault(emptyList())

        if (prefs.notifications_enabled == 0L) {
            alarmManager.cancelAll(pinIds)
            return
        }

        // أخطر موضع في التطبيق: أذانٌ مجدول على إحداثية ليست إحداثية المستخدم
        // يوقظه على وقت خاطئ ويثق به. بلا موقع لا جدولة — والصمت هنا صدق.
        val here = coordsOrNull(prefs.last_known_lat, prefs.last_known_lng)
        if (here == null) {
            alarmManager.cancelAll(pinIds)
            return
        }
        val lat = here.lat
        val lng = here.lng

        val today = calculator.calculate(
            lat           = lat,
            lng           = lng,
            method        = prefs.prayer_method,
            elevation     = prefs.elevation,
            madhab        = prefs.madhab,
            fajrOffset    = prefs.fajr_offset.toInt(),
            dhuhrOffset   = prefs.dhuhr_offset.toInt(),
            asrOffset     = prefs.asr_offset.toInt(),
            maghribOffset = prefs.maghrib_offset.toInt(),
            ishaOffset    = prefs.isha_offset.toInt()
        )

        // نهاية يوم التنبيهات = تذكير النوم (بعد العشاء) — حتى لا نلغي تذكيراً معلقاً
        val endOfAlarmDay = today.isha + PrayerAlarmManager.SLEEP_DELAY_MS
        val times = if (endOfAlarmDay > System.currentTimeMillis()) {
            today
        } else {
            calculator.calculateForTomorrow(
                lat           = lat,
                lng           = lng,
                method        = prefs.prayer_method,
                elevation     = prefs.elevation,
                madhab        = prefs.madhab,
                fajrOffset    = prefs.fajr_offset.toInt(),
                dhuhrOffset   = prefs.dhuhr_offset.toInt(),
                asrOffset     = prefs.asr_offset.toInt(),
                maghribOffset = prefs.maghrib_offset.toInt(),
                ishaOffset    = prefs.isha_offset.toInt()
            )
        }

        val alarmMap = times.toAlarmMap()
        alarmManager.scheduleAllForToday(
            prayerTimes = alarmMap,
            prayers     = prefs.notify_prayers == 1L,
            morning     = prefs.notify_morning == 1L,
            evening     = prefs.notify_evening == 1L,
        )

        /*  تذكيراتُ الأذكار المثبَّتة — تُجدوَل مع مواقيت اليوم نفسِها.
         *
         *  وموضعُها هنا لا في شاشةٍ: هذا هو الموضعُ الذي يُنادى عند
         *  الإقلاع وعند تبديل الوقت وعند تغيّر الموقع. فلو جُدولت في
         *  الشاشة لضاعت تذكيراتُ من لم يفتحها بعد سفرٍ أو إعادةِ تشغيل. */
        runCatching {
            val pins = db.dhikrPinQueries.getReminders().executeAsList()
            alarmManager.schedulePinned(
                prayerTimes = alarmMap,
                pins = pins.map { Triple(it.dhikr_id, it.meeqat, it.offset_m.toInt()) },
            )
        }
    }

    private fun PrayerTimesResult.toAlarmMap() = mapOf(
        "fajr"    to fajr,
        "dhuhr"   to dhuhr,
        "asr"     to asr,
        "maghrib" to maghrib,
        "isha"    to isha
    )
}
