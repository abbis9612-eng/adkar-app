package app.rafiqaldhikr.ui.hero

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.formatClock

/* ══════════════════════════════════════════════════════════════
   سطرُ الميقات — الاسمُ والساعةُ وكم بقي

   هنا كان قوسُ شمسٍ: قرصٌ يمشي على مسارٍ إهليلجيّ وعليه ستُّ علاماتٍ
   في مواضع الشمس، والظهرُ في قمّته لأنّه الزوال. وكان **صحيحاً في
   حسابه** — وقد قِيس: الظهرُ على القمّة بفارق صفرِ بكسل، والمغربُ على
   خطّ الأفق تماماً.

   ومع ذلك سقط، والسببُ أهمُّ من الحساب: **لم يُفهَم بلا شرح**. نظر
   صاحبُ التطبيق إليه فسأل «ما هذا؟» — والشاشةُ الأولى لا تُشرَح، تُقرأ.
   وبلا موقعٍ كان يصير بيضةً رماديّةً فارغة.

   فبقي ما كان القوسُ يخدمه أصلاً، ولا شيءَ غيره: **متى الصلاةُ
   القادمةُ وكم بقي لها**. سطرٌ يُقرأ في نصف ثانيةٍ ويكبر مع خطّ
   صاحبه ويقرؤه التدقيقُ الصوتيّ.
══════════════════════════════════════════════════════════════ */

/**
 * @param times مواقيتُ اليوم — وبلا مواقيتَ لا يُستدعى أصلاً.
 * @param left  ما بقي بصيغةٍ مقروءة، من مُنسِّق الشاشة نفسِه.
 */
@Composable
fun MeeqatLine(
    times: PrayerTimesResult,
    nowMs: Long,
    left: String?,
    ink: Color,
    ar: Boolean,
    modifier: Modifier = Modifier,
) {
    val marks = listOf(
        R.string.fajr to times.fajr,
        R.string.sunrise to times.sunrise,
        R.string.dhuhr to times.dhuhr,
        R.string.asr to times.asr,
        R.string.maghrib to times.maghrib,
        R.string.isha to times.isha,
    )
    //  أوّلُ ما لم يأتِ بعد، وإلّا فجرُ الغد
    val next = marks.firstOrNull { it.second > nowMs } ?: marks.first()
    val name = stringResource(next.first)

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            //  الاسمُ وحدَه ثقيل: «التالي» كلمةُ ربطٍ لا خبر
            emphasize(stringResource(R.string.meeqat_next, name), name),
            style = RafiqType.body, color = ink, maxLines = 1,
        )
        Text(
            buildString {
                append(formatClock(next.second, ar))
                if (left != null) append(" · ").append(stringResource(R.string.meeqat_in, left))
            },
            style = RafiqType.bodyS, color = ink.copy(alpha = 0.78f), maxLines = 1,
        )
    }
}

/** يُثقّل [part] داخل [whole] — بالبحث لا بالقصّ، فيصحّ في اللغتين. */
private fun emphasize(whole: String, part: String): AnnotatedString {
    val i = whole.indexOf(part)
    if (i < 0) return AnnotatedString(whole)
    return buildAnnotatedString {
        append(whole)
        addStyle(SpanStyle(fontWeight = FontWeight.SemiBold), i, i + part.length)
    }
}
