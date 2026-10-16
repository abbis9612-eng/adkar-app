package app.rafiqaldhikr.ui.screens.adhkar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.*
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.theme.stillableFloat
import app.rafiqaldhikr.ui.components.MisbahaIcon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals

/* ═══════════════════════════════════════════════════════════════════
 *  شاشةُ أبواب الذِّكر — بلا توقيعٍ بقرار صاحب التطبيق
 *
 *  كان توقيعُها بَلاطاتٍ لونُها سماءُ وقت كلِّ باب (`DhikrTile`). فرآها
 *  صاحبُ التطبيق على هاتفه وقال إنّها غيرُ متناسقةٍ مع أيقونات التطبيق —
 *  وصدق: صفحةُ الدعاء المجاورةُ في الشريط تستعمل [CategoryBadge]، فصارت
 *  الصفحتان بلغتين. فرجعت الأبوابُ إلى [CategoryBadge] كما كانت،
 *  وحُذفت البَلاطات، ورُفع سقفُ `check_signatures.py` درجةً باستثناءٍ منه.
 *
 *  ═══ ولا «الآن» هنا ═══
 *  «اليوم» منبّهٌ يقول ما وقتُك بأوقات الصلاة الحقيقيّة، وهذه مكتبةٌ
 *  تقرأ منها ما شئت. وكانت هنا بطاقةُ «الآن» بساعة الهاتف فناقضت «اليوم»
 *  في اللحظة نفسِها — فحُذفت.
 * ═══════════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   DESIGN TOKENS
══════════════════════════════════════════════════════════════ */

/* General colors provided by LocalRafiqColors */

// ألوان الأقسام تأتي من RafiqPalette (فاتح/داكن) عبر AdhkarAccent

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION
══════════════════════════════════════════════════════════════ */

@Composable
private fun GeomDecoration(
    sizeDp: Dp = 160.dp,
    color: Color = LocalRafiqColors.current.gold.copy(alpha = 0.10f),
    spinDuration: Int = 90_000,
    modifier: Modifier = Modifier,
) {
    val rotation by stillableFloat(0f, 360f, spinDuration, LinearEasing, label = "geomRot")
    Canvas(modifier = modifier.size(sizeDp)) {
        val sz = this.size.width; val cx = sz / 2f; val cy = sz / 2f
        rotate(rotation, pivot = Offset(cx, cy)) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (i * 60 - 90) * PI.toFloat() / 180f; val r = sz * 0.43f
                    if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                    else lineTo(cx + r * cos(a), cy + r * sin(a))
                }; close()
            }
            drawPath(hex, color, style = Stroke(1.2f))
            drawCircle(color, sz * 0.44f, Offset(cx, cy), style = Stroke(0.7f))
            for (i in 0 until 6) {
                val a = (i * 60 - 90) * PI.toFloat() / 180f
                drawLine(color,
                    Offset(cx + sz * 0.27f * cos(a), cy + sz * 0.27f * sin(a)),
                    Offset(cx + sz * 0.43f * cos(a), cy + sz * 0.43f * sin(a)),
                    1.1f)
            }
        }
    }
}

/** مدخل المسبحة — أوّل ما في صفحة الذِّكر، لأنه أكثر ما يُفتح. */
@Composable
private fun MisbahaEntry(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .rafiqCard()
            .clickable { nav.navigate(RafiqRoute.Tasbeeh.route) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MisbahaIcon(28.dp, rc.gold)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.misbaha), style = RafiqType.titleM, color = rc.ink)
            Text(stringResource(R.string.adhkar_count_by_tap), style = RafiqType.bodyS, color = rc.inkMed)
        }
        RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
    }
}

/**
 * مدخلُ الأثر — جوابُ «من أين لكم هذا؟».
 *
 * وموضعُه هنا لا في الإعدادات: من يقرأ الأذكارَ هو من يسأل عن دليلها،
 * وشاشةُ إعداداتٍ لا يصلها إلّا من يبحث.
 */
/** مدخلُ الأربعين — كتابٌ يُقرأ على مُهلٍ لا قائمةٌ تُتصفَّح. */
@Composable
private fun ArbaeenEntry(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .rafiqCard()
            .clickable { nav.navigate(RafiqRoute.Arbaeen.route) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RafiqIcon(RIcon.Book, 28.dp, rc.gold)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.arbaeen_title), style = RafiqType.titleM, color = rc.ink)
            Text(
                stringResource(R.string.arbaeen_entry),
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
        }
        RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
    }
}

@Composable
private fun AtharEntry(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .rafiqCard()
            .clickable { nav.navigate(RafiqRoute.Athar.route) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RafiqIcon(RIcon.Document, 28.dp, rc.emerald)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.athar_title), style = RafiqType.titleM, color = rc.ink)
            Text(
                stringResource(R.string.athar_entry),
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
        }
        RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
    }
}

/* ══════════════════════════════════════════════════════════════
   CATEGORY DATA
══════════════════════════════════════════════════════════════ */

private data class AdhkarCatDef(
    val key: String,
    // معرّفات موارد لا نصوص: القائمة تُبنى في مستوى الملف خارج أي
    // @Composable، فلا سياق عندها لتحلّ stringResource.
    @StringRes val label: Int,
    @StringRes val description: Int,
)

/**
 * الأقسام المعروضة = الأقسام التي يبذرها DatabaseSeeder بالضبط، لا أكثر.
 *
 * كان هنا قسم "istighfar" لا يبذره الباذر إطلاقاً — فالضغط عليه يفتح
 * شاشة فارغة. وكان "misc" مبذوراً وغائباً عن هذه القائمة. وكانت هناك
 * قائمة ثانية منافسة داخل AdhkarCategoriesViewModel تصف الشيء نفسه
 * بتسميات مختلفة، وتعليقٌ يقول إنها «من قاعدة البيانات» وهي ليست كذلك.
 */
private val ADHKAR_CATS = listOf(
    AdhkarCatDef("morning", R.string.cat_morning, R.string.cat_morning_desc),
    AdhkarCatDef("evening", R.string.cat_evening, R.string.cat_evening_desc),
    AdhkarCatDef("sleep", R.string.cat_sleep, R.string.cat_sleep_desc),
    AdhkarCatDef("prayer", R.string.cat_prayer, R.string.cat_prayer_desc),
    AdhkarCatDef("misc", R.string.cat_misc, R.string.cat_misc_desc),
)

/* ══════════════════════════════════════════════════════════════
   MAIN SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun AdhkarCategoriesScreen(
    navController: NavHostController,
    vm: AdhkarCategoriesViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val ui by vm.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        // Background decoration
        GeomDecoration(
            sizeDp = 220.dp,
            color = LocalRafiqColors.current.gold.copy(alpha = 0.06f),
            spinDuration = 100_000,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            // جذر تبويب الآن — لا زرّ رجوع
            RafiqTopBar(
                title    = stringResource(R.string.dhikr_word),
                subtitle = stringResource(R.string.adhkar_subtitle),
            )

            // المسبحة كانت تبويباً منفصلاً، والفعل واحد — فجُمعت هنا.
            MisbahaEntry(navController)
            AtharEntry(navController)
            ArbaeenEntry(navController)

            Spacer(Modifier.height(18.dp))

            // ═══ HERO CARD ═══
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RafiqShape.card)
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            )
                        )
                )

                GeomDecoration(
                    sizeDp = 180.dp,
                    color = LocalRafiqColors.current.goldLight.copy(alpha = 0.12f),
                    spinDuration = 80_000,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-30).dp)
                        .graphicsLayer { alpha = 0.3f },
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    Text("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                        color = LocalRafiqColors.current.goldLight.copy(alpha = 0.7f), style = RafiqType.bodyS)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "أَلَا بِذِكْرِ اللَّهِ\nتَطْمَئِنُّ الْقُلُوبُ",
                        style = RafiqType.titleL,
                        color = Color.White,
                        lineHeight = 34.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.adhkar_ayah_source),
                        color = Color.White.copy(alpha = 0.5f), style = RafiqType.caption)
                }
            }

            Spacer(Modifier.height(16.dp))

            StreakRow(streak = ui.streak, week = ui.week)

            Spacer(Modifier.height(8.dp))

            // ═══ الأبوابُ الخمسة — متساويةٌ، لا يُرشَّح منها شيء ═══
            ADHKAR_CATS.forEach { cat ->
                DoorRow(
                    cat  = cat,
                    done = cat.key in ui.doneToday,
                    onClick = { navController.navigate(RafiqRoute.DhikrReading.withCategory(cat.key)) },
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   شريطُ المواظبة
══════════════════════════════════════════════════════════════ */

/**
 * السلسلةُ وسبعُ حبّات.
 *
 * ولا يُعرض رقمٌ لا سلسلةَ له: من لم يبدأ بعدُ يُدعى لا يُواجَه بصفرٍ
 * كبير.
 */
@Composable
private fun StreakRow(streak: Long, week: List<Boolean>) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .rafiqCard()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (streak > 0) {
            Text(
                streak.localized(ar),
                style = RafiqType.display,
                color = rc.emerald,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                if (streak > 0) {
                    stringResource(R.string.adhkar_streak_title, streak.localized(ar))
                } else {
                    stringResource(R.string.adhkar_streak_none)
                },
                style = RafiqType.label,
                fontWeight = FontWeight.Bold,
                color = rc.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(
                    if (streak > 0) R.string.adhkar_streak_hint else R.string.adhkar_streak_invite,
                ),
                style = RafiqType.meta,
                color = rc.inkLight,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            week.forEach { on ->
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (on) rc.emeraldFill else rc.divider)
                )
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   صفُّ باب
══════════════════════════════════════════════════════════════ */

/**
 * صفُّ البطاقة الأوّلُ نفسُه — شارةٌ واسمٌ ووصفٌ وسهم. والمُسجَّلُ اليومَ
 * علامةٌ على ركن شارته، والبابُ مفتوحٌ دائماً: الشحوبُ يقول «سُجِّل» لا
 * «لا تدخل».
 */
@Composable
private fun DoorRow(
    cat: AdhkarCatDef,
    done: Boolean,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .rafiqCard()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Box(Modifier.graphicsLayer { alpha = if (done) 0.5f else 1f }) {
                app.rafiqaldhikr.ui.components.CategoryBadge(
                    app.rafiqaldhikr.ui.components.adhkarCatDrawable(cat.key), 54.dp)
            }
            if (done) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-5).dp, y = (-5).dp)
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(rc.emeraldFill),
                    contentAlignment = Alignment.Center,
                ) {
                    RafiqIcon(RIcon.Check, 11.dp, rc.onEmeraldFill)
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                stringResource(cat.label),
                fontWeight = FontWeight.Bold,
                color = if (done) rc.inkLight else rc.ink,
                style = RafiqType.body,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                stringResource(cat.description),
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
        }

        RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
    }
}
