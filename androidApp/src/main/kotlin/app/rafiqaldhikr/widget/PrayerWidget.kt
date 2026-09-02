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
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.util.coordsOrNull
import org.koin.core.context.GlobalContext

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        /*  `executeAsOne()` كان يرمي حين لا يوجد صفٌّ بعد.
         *
         *  والصفُّ يُنشَأ في `DatabaseSeeder` على كوروتين خلفيّ عند إقلاع
         *  التطبيق — فمن أضاف الودجتَ قبل أن يفتح التطبيقَ أصلاً، أو في
         *  أوّل ثوانٍ بعد التثبيت، رأى «تعذّر تحميل الأداة» بدل الودجت.  */
        val db     = GlobalContext.get().get<RafiqDatabase>()
        val prefs  = db.userPrefsQueries.get().executeAsOneOrNull()
        if (prefs == null) {
            provideContent {
                PrayerWidgetContent(
                    prayerName = context.getString(R.string.widget_needs_location),
                    prayerTime = "—",
                )
            }
            return
        }
        val calc   = PrayerTimeCalculator()
        val method = prefs.prayer_method

        // بلا موقع محفوظ يعرض الودجت طلباً صريحاً، لا وقتَ مدينةٍ أخرى.
        val here = coordsOrNull(prefs.last_known_lat, prefs.last_known_lng)
        if (here == null) {
            provideContent {
                PrayerWidgetContent(
                    prayerName = context.getString(R.string.widget_needs_location),
                    prayerTime = "—",
                )
            }
            return
        }
        val lat = here.lat
        val lng = here.lng

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
        
        var next = findNextPrayerWithinDay(context, times, System.currentTimeMillis(), prefs.numerals)
        
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
                 next = context.getString(R.string.fajr) to
                     formatWidgetTime(context, tomorrowTimes.fajr, prefs.numerals)
             }
        }

        provideContent {
            PrayerWidgetContent(
                prayerName = next?.first ?: "—",
                prayerTime = next?.second ?: "—"
            )
        }
    }

    /*  أسماءُ الصلوات من الموارد لا مكتوبةً في الكود، والوقتُ بلغة أرقامٍ
     *  يختارها المستخدم. وكان `Locale("ar")` مثبَّتاً — فيخالف الودجتُ
     *  التطبيقَ نفسَه في شكل الأرقام، ويتجاهل إعدادَ لغة الأرقام كلَّه. */
    private fun findNextPrayerWithinDay(
        context: Context,
        times: PrayerTimesResult?,
        now: Long,
        numerals: String,
    ): Pair<String, String>? {
        if (times == null) return null
        val ordered = listOf(
            context.getString(R.string.fajr)    to times.fajr,
            context.getString(R.string.dhuhr)   to times.dhuhr,
            context.getString(R.string.asr)     to times.asr,
            context.getString(R.string.maghrib) to times.maghrib,
            context.getString(R.string.isha)    to times.isha,
        )
        val next = ordered.firstOrNull { it.second > now }
        return next?.let { it.first to formatWidgetTime(context, it.second, numerals) }
    }

    /** نفسُ صيغة شاشة المواقيت: Locale.US ثمّ تحويلُ الأرقام حسب الإعداد. */
    private fun formatWidgetTime(context: Context, millis: Long, numerals: String): String {
        val raw = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            .format(java.util.Date(millis))
        return raw.localizedDigits(numerals == "arabic")
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
