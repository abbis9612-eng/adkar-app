package app.rafiqaldhikr.ui.components

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalMeeqat
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LocalReducedMotion
import app.rafiqaldhikr.ui.theme.RafiqType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.json.JSONArray
import org.json.JSONObject

/* ═══════════════════════════════════════════════════════════════════
   اللوحُ الطائف — قبل الصلاة بعشر دقائق

   لوحٌ ينزل من أعلى الشاشة كلوحٍ معلَّقٍ بخيط، يتجاوز موضعَه قليلاً ثمّ
   يستقرّ — فوق أيّ شاشةٍ من التطبيق، وما تحته مُعتَم. وخارجَ التطبيق
   إشعارٌ بالكلام نفسِه (`PrayerAlarmReceiver`).

   وفيه ثلاثُ طبقاتٍ لا تُخلط:
     ١) نداءٌ أخويّ — «أخي الكريم» أو «أختي الكريمة» بإعدادٍ واحد.
     ٢) كلامُنا — من `strings.xml`، بخطّ الواجهة خفيفاً.
     ٣) المرويُّ بمصدره — من أصول التطبيق نفسِها لا من الكود: الآيةُ
        كاملةً من `quran_uthmani.json`، والحديثُ بمتنه كاملاً من
        `athar.json`. وبينهما فاصلٌ ذهبيٌّ يقول من أين.

   ولماذا داخل التطبيق لا فوق التطبيقات الأخرى: ذاك يحتاج إذنَ «الظهور
   فوق التطبيقات»، وهو إذنٌ يتشدّد فيه المتجر ويخافه الناس.
═══════════════════════════════════════════════════════════════════ */

private const val LEAD_MS = 10 * 60_000L
private const val SNOOZE_MS = 5 * 60_000L

/** مفاتيحُ اللوح — حالةُ عرضٍ لا بيانُ مستخدم، فهي في تفضيلاتٍ لا في القاعدة. */
object LawhPrefs {
    private fun sp(ctx: Context) = ctx.getSharedPreferences("lawh", Context.MODE_PRIVATE)
    fun enabled(ctx: Context) = sp(ctx).getBoolean("enabled", true)
    fun setEnabled(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("enabled", v).apply()
    fun sister(ctx: Context) = sp(ctx).getBoolean("sister", false)
    fun setSister(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("sister", v).apply()
    internal fun dismissed(ctx: Context, key: String) = sp(ctx).getBoolean("done_$key", false)
    internal fun dismiss(ctx: Context, key: String) = sp(ctx).edit().putBoolean("done_$key", true).apply()
    internal fun snoozedUntil(ctx: Context, key: String) = sp(ctx).getLong("snooze_$key", 0L)
    internal fun snooze(ctx: Context, key: String, until: Long) = sp(ctx).edit().putLong("snooze_$key", until).apply()
}

/** نصٌّ مرويٌّ بمصدره — يُعرض كما هو. */
data class LawhText(val text: String, val source: String, val quran: Boolean)

object LawhStore {
    @Volatile private var cached: List<LawhText>? = null

    /*  الآياتُ بمواضعها، والحديثُ بمعرّفه في `athar.json` — فلا يُنسخ نصٌّ
     *  دينيٌّ في الكود، ولا يُختصر: الآيةُ كاملة، والحديثُ بمتنه كاملاً. */
    private val AYAT = listOf(listOf(23 to 1, 23 to 2), listOf(2 to 45))
    private const val HADITH_ID = "5382"

    suspend fun load(ctx: Context): List<LawhText> = cached ?: withContext(Dispatchers.IO) {
        cached ?: runCatching {
            val out = mutableListOf<LawhText>()
            val q = JSONArray(ctx.assets.open("quran_uthmani.json").bufferedReader().use { it.readText() })
            val want = AYAT.flatten().toSet()
            val found = HashMap<Pair<Int, Int>, String>()
            for (i in 0 until q.length()) {
                val o = q.getJSONObject(i)
                val k = o.getInt("surah") to o.getInt("ayah")
                if (k in want) found[k] = o.getString("text_uthmani")
            }
            val surahs = mapOf(23 to R.string.lawh_surah_23, 2 to R.string.lawh_surah_2)
            AYAT.forEach { group ->
                if (group.all { it in found }) {
                    val s = group.first().first
                    val range = group.first().second.toString() +
                        if (group.size > 1) "–" + group.last().second else ""
                    out += LawhText(
                        group.joinToString(" ") { found.getValue(it) },
                        ctx.getString(surahs.getValue(s), range),
                        quran = true,
                    )
                }
            }
            val items = JSONObject(ctx.assets.open("athar.json").bufferedReader().use { it.readText() })
                .getJSONArray("items")
            for (i in 0 until items.length()) {
                val o = items.getJSONObject(i)
                if (o.optString("hadeeth_id") == HADITH_ID) {
                    val src = o.getString("source").lineSequence().first().trim()
                    out += LawhText(o.getString("text_ar"), "$src · ${o.getString("source_grade")}", quran = false)
                }
            }
            out.toList()
        }.getOrDefault(emptyList()).also { if (it.isNotEmpty()) cached = it }
    }
}

private val PRAYERS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
private val PRAYER_NAMES = mapOf(
    "fajr" to R.string.fajr, "dhuhr" to R.string.dhuhr, "asr" to R.string.asr,
    "maghrib" to R.string.maghrib, "isha" to R.string.isha,
)

/**
 * يركَّب فوق التطبيق كلِّه في `MainActivity`، ويظهر وحدَه قبل الصلاة
 * بعشر دقائق. [allowed] = false على شاشاتٍ لا يُقاطَع فيها (الترحيب).
 */
@Composable
fun LawhHost(allowed: Boolean) {
    val ctx = LocalContext.current
    val times = LocalMeeqat.current.times ?: return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(15_000) } }
    var texts by remember { mutableStateOf<List<LawhText>>(emptyList()) }
    LaunchedEffect(Unit) { texts = LawhStore.load(ctx) }
    var tick by remember { mutableIntStateOf(0) } // يُعيد القراءةَ بعد إخفاءٍ أو تأجيل

    val date = remember(now / 3_600_000L) {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val at = mapOf(
        "fajr" to times.fajr, "dhuhr" to times.dhuhr, "asr" to times.asr,
        "maghrib" to times.maghrib, "isha" to times.isha,
    )
    val due = PRAYERS.firstOrNull { p -> at.getValue(p).let { now in (it - LEAD_MS) until it } }
    val key = due?.let { "${date}_$it" }
    //  `now` في المفاتيح: بانقضاء التأجيل يعود اللوحُ مع أوّل نبضة
    val show = remember(key, tick, allowed, now) {
        allowed && key != null && LawhPrefs.enabled(ctx) && !LawhPrefs.dismissed(ctx, key) &&
            LawhPrefs.snoozedUntil(ctx, key) <= System.currentTimeMillis()
    }
    if (!show || due == null || key == null || texts.isEmpty()) return

    val text = texts[(date.dayOfYear + PRAYERS.indexOf(due)) % texts.size]
    Lawh(
        prayer = PRAYER_NAMES.getValue(due),
        minutes = ((at.getValue(due) - now) / 60_000L).toInt().coerceAtLeast(1),
        sister = LawhPrefs.sister(ctx),
        text = text,
        onGo = { LawhPrefs.dismiss(ctx, key); tick++ },
        onSnooze = { LawhPrefs.snooze(ctx, key, System.currentTimeMillis() + SNOOZE_MS); tick++ },
    )
}

@Composable
private fun Lawh(
    prayer: Int,
    minutes: Int,
    sister: Boolean,
    text: LawhText,
    onGo: () -> Unit,
    onSnooze: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val reduced = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val name = stringResource(prayer)

    /*  النزولُ والتأرجح — الحركةُ الواحدةُ المقصودةُ هنا.
     *
     *  نابضٌ بتخميدٍ منخفض: يتجاوز موضعَه ثمّ يرتدّ ويستقرّ، فيُحَسّ ثِقَلُ
     *  اللوح. وميلُه حول محوره الأفقيّ يتبع سرعتَه، كلوحٍ معلَّقٍ بخيط.
     *  ومع «تقليل الحركة» يظهر في موضعه بلا نزول. */
    val drop = remember { Animatable(if (reduced) 0f else -1.2f) }
    val dim = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (reduced) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope {
            launch { dim.animateTo(1f, spring(stiffness = Spring.StiffnessLow)) }
            drop.animateTo(0f, spring(dampingRatio = 0.48f, stiffness = 140f))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = dim.value }
            .background(rc.scrim.copy(alpha = 0.55f))
            // يحجب ما تحته: اللوحُ يُجاب أو يُؤجَّل، لا يُتخطّى بلمسةٍ عابرة
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        val shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 32.dp)
        Column(
            Modifier
                .padding(horizontal = 22.dp)
                .widthIn(max = 460.dp)
                .graphicsLayer {
                    translationY = drop.value * size.height
                    rotationX = (-drop.value * 14f).coerceIn(-6f, 17f)
                    cameraDistance = 16f * density
                }
                .shadow(24.dp, shape, ambientColor = rc.gold, spotColor = rc.ink)
                .clip(shape)
                .background(rc.card)
                .border(1.dp, rc.gold.copy(alpha = 0.7f), shape)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(rc.gold))
                Text(stringResource(R.string.app_name), style = RafiqType.metaS, color = rc.inkLight,
                    modifier = Modifier.weight(1f))
                Text(stringResource(R.string.lawh_before, name), style = RafiqType.metaS, color = rc.gold)
            }
            // ١ · النداء
            Text(stringResource(if (sister) R.string.lawh_call_f else R.string.lawh_call_m),
                style = RafiqType.titleL, color = rc.ink, modifier = Modifier.padding(top = 12.dp))
            // ٢ · كلامُنا
            Text(stringResource(R.string.lawh_plain, minutes, name),
                style = RafiqType.body.copy(fontWeight = FontWeight.Light), color = rc.inkMed,
                modifier = Modifier.padding(top = 4.dp))
            // فاصلٌ يقول من أين — فلا يُخلط كلامُنا بالمرويّ
            Row(Modifier.padding(top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(rc.gold.copy(alpha = 0.5f)))
                Text(stringResource(if (text.quran) R.string.lawh_from_quran else R.string.lawh_from_hadith),
                    style = RafiqType.metaS, color = rc.inkLight, modifier = Modifier.padding(horizontal = 8.dp))
                Box(Modifier.weight(1f).height(1.dp).background(rc.gold.copy(alpha = 0.5f)))
            }
            // ٣ · المرويّ بمصدره
            Text(text.text, style = if (text.quran) RafiqType.ayah.copy(fontSize = RafiqType.ayah.fontSize * 0.85f)
                else RafiqType.dhikr, color = rc.ink)
            Text(text.source, style = RafiqType.metaS, color = rc.gold, modifier = Modifier.padding(top = 8.dp))

            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.weight(1f).heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 18.dp))
                        .background(rc.emeraldFill)
                        .clickable(role = Role.Button, onClick = onGo),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.lawh_go), style = RafiqType.label, fontWeight = FontWeight.Bold,
                        color = rc.onEmeraldFill)
                }
                Box(
                    Modifier.heightIn(min = 48.dp).clip(CircleShape)
                        .border(1.dp, rc.divider, CircleShape)
                        .clickable(role = Role.Button, onClick = onSnooze)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.lawh_snooze), style = RafiqType.metaS, color = rc.inkMed)
                }
            }
        }
    }
}
