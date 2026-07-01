package app.rafiqaldhikr.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class PrayerAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val FAJR_ID    = 100
        const val DHUHR_ID   = 101
        const val ASR_ID     = 102
        const val MAGHRIB_ID = 103
        const val ISHA_ID    = 104
    }

    fun scheduleAllForToday(prayerTimes: Map<String, Long>) {
        prayerTimes.forEach { (name, timeMillis) ->
            val notifId = when (name) {
                "fajr"    -> FAJR_ID
                "dhuhr"   -> DHUHR_ID
                "asr"     -> ASR_ID
                "maghrib" -> MAGHRIB_ID
                "isha"    -> ISHA_ID
                else      -> return@forEach
            }
            schedulePrayer(name, timeMillis, notifId)
        }
    }

    private fun schedulePrayer(prayerName: String, triggerAtMillis: Long, notifId: Int) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("notif_id",    notifId)
        }
        val pending = PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
    }

    fun cancelAll() {
        listOf(FAJR_ID, DHUHR_ID, ASR_ID, MAGHRIB_ID, ISHA_ID).forEach { id ->
            val intent  = Intent(context, PrayerAlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }
}
