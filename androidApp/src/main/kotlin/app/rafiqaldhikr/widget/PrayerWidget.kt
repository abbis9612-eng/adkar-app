package app.rafiqaldhikr.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult
import org.koin.core.context.GlobalContext

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db     = GlobalContext.get().get<RafiqDatabase>()
        val prefs  = db.userPrefsQueries.get().executeAsOne()
        val calc   = PrayerTimeCalculator()
        val lat = if (prefs.last_known_lat != 0.0) prefs.last_known_lat else 35.5558
        val lng = if (prefs.last_known_lng != 0.0) prefs.last_known_lng else 45.4436
        val method = prefs.prayer_method
        
        val times  = runCatching {
            calc.calculate(lat, lng, method,
                elevation = prefs.elevation,
                madhab = prefs.madhab,
                fajrOffset = prefs.fajr_offset.toInt(),
                dhuhrOffset = prefs.dhuhr_offset.toInt(),
                asrOffset = prefs.asr_offset.toInt(),
                maghribOffset = prefs.maghrib_offset.toInt(),
                ishaOffset = prefs.isha_offset.toInt()
            )
        }.getOrNull()
        
        var next = findNextPrayerWithinDay(times, System.currentTimeMillis())
        
        // If all today's prayers have passed, show tomorrow's Fajr
        if (next == null && times != null) {
             val tomorrowTimes = runCatching {
                 calc.calculateForTomorrow(lat, lng, method,
                     elevation = prefs.elevation,
                     madhab = prefs.madhab,
                     fajrOffset = prefs.fajr_offset.toInt(),
                     dhuhrOffset = prefs.dhuhr_offset.toInt(),
                     asrOffset = prefs.asr_offset.toInt(),
                     maghribOffset = prefs.maghrib_offset.toInt(),
                     ishaOffset = prefs.isha_offset.toInt()
                 )
             }.getOrNull()
             if (tomorrowTimes != null) {
                 val fmt = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar"))
                 next = "الفجر" to fmt.format(java.util.Date(tomorrowTimes.fajr))
             }
        }

        provideContent {
            PrayerWidgetContent(
                prayerName = next?.first ?: "—",
                prayerTime = next?.second ?: "—"
            )
        }
    }

    private fun findNextPrayerWithinDay(times: PrayerTimesResult?, now: Long): Pair<String, String>? {
        if (times == null) return null
        val fmt     = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar"))
        val ordered = listOf(
            "الفجر" to times.fajr, "الظهر" to times.dhuhr,
            "العصر" to times.asr,  "المغرب" to times.maghrib, "العشاء" to times.isha
        )
        val next = ordered.firstOrNull { it.second > now }
        return next?.let { it.first to fmt.format(java.util.Date(it.second)) }
    }
}

@Composable
private fun PrayerWidgetContent(prayerName: String, prayerTime: String) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(24.dp)
                .padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = prayerName, style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = prayerTime, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.primary))
        }
    }
}

class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = PrayerWidget()
}
