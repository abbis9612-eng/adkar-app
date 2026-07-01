package app.rafiqaldhikr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.PrayerTimeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val db         = GlobalContext.get().get<RafiqDatabase>()
                val prefs      = db.userPrefsQueries.get().executeAsOne()
                val calculator = PrayerTimeCalculator()
                val times      = calculator.calculate(
                    lat    = prefs.last_known_lat.takeIf { it != 0.0 } ?: 35.5558,
                    lng    = prefs.last_known_lng.takeIf { it != 0.0 } ?: 45.4436,
                    method = prefs.prayer_method,
                    elevation = prefs.elevation,
                    madhab = prefs.madhab,
                    fajrOffset = prefs.fajr_offset.toInt(),
                    dhuhrOffset = prefs.dhuhr_offset.toInt(),
                    asrOffset = prefs.asr_offset.toInt(),
                    maghribOffset = prefs.maghrib_offset.toInt(),
                    ishaOffset = prefs.isha_offset.toInt()
                )
                PrayerAlarmManager(context).scheduleAllForToday(
                    mapOf(
                        "fajr"    to times.fajr,
                        "dhuhr"   to times.dhuhr,
                        "asr"     to times.asr,
                        "maghrib" to times.maghrib,
                        "isha"    to times.isha
                    )
                )
            } catch (_: Exception) {
                // Crashlytics log
            } finally {
                pendingResult.finish()
            }
        }
    }
}
