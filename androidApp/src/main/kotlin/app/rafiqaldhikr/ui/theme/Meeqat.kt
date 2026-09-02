package app.rafiqaldhikr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiqaldhikr.R

/* ═══════════════════════════════════════════════════════════════════
   طبقة الميقات — عنصر التوقيع

   أسماء الصلوات كلّها أسماء حالات ضوء: فجر · ضحى · ظهر · عصر · مغرب ·
   عشاء. فالوقت في هذا التطبيق **بنية**، لا زخرفة.

   كان التطبيق يقول «الوقت» بأربع طرق زخرفية متنافسة في آن واحد:
   خلفية سماء مرسومة، وأربع صور مساجد، ومشهد صلاة، ونجمة تتنفّس.
   أربع لهجات تقول الجملة نفسها، ولا واحدة منها بيانات.

   هذه الطبقة تقولها مرّة واحدة، ومن مواقيت الصلاة الحقيقية:

     ١) لون الورق ينحرف قليلاً نحو ضوء الوقت — الصفحة كلّها تعرف
        الساعة دون أن يشعر أحد أن هناك «خلفية».
     ٢) شريط الميقات: خيط رفيع أسفل الرأس في كل شاشة، يمثّل اليوم
        من الفجر إلى العشاء وعليه علامة بموضع اللحظة الحالية.
═══════════════════════════════════════════════════════════════════ */

/*  الاسمُ مرجعُ مورد لا نصّ — كان عربياً مكتوباً فلا يُترجَم شريطُ
 *  الميقات في أيّ شاشة. */
enum class MeeqatPhase(@androidx.annotation.StringRes val label: Int) {
    FAJR   (R.string.fajr),
    DUHA   (R.string.meeqat_duha),
    DHUHR  (R.string.dhuhr),
    ASR    (R.string.asr),
    MAGHRIB(R.string.maghrib),
    ISHA   (R.string.isha),
    LAYL   (R.string.meeqat_night),
}

/** علامة صلاة على شريط اليوم. [at] موضعها 0..1 من الفجر إلى العشاء. */
@Immutable
data class MeeqatMark(@androidx.annotation.StringRes val label: Int, val at: Float)

@Immutable
data class Meeqat(
    /** الطور الحالي — يحدّد لون الضوء. */
    val phase: MeeqatPhase,
    /** لون ضوء هذا الوقت. */
    val tint: Color,
    /** موضع اللحظة داخل اليوم 0..1 (الفجر = 0، العشاء = 1). */
    val dayProgress: Float,
    /** مواضع الصلوات الخمس على الشريط. */
    val marks: List<MeeqatMark>,
    /** هل اشتُقّ من مواقيت حقيقية؟ إن لا، الشريط لا يُرسم. */
    val resolved: Boolean,
)

/**
 * قيمة احتياطية قبل وصول المواقيت (أو عند تعذّر حسابها).
 * [resolved] = false فلا يُرسم الشريط ولا يُصبغ الورق — لا نخترع وقتاً.
 */
val UnresolvedMeeqat = Meeqat(
    phase       = MeeqatPhase.DUHA,
    tint        = Color.Transparent,
    dayProgress = 0f,
    marks       = emptyList(),
    resolved    = false,
)

val LocalMeeqat = staticCompositionLocalOf { UnresolvedMeeqat }

/* ═══════════════════════════════════════════════════════════════════
   الاشتقاق من المواقيت
═══════════════════════════════════════════════════════════════════ */

/**
 * يحدّد طور الميقات وموضع اللحظة من مواقيت اليوم.
 *
 * الشريط يمتدّ من الفجر إلى العشاء لأن ذلك هو «اليوم» في هذا التطبيق —
 * نافذة العمل. ما بعد العشاء ليل، فيُثبَّت طرف الشريط ويُطفأ نبضه.
 */
fun meeqatOf(times: PrayerTimesResult, nowMs: Long, palette: RafiqPalette): Meeqat {
    val fajr = times.fajr
    val isha = times.isha
    val span = (isha - fajr).coerceAtLeast(1L)

    val phase = when {
        nowMs <  fajr          -> MeeqatPhase.LAYL
        nowMs <  times.sunrise -> MeeqatPhase.FAJR
        nowMs <  times.dhuhr   -> MeeqatPhase.DUHA
        nowMs <  times.asr     -> MeeqatPhase.DHUHR
        nowMs <  times.maghrib -> MeeqatPhase.ASR
        nowMs <  times.isha    -> MeeqatPhase.MAGHRIB
        else                   -> MeeqatPhase.ISHA
    }

    // سلّم الضوء الثلاثي من اللوحة — لا ألوان جديدة هنا
    val tint = when (phase) {
        MeeqatPhase.FAJR, MeeqatPhase.DUHA      -> palette.goldLight
        MeeqatPhase.DHUHR                       -> palette.gold
        MeeqatPhase.ASR, MeeqatPhase.MAGHRIB    -> palette.lightDusk
        MeeqatPhase.ISHA, MeeqatPhase.LAYL      -> palette.lightNight
    }

    fun at(t: Long) = ((t - fajr).toFloat() / span).coerceIn(0f, 1f)

    return Meeqat(
        phase       = phase,
        tint        = tint,
        dayProgress = at(nowMs),
        marks = listOf(
            MeeqatMark(R.string.fajr,  at(times.fajr)),
            MeeqatMark(R.string.dhuhr,  at(times.dhuhr)),
            MeeqatMark(R.string.asr,  at(times.asr)),
            MeeqatMark(R.string.maghrib, at(times.maghrib)),
            MeeqatMark(R.string.isha, at(times.isha)),
        ),
        resolved = true,
    )
}

/* ═══════════════════════════════════════════════════════════════════
   انحراف الورق

   النسبة صغيرة عن قصد. الهدف ألّا يقول المستخدم «فيه خلفية ملوّنة»،
   بل أن يحسّ أن ضوء الغرفة تغيّر. أي قيمة أعلى تصير زخرفة، وهو
   بالضبط ما تستبدله هذه الطبقة.
═══════════════════════════════════════════════════════════════════ */

private const val PaperShift = 0.035f
private const val CardShift  = 0.018f

/** ينسخ اللوحة بورقٍ وبطاقةٍ منحرفَين نحو ضوء الوقت. */
fun RafiqPalette.litBy(meeqat: Meeqat): RafiqPalette =
    if (!meeqat.resolved) this
    else copy(
        bg   = lerp(bg,   meeqat.tint, PaperShift),
        card = lerp(card, meeqat.tint, CardShift),
    )
