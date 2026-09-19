package app.rafiqaldhikr.ui.hero

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.MeeqatPhase
import app.rafiqaldhikr.ui.theme.RafiqType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/* ══════════════════════════════════════════════════════════════
   النداء — كلامُنا على السماء، والنصُّ المرويُّ على الورق

   الشاشةُ الأولى كانت تقول «ابدأ يومَك»: فعلٌ صحيحٌ وجافّ. وطلبُ صاحبِ
   التطبيق أن تقول له «ابدأْ، **رعاك الله**» — أي أن تخاطبه وتدعو له،
   ثمّ تُريه نصّاً يحرّكه، ثمّ تفتح له باباً واحداً.

   ═══ القاعدةُ التي تحكم هذا الملفّ كلَّه: صوتان لا يختلطان ═══

   وهي قاعدةٌ شرعيّةٌ قبل أن تكون بصريّة، ولها هنا **ثلاثُ طبقاتٍ من
   الفصل** لا واحدة:

     ١) **السطح**: كلامُنا على السماء، والنصُّ المرويُّ على الورق.
     ٢) **الحرف**: كلامُنا بـ`UiFamily`، والنصُّ بـ`AmiriFamily`
        (`RafiqType.ayah`). ولا تُكتب كلمةٌ من كلامنا بخطّ النصّ أبداً.
     ٣) **اللون**: الذهبُ محجوزٌ للإسناد وحدَه في هذه الرقعة — فحيثما
        رأيتَ ذهباً عرفتَ أنّ ما فوقه مَرويٌّ ومخرَّج.

   ولهذا سببٌ مُعايَن: وصلنا نموذجٌ خارجيٌّ متقَنٌ مكتوبٌ بعنايةٍ، وفيه
   عنوانٌ كبيرٌ نصُّه «بذِكرٍ يطمئنُّ به قلبُك» — وهو **معنى الرعد ٢٨
   بصياغةٍ محرَّرة**، والآيةُ نفسُها مكتوبةٌ بلفظها في الشاشة عينِها.
   فحملت الشاشةُ الواحدةُ المعنى القرآنيَّ مرّتين: مرّةً بلفظه ومرّةً
   بكلامنا. وكاتبُه صنّفه «صياغةً تحريريّة» في ملفّ بحثٍ — والقارئُ لا
   يقرأ ملفّات البحث. فالفصلُ يجب أن يكون **في الشاشة** لا في الوثائق.

   ═══ ولا نصَّ دينيٌّ في الكود ═══

   النصوصُ في `assets/nida.json` بمصدرها ودرجتها، يفحصها
   `check_religious_sources.py`. وما في هذا الملفّ من عربيّةٍ تعليقاتٌ
   ونداءاتٌ من `strings.xml` — أي كلامُنا وحدَه، وهو مترجَم.
══════════════════════════════════════════════════════════════ */

/**
 * موعظةُ الطور — نصٌّ واحدٌ بمصدره.
 *
 * @param partial مقطعٌ من آيةٍ لا آيةٌ كاملة، فيُوسَم «من الآية» — لا
 *        يُترك القارئُ يظنّ أنّ ما رآه هو الآيةُ كلُّها.
 */
data class Nida(
    val phase: String,
    val text: String,
    val source: String,
    val grade: String,
    val link: String,
    val partial: Boolean,
    /** `quran` أو `hadith` أو `athar` — يُترجَم إلى وسمٍ فوق النصّ. */
    val kind: String,
)

/** وسمُ النوع — يُقرأ **قبل** النصّ فيعرف القارئُ ما بين يديه. */
@androidx.annotation.StringRes
fun kindLabel(kind: String): Int = when (kind) {
    "quran" -> R.string.nida_kind_quran
    "athar" -> R.string.nida_kind_athar
    else -> R.string.nida_kind_hadith
}

object NidaStore {
    @Volatile private var cached: List<Nida>? = null

    /*  يُقرأ مرّةً واحدةً خارج الخيط الرئيسيّ — ملفٌّ صغير، لكنّ فتح
     *  الأصول وتحليل JSON داخل التأليف يوقف أوّلَ إطار. */
    suspend fun load(ctx: Context): List<Nida> = cached ?: withContext(Dispatchers.IO) {
        cached ?: runCatching {
            val raw = ctx.assets.open("nida.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Nida(
                    phase = o.getString("phase"),
                    text = o.getString("text_ar"),
                    source = o.getString("source"),
                    grade = o.getString("source_grade"),
                    link = o.optString("link"),
                    partial = o.optBoolean("partial"),
                    kind = o.optString("kind", "hadith"),
                )
            }
        }.getOrDefault(emptyList()).also { if (it.isNotEmpty()) cached = it }
    }

    /** طورُ الميقات ← مفتاحُ النصّ. والضحى والفجرُ بابان مختلفان. */
    fun keyOf(phase: MeeqatPhase): String = when (phase) {
        MeeqatPhase.FAJR -> "fajr"
        MeeqatPhase.DUHA -> "duha"
        MeeqatPhase.DHUHR -> "dhuhr"
        MeeqatPhase.ASR -> "asr"
        MeeqatPhase.MAGHRIB -> "maghrib"
        MeeqatPhase.ISHA -> "isha"
        MeeqatPhase.LAYL -> "layl"
    }
}

/** نداءُ الطور ودعاؤه — **كلامُنا**، من `strings.xml` فيُترجَم. */
@Composable
fun nidaCall(phase: MeeqatPhase): Pair<Int, Int> = when (phase) {
    MeeqatPhase.FAJR -> R.string.nida_fajr to R.string.nida_fajr_sub
    MeeqatPhase.DUHA -> R.string.nida_duha to R.string.nida_duha_sub
    MeeqatPhase.DHUHR -> R.string.nida_dhuhr to R.string.nida_dhuhr_sub
    MeeqatPhase.ASR -> R.string.nida_asr to R.string.nida_asr_sub
    MeeqatPhase.MAGHRIB -> R.string.nida_maghrib to R.string.nida_maghrib_sub
    MeeqatPhase.ISHA -> R.string.nida_isha to R.string.nida_isha_sub
    MeeqatPhase.LAYL -> R.string.nida_layl to R.string.nida_layl_sub
}

/** خيطٌ رفيع — للوسم فوق النصّ وللفصل عن إسناده. */
@Composable
private fun Rule(color: Color, w: androidx.compose.ui.unit.Dp = 30.dp) {
    androidx.compose.foundation.layout.Box(
        Modifier.width(w).height(1.dp).background(color),
    )
}

/**
 * الموعظةُ على الورق: النصُّ بخطّ المصحف، وتحته إسنادُه ذهبيّاً.
 *
 * والإسنادُ **يُلمَس فيفتح المصدر** — أخذتُها عن النموذج الخارجيّ وهي
 * أحسنُ ما فيه: من شكَّ في تخريجٍ راجعه بنفسه، فلا يُطلب منه أن يثق
 * بنا. وتُفتح خارج التطبيق فتحتاج شبكةً — والتطبيقُ نفسُه لا يحتاجها.
 */
@Composable
fun NidaBlock(nida: Nida, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    val ctx = LocalContext.current
    Column(
        modifier.fillMaxWidth().padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        /*  «من القرآن الكريم» / «من حديث النبي ﷺ» / «من قول صحابيّ»
         *  — بين خطّين رفيعين فوق النصّ.
         *
         *  أُخذ عن نموذجٍ خارجيّ، وهو أحسنُ ما فيه: القارئُ يعرف نوعَ ما
         *  يقرأ **قبل** أن يقرأه، فلا يلتبس عليه قرآنٌ بحديثٍ بأثر. */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Rule(rc.divider)
            Text(
                stringResource(kindLabel(nida.kind)),
                fontSize = 11.5.sp,
                color = rc.inkMed,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Rule(rc.divider)
        }
        Spacer(Modifier.height(13.dp))
        Text(
            nida.text,
            style = RafiqType.ayah,
            color = rc.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        //  خيطٌ قصيرٌ يفصل النصَّ عن إسناده — لا إطارٌ حول النصّ
        Rule(rc.divider, 64.dp)
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .then(
                    if (nida.link.isNotBlank()) {
                        Modifier.clickable {
                            runCatching {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(nida.link))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        }
                    } else {
                        Modifier
                    },
                )
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                //  «من الآية» حين يكون مقطعاً — لا يُوهَم أنّه الآيةُ كلُّها
                if (nida.partial) {
                    stringResource(R.string.nida_partial, nida.source)
                } else {
                    nida.source
                },
                fontSize = 12.sp,
                color = rc.gold,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.width(8.dp))
            Text("·", fontSize = 12.sp, color = rc.inkLight)
            Spacer(Modifier.width(8.dp))
            Text(nida.grade, fontSize = 11.sp, color = rc.inkMed, maxLines = 1)
        }
    }
}
