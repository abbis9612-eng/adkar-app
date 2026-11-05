package app.rafiqaldhikr.ui.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.enterSpec
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized

/*
 * الجولةُ التعريفيّة — حجابٌ وثقبٌ وبطاقة
 * ═══════════════════════════════════════
 *
 * بطاقاتٌ تُعتم الشاشةَ وتشرح الأزرارَ واحداً واحداً. وهي ما طلبه صاحبُ
 * التطبيق صريحاً بعد أن عرضتُ عليه رأياً مخالفاً — فالقرارُ قرارُه،
 * والمكتوبُ هنا كيف نُفِّذت لا جدالٌ فيها.
 *
 * ═══ وما يجعلها جولةً لا شاشةً خامسة ═══
 *
 * **الثقبُ لا الصورة.** لا رسمَ يمثّل الزرَّ ولا لقطةَ شاشة: الحجابُ
 * يُثقَب فوق **الزرّ نفسِه** حيث هو. فما يحفظه المستخدمُ موضعٌ على شاشته
 * لا صورةٌ في كتيّب، ولو تغيّر الزرُّ غداً تغيّر الثقبُ معه بلا تحديثِ
 * سطرٍ واحد.
 *
 * **والحجابُ شفّافٌ لا أسود.** [RafiqPalette.scrim] يحمل شفافيّتَه في
 * قيمته، فيبقى ما تحته مرئيّاً خافتاً. فهو يعرف أين هو من التطبيق في كلّ
 * خطوة، ولا يحسّ أنّه خرج منه إلى شرحٍ عنه.
 *
 * ═══ وموضعُ البطاقة يُحسب ولا يُثبَّت ═══
 *
 * فوق الثقب إن كان في النصف الأسفل من الشاشة، وتحته إن كان في الأعلى —
 * فلا تحجب البطاقةُ ما جاءت تشرحه. وزرّا «التالي» و«تخطّي» في الأسفل
 * حيث يقع الإبهام.
 *
 * ═══ وما لم يُقَس لا يُشار إليه ═══
 *
 * المرساةُ تُقاس بـ[tourAnchor] حين تُركَّب. فإن كان ما تشير إليه الخطوةُ
 * غيرَ ظاهرٍ — لم يُركَّب، أو خارجَ الشاشة بعد تمرير — عُرضت البطاقةُ في
 * الوسط **بلا ثقب**. والبديلُ أن يُثقَب الحجابُ على مربّعٍ فارغٍ ويُقال
 * «هذا زرُّ كذا» وليس هناك زرّ.
 */

/** خطوةٌ واحدة: ما تشير إليه، وما تقوله عنه. */
data class TourStep(
    /** مفتاحُ المرساة — نفسُه الممرَّرُ إلى [tourAnchor]. */
    val anchor: String,
    @get:StringRes val title: Int,
    @get:StringRes val body: Int,
)

/**
 * مواضعُ المراسي المقيسة — مفتاحٌ ← مستطيلٌ بإحداثيّات النافذة.
 *
 * وهي `mutableStateMapOf` لا خريطةً عاديّة: الموضعُ يتغيّر بالتمرير
 * وبدوران الشاشة وبتغيّر مقياس الخطّ، والثقبُ يجب أن يتبعه.
 */
val LocalTourAnchors = compositionLocalOf<MutableMap<String, Rect>?> { null }

/**
 * يُسجّل موضعَ هذا العنصر لتُشير إليه الجولة.
 *
 * ولا يفعل شيئاً إن لم تكن الجولةُ مركَّبةً فوق الشاشة ([LocalTourAnchors]
 * فارغة) — فوضعُه على عنصرٍ لا تشير إليه جولةٌ لا يكلّف شيئاً.
 */
@Composable
fun Modifier.tourAnchor(key: String): Modifier {
    val anchors = LocalTourAnchors.current ?: return this
    return this.onGloballyPositioned { c ->
        val pos = c.positionInWindow()
        anchors[key] = Rect(pos.x, pos.y, pos.x + c.size.width, pos.y + c.size.height)
    }
}

/** هل أُنهيت الجولةُ (أو تُخطّيت) من قبل؟ */
private fun tourDone(ctx: Context): Boolean =
    ctx.getSharedPreferences(HINT_STORE, Context.MODE_PRIVATE)
        .getBoolean(TOUR_KEY, false)

private fun markTourDone(ctx: Context) {
    ctx.getSharedPreferences(HINT_STORE, Context.MODE_PRIVATE)
        .edit().putBoolean(TOUR_KEY, true).apply()
}

/*  رقمُ الإصدار في المفتاح: فإن زِيدت خطوةٌ غداً تُعرَض الجولةُ من جديد
 *  لمن رآها، ولا تُعرَض الخطوةُ الجديدةُ وحدَها بلا سياق. */
private const val TOUR_KEY = "tour_v1"

/** خطواتُ الجولة بترتيبها — من المحتوى إلى التنقّل. */
val RAFIQ_TOUR: List<TourStep> = listOf(
    TourStep("meeqat", R.string.tour_meeqat_title, R.string.tour_meeqat_body),
    TourStep("nav_home", R.string.tour_nav_home_title, R.string.tour_nav_home_body),
    TourStep("nav_quran", R.string.tour_nav_quran_title, R.string.tour_nav_quran_body),
    TourStep("nav_adhkar", R.string.tour_nav_adhkar_title, R.string.tour_nav_adhkar_body),
    TourStep("nav_dua", R.string.tour_nav_dua_title, R.string.tour_nav_dua_body),
    TourStep("nav_profile", R.string.tour_nav_profile_title, R.string.tour_nav_profile_body),
)

/**
 * يغلّف التطبيقَ كلَّه فيُركّب الجولةَ فوقه.
 *
 * و[content] هو التطبيق — يُعرَض دائماً، والجولةُ طبقةٌ فوقه لا بديلٌ
 * عنه: فالأزرارُ التي تُشرح موجودةٌ تحت الحجاب حقّاً، لا صورةٌ لها.
 *
 * @param active هل تُعرَض الآن (أوّلَ تشغيلٍ بعد الترحيب، أو بطلبٍ من
 *   الإعدادات)
 */
@Composable
fun TourHost(active: Boolean, content: @Composable () -> Unit) {
    val anchors = remember { mutableStateMapOf<String, Rect>() }
    val ctx = LocalContext.current
    var running by remember(active) { mutableStateOf(active && !tourDone(ctx)) }

    CompositionLocalProvider(LocalTourAnchors provides anchors) {
        Box(Modifier.fillMaxSize()) {
            content()
            if (running) {
                SpotlightTour(
                    steps = RAFIQ_TOUR,
                    anchors = anchors,
                    onFinish = { running = false; markTourDone(ctx) },
                )
            }
        }
    }
}

/** هل بقيت جولةٌ لم تُعرَض؟ تقرؤه الإعداداتُ لتعرف ماذا تكتب. */
fun tourPending(ctx: Context): Boolean = !tourDone(ctx)

/**
 * الحجابُ والثقبُ والبطاقة.
 *
 * مفصولةٌ عن [TourHost] لتُختبَر وتُعرَض بمراسٍ مُعطاةٍ مباشرة.
 */
@Composable
fun SpotlightTour(
    steps: List<TourStep>,
    anchors: Map<String, Rect>,
    onFinish: () -> Unit,
) {
    if (steps.isEmpty()) return
    val rc = LocalRafiqColors.current
    val density = LocalDensity.current
    var i by remember { mutableIntStateOf(0) }

    //  موضعُ الطبقة نفسِها: المراسي مقيسةٌ بإحداثيّات **النافذة**، وهذه
    //  الطبقةُ قد لا تبدأ من رأسها. فيُطرَح الفرقُ ولا يُفترَض صفراً.
    var origin by remember { mutableStateOf(Offset.Zero) }
    var layer by remember { mutableStateOf(Size.Zero) }

    val step = steps[i.coerceIn(0, steps.lastIndex)]
    val raw = anchors[step.anchor]
    val hole = raw
        ?.translate(-origin.x, -origin.y)
        ?.takeIf { it.width > 0f && it.height > 0f && it.top < layer.height && it.bottom > 0f }

    val pad = with(density) { 7.dp.toPx() }
    val radius = with(density) { 14.dp.toPx() }
    val ring = with(density) { 1.5.dp.toPx() }

    //  الثقبُ ينتقل من زرٍّ إلى زرّ بانسيابٍ لا بقفزة — فتتبعه العين
    //  وتعرف أنّ الكلامَ انتقل. ويرجع `snap()` عند تقليل الحركة.
    val animLeft by animateFloatAsState(hole?.left ?: 0f, enterSpec(), label = "holeL")
    val animTop by animateFloatAsState(hole?.top ?: 0f, enterSpec(), label = "holeT")
    val animRight by animateFloatAsState(hole?.right ?: 0f, enterSpec(), label = "holeR")
    val animBottom by animateFloatAsState(hole?.bottom ?: 0f, enterSpec(), label = "holeB")

    BackHandler(enabled = true) { onFinish() }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin = it.positionInWindow()
                layer = Size(it.size.width.toFloat(), it.size.height.toFloat())
            }
            /*  يبتلع كلَّ لمسةٍ لا تقع على زرَّيه.
             *
             *  وإلّا نفذت اللمسةُ إلى الزرّ الذي تحت الثقب فانتقلت الشاشةُ
             *  والجولةُ في منتصفها. */
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                //  طبقةٌ منفصلة: `BlendMode.Clear` يحتاج أن يمحو من هذه
                //  الطبقة وحدَها، لا من الشاشة كلِّها.
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            drawRect(rc.scrim)
            if (hole != null) {
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(animLeft - pad, animTop - pad),
                    size = Size(
                        (animRight - animLeft) + pad * 2,
                        (animBottom - animTop) + pad * 2,
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                    blendMode = BlendMode.Clear,
                )
                //  حلقةٌ ذهبيّةٌ حول الثقب: الذهبُ في هذا التطبيق لونُ
                //  الإشارة، وبلا حلقةٍ يذوب حدُّ الثقب في حافّة الزرّ.
                drawRoundRect(
                    color = rc.gold,
                    topLeft = Offset(animLeft - pad, animTop - pad),
                    size = Size(
                        (animRight - animLeft) + pad * 2,
                        (animBottom - animTop) + pad * 2,
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = ring),
                )
            }
        }

        /*  البطاقةُ فوق الثقب أو تحته — أيُّهما لا يحجبه.
         *
         *  والمعيارُ منتصفُ الشاشة: ما كان في نصفها الأسفل شُرح من فوقه،
         *  وما كان في الأعلى شُرح من تحته. والذي لا مرساةَ له يُشرح في
         *  الوسط. */
        val below = hole != null && hole.center.y < layer.height / 2f
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = when {
                hole == null -> Arrangement.Center
                below -> Arrangement.Bottom
                else -> Arrangement.Top
            },
        ) {
            TourCard(
                n = i + 1,
                total = steps.size,
                title = stringResource(step.title),
                body = stringResource(step.body),
                last = i == steps.lastIndex,
                onNext = { if (i == steps.lastIndex) onFinish() else i++ },
                onSkip = onFinish,
            )
        }
    }
}

@Composable
private fun TourCard(
    n: Int,
    total: Int,
    title: String,
    body: String,
    last: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    Column(
        Modifier
            .fillMaxWidth()
            //  زاويةٌ واحدةٌ أوسعُ من أخواتها — توقيعُ البطاقات في هذا
            //  التطبيق كلِّه.
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 26.dp))
            .background(rc.card)
            .padding(17.dp),
    ) {
        Text(
            stringResource(R.string.tour_step_of, n.localized(ar), total.localized(ar)),
            style = RafiqType.metaS,
            color = rc.gold,
        )
        Spacer(Modifier.height(7.dp))
        Text(title, style = RafiqType.titleM, color = rc.ink)
        Spacer(Modifier.height(6.dp))
        Text(body, style = RafiqType.bodyS, color = rc.inkMed)
        Spacer(Modifier.height(15.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            //  «تخطّي» حرفٌ خافتٌ لا زرٌّ مملوء: الخروجُ متاحٌ دائماً ولا
            //  يُزاحم الطريقَ الذي جاء من أجله.
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSkip)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .padding(horizontal = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.tour_skip),
                    style = RafiqType.label,
                    color = rc.inkLight,
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 20.dp))
                    .background(rc.emeraldFill)
                    .clickable(onClick = onNext)
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(if (last) R.string.tour_done else R.string.tour_next),
                    style = RafiqType.label,
                    color = rc.onEmeraldFill,
                )
            }
        }
    }
}
