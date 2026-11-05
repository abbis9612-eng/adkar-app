package app.rafiqaldhikr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiq.domain.model.DuaItem
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LocalReducedMotion
import app.rafiqaldhikr.ui.theme.MeeqatPhase
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.progressSpec

/*
 * رسالتك اليوم
 * ════════════
 *
 * **دعاءٌ واحد، لا قائمةٌ من سبعةٍ وعشرين.**
 *
 * الفرقُ بين الاثنين ليس في العدد بل في الموقف: القائمةُ تسأل «ماذا
 * تريد؟» فتُحيل الاختيارَ إلى من جاء متعباً، والواحدُ يقول «هذا لك
 * اليوم» فيُقرأ. ومن فتح قائمةً من سبعةٍ وعشرين أغلقها غالباً بلا دعاء.
 *
 * والمظروفُ مغلَقٌ عن قصد: الفتحُ فعلٌ صغيرٌ يُحدث فرقاً — ما يُفتح
 * يُقرأ، وما يُعرض يُمرَّر عليه.
 *
 * ═══ والاختيارُ ليس عشوائيّاً ═══
 *
 * البذرةُ `اليومُ + الطور`. فيترتّب على ذلك أمران مقصودان:
 *
 *   • **ثابتٌ طولَ الطور** — من فتحها ثمّ عاد بعد دقيقةٍ وجد الدعاءَ
 *     نفسَه. ولو تبدّل بكلّ فتحةٍ لصار آلةَ تسليةٍ لا رسالة.
 *   • **يتبدّل بتبدّل الطور** — فدعاءُ الفجر غيرُ دعاء العشاء. وهذا ما
 *     لا يملكه تطبيقٌ لا يعرف أين صاحبُه من يومه.
 *
 * ═══ ولا نصَّ يُكتب هنا ═══
 *
 * الأدعيةُ تُقرأ من القاعدة بتخريجها ودرجتها كما هي. ولا تُصاغ ولا
 * تُختصر، ولا يُضاف إليها حرفٌ من كلامنا.
 */

/**
 * يختار دعاءَ اليوم اختياراً **ثابتاً** من [duas].
 *
 * دالّةٌ خالصةٌ لا حالةَ فيها — تُعطي الجوابَ نفسَه لليوم والطور نفسِهما
 * مهما تكرّر النداء، ويمكن اختبارُها بلا شاشة.
 *
 * @param epochDay رقمُ اليوم منذ المبدأ
 * @param phase    طورُ اليوم الحاضر
 */
fun pickRisala(duas: List<DuaItem>, epochDay: Long, phase: MeeqatPhase): DuaItem? {
    if (duas.isEmpty()) return null
    /*  عددٌ أوّليٌّ في معامل الطور.
     *
     *  لو ضُرب الطورُ في واحدٍ لتداخلت الأطوارُ بين الأيّام: طورُ العشاء
     *  اليومَ يُعطي ما أعطاه طورُ العصر غداً. والسبعةُ أوّليّةٌ لا تقسم
     *  سبعةً وعشرين، فتتفرّق النتائجُ ولا تدور على نفسها. */
    val seed = epochDay * 7 + phase.ordinal
    //  `floorMod`: اليومُ قد يكون سالباً قبل المبدأ، و`%` تُعطي سالباً
    //  فيسقط الفهرسُ خارج القائمة.
    return duas[Math.floorMod(seed, duas.size)]
}

/**
 * بطاقةُ المظروف — مغلقةٌ حتى تُضغط.
 *
 * @param opened هل فُتحت اليوم · [onOpen] يُستدعى عند أوّل فتحة
 */
@Composable
fun RisalaCard(
    dua: DuaItem?,
    opened: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    if (dua == null) return

    //  الغطاءُ ينطبق عند الفتح: ١ مغلقٌ · ٠ منبسط
    val flap by animateFloatAsState(
        targetValue = if (opened) 0f else 1f,
        animationSpec = progressSpec(if (LocalReducedMotion.current) 0 else 520),
        label = "risalaFlap",
    )

    Column(
        modifier
            .fillMaxWidth()
            .clip(RisalaShape)
            .background(rc.card)
            .border(1.dp, rc.cardBorder, RisalaShape)
            .then(if (opened) Modifier else Modifier.clickable(onClick = onOpen))
            .padding(bottom = 18.dp),
    ) {
        //  غطاءُ المظروف — مثلّثٌ يُرسم بالخطّ لا بصورة، فيتبع لونَ
        //  السمة ولا يحمل كيلوبايتاً في الحزمة.
        Box(
            Modifier
                .fillMaxWidth()
                .height(66.dp),
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(66.dp)) {
                val w = size.width
                val h = size.height * flap
                if (h <= 0.5f) return@Canvas
                val p = Path().apply {
                    moveTo(0f, 0f); lineTo(w / 2f, h); lineTo(w, 0f)
                }
                drawPath(p, rc.bg)
                drawPath(p, rc.cardBorder, style = Stroke(width = 1.5f))
            }
        }

        Column(Modifier.padding(horizontal = 18.dp)) {
            Text(
                stringResource(R.string.risala_title),
                style = RafiqType.titleL,
                fontSize = 20.sp,
                color = rc.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))

            AnimatedVisibility(visible = !opened) {
                Column {
                    Text(
                        stringResource(R.string.risala_sub),
                        style = RafiqType.bodyS,
                        fontSize = 13.5.sp,
                        color = rc.inkMed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(13.dp, 13.dp, 22.dp, 13.dp))
                            .background(rc.emeraldFill)
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                    ) {
                        Text(
                            stringResource(R.string.risala_open),
                            style = RafiqType.titleM,
                            fontSize = 15.sp,
                            color = rc.onEmeraldFill,
                        )
                    }
                }
            }

            AnimatedVisibility(visible = opened, enter = fadeIn() + expandVertically()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    /*  النصُّ المرويُّ بخطّ الأميري — وهو في هذا التطبيق
                     *  حرفُ الوحي والأثر وحدَه. وما فوقه («رسالتك اليوم»)
                     *  كلامُنا، فبخطّ الواجهة. */
                    Text(
                        dua.textAr,
                        style = RafiqType.ayah,
                        lineHeight = 50.7.sp,
                        color = rc.ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.width(64.dp).height(1.dp).background(rc.divider))
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(dua.source, fontSize = 12.sp, color = rc.gold, maxLines = 2)
                        if (dua.sourceGrade.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                dua.sourceGrade,
                                style = RafiqType.metaS,
                                color = rc.inkMed,
                                maxLines = 1,
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .border(
                                        1.dp, rc.divider,
                                        androidx.compose.foundation.shape.CircleShape,
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** نفسُ توقيع أسطح التطبيق — زاويةٌ عريضةٌ واحدةٌ في bottomStart. */
private val RisalaShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 32.dp,
)
