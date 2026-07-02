package app.rafiqaldhikr.service

import android.content.Context
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult

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
    suspend fun reschedule() {
        val alarmManager = PrayerAlarmManager(context)
        val prefs = db.userPrefsQueries.get().executeAsOneOrNull() ?: return

        if (prefs.notifications_enabled == 0L) {
            alarmManager.cancelAll()
            return
        }

        val lat = prefs.last_known_lat.takeIf { it != 0.0 } ?: 35.5558
        val lng = prefs.last_known_lng.takeIf { it != 0.0 } ?: 45.4436

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

        val times = if (today.isha > System.currentTimeMillis()) {
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

        alarmManager.scheduleAllForToday(times.toAlarmMap())
    }

    private fun PrayerTimesResult.toAlarmMap() = mapOf(
        "fajr"    to fajr,
        "dhuhr"   to dhuhr,
        "asr"     to asr,
        "maghrib" to maghrib,
        "isha"    to isha
    )
}
