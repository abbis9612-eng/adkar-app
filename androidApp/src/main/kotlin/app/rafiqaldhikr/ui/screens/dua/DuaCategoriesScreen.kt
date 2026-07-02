package app.rafiqaldhikr.ui.screens.dua

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.toEasternArabic
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*

/* Colors are now provided by LocalRafiqColors from RafiqPalette.kt */

/* ══════════════════════════════════════════════════════════════
   DUA CATEGORY DATA
══════════════════════════════════════════════════════════════ */

internal enum class DuaAccent { GOLD, INDIGO, PURPLE, GREEN, BROWN, ORANGE }

internal data class DuaCategoryDef(
    val name: String,
    val key: String,
    val iconType: Int, // 0=sun, 1=moon, 2=star, 3=leaf, 4=compass, 5=diamond
    val accent: DuaAccent,
)

// كل الأقسام المعروفة — تُعرض فقط التي لها أدعية فعلية في قاعدة البيانات
internal val KNOWN_DUA_CATEGORIES = listOf(
    DuaCategoryDef("أدعية الصباح",     "morning",    0, DuaAccent.GOLD),
    DuaCategoryDef("أدعية المساء",     "evening",    1, DuaAccent.INDIGO),
    DuaCategoryDef("أدعية النوم",      "sleep",      2, DuaAccent.PURPLE),
    DuaCategoryDef("أدعية من القرآن",  "quran",      2, DuaAccent.GREEN),
    DuaCategoryDef("أدعية الطعام",     "food",       3, DuaAccent.GREEN),
    DuaCategoryDef("أدعية السفر",      "travel",     4, DuaAccent.BROWN),
    DuaCategoryDef("أدعية الهمّ والقلق", "anxiety",   3, DuaAccent.INDIGO),
    DuaCategoryDef("أدعية المرض",      "sickness",   3, DuaAccent.PURPLE),
    DuaCategoryDef("أدعية جامعة",      "general",    2, DuaAccent.BROWN),
    DuaCategoryDef("دعاء الاستخارة",   "istikharah", 5, DuaAccent.GOLD),
)

internal fun duaCategoryLabel(key: String): String =
    KNOWN_DUA_CATEGORIES.firstOrNull { it.key == key }?.name ?: key

internal fun duaCountLabel(count: Long): String = when {
    count == 1L        -> "دعاء واحد"
    count == 2L        -> "دعاءان"
    count in 3..10     -> "${count.toEasternArabic()} أدعية"
    else               -> "${count.toEasternArabic()} دعاء"
}

@Composable
private fun DuaAccent.colors(): Pair<Color, Color> {
    val rc = LocalRafiqColors.current
    return when (this) {
        DuaAccent.GOLD   -> rc.accentGoldBg   to rc.accentGold
        DuaAccent.INDIGO -> rc.accentIndigoBg to rc.accentIndigo
        DuaAccent.PURPLE -> rc.accentPurpleBg to rc.accentPurple
        DuaAccent.GREEN  -> rc.emeraldPastel  to rc.emerald
        DuaAccent.BROWN  -> rc.accentBrownBg  to rc.accentBrown
        DuaAccent.ORANGE -> rc.accentOrangeBg to rc.accentOrange
    }
}

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS FOR CATEGORIES
══════════════════════════════════════════════════════════════ */

@Composable
private fun CategoryIcon(type: Int, color: Color, size: Dp = 32.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            0 -> { // Sun
                drawCircle(color.copy(alpha = 0.15f), w * 0.22f, Offset(cx, cy))
                drawCircle(color, w * 0.22f, Offset(cx, cy), style = st)
                for (i in 0 until 8) {
                    val a = (i * 45) * PI.toFloat() / 180f
                    drawLine(color, Offset(cx + w * 0.30f * cos(a), cy + w * 0.30f * sin(a)),
                        Offset(cx + w * 0.42f * cos(a), cy + w * 0.42f * sin(a)), w * 0.05f, StrokeCap.Round)
                }
            }
            1 -> { // Moon
                val moonPath = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - w * 0.28f, cy - w * 0.28f), w * 0.28f)) }
                val cutPath = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx + w * 0.08f - w * 0.22f, cy - w * 0.05f - w * 0.22f), w * 0.22f)) }
                val crescent = Path().apply { op(moonPath, cutPath, PathOperation.Difference) }
                drawPath(crescent, color.copy(alpha = 0.15f))
                drawPath(crescent, color, style = st)
                // Stars
                drawCircle(color, w * 0.03f, Offset(cx + w * 0.28f, cy - w * 0.22f))
                drawCircle(color, w * 0.025f, Offset(cx + w * 0.35f, cy - w * 0.08f))
            }
            2 -> { // 8-pointed star
                val star = Path().apply {
                    for (i in 0 until 16) {
                        val a = (i * 22.5f - 90f) * PI.toFloat() / 180f
                        val r = if (i % 2 == 0) w * 0.40f else w * 0.22f
                        val px = cx + r * cos(a); val py = cy + r * sin(a)
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }; close()
                }
                drawPath(star, color.copy(alpha = 0.12f))
                drawPath(star, color, style = st)
            }
            3 -> { // Leaf
                val leaf = Path().apply {
                    moveTo(cx, cy - w * 0.38f)
                    cubicTo(cx + w * 0.35f, cy - w * 0.20f, cx + w * 0.30f, cy + w * 0.15f, cx, cy + w * 0.38f)
                    cubicTo(cx - w * 0.30f, cy + w * 0.15f, cx - w * 0.35f, cy - w * 0.20f, cx, cy - w * 0.38f)
                    close()
                }
                drawPath(leaf, color.copy(alpha = 0.12f))
                drawPath(leaf, color, style = st)
                drawLine(color.copy(alpha = 0.5f), Offset(cx, cy - w * 0.20f), Offset(cx, cy + w * 0.25f), w * 0.04f, StrokeCap.Round)
            }
            4 -> { // Compass
                drawCircle(color.copy(alpha = 0.08f), w * 0.38f, Offset(cx, cy))
                drawCircle(color, w * 0.38f, Offset(cx, cy), style = Stroke(w * 0.04f))
                drawCircle(color, w * 0.06f, Offset(cx, cy))
                // N-S-E-W
                for (i in 0 until 4) {
                    val a = (i * 90 - 90) * PI.toFloat() / 180f
                    drawLine(color, Offset(cx + w * 0.12f * cos(a), cy + w * 0.12f * sin(a)),
                        Offset(cx + w * 0.34f * cos(a), cy + w * 0.34f * sin(a)), w * 0.05f, StrokeCap.Round)
                }
            }
            5 -> { // Diamond
                val diamond = Path().apply {
                    moveTo(cx, cy - w * 0.38f)
                    lineTo(cx + w * 0.28f, cy)
                    lineTo(cx, cy + w * 0.38f)
                    lineTo(cx - w * 0.28f, cy)
                    close()
                }
                drawPath(diamond, color.copy(alpha = 0.12f))
                drawPath(diamond, color, style = st)
                drawLine(color.copy(alpha = 0.4f), Offset(cx - w * 0.28f, cy), Offset(cx + w * 0.28f, cy), w * 0.04f)
            }
        }
    }
}

@Composable
private fun IconSettings(size: Dp = 17.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val hex = Path().apply {
            for (i in 0 until 6) {
                val a = (i * 60 - 30) * PI.toFloat() / 180f; val r = w * 0.42f
                val px = cx + r * cos(a); val py = cy + r * sin(a)
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }; close()
        }
        drawPath(hex, color, style = st)
        drawCircle(color, w * 0.13f, Offset(cx, cy), style = Stroke(w * 0.065f))
    }
}

/* ══════════════════════════════════════════════════════════════
   PILL BUTTON
══════════════════════════════════════════════════════════════ */

@Composable
private fun PillBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        modifier.size(40.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ══════════════════════════════════════════════════════════════
   CATEGORY GRID CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun DuaCategoryGridCard(
    def: DuaCategoryDef,
    count: Long,
    onClick: () -> Unit,
) {
    val (bgColor, iconColor) = def.accent.colors()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.5.dp, iconColor.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            CategoryIcon(type = def.iconType, color = iconColor)
            Spacer(Modifier.height(12.dp))
            Text(
                def.name,
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = LocalRafiqColors.current.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                duaCountLabel(count),
                fontSize = 12.sp, color = LocalRafiqColors.current.inkMed,
            )
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   FAVORITE DUA CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun FavoriteDuaCard(text: String, source: String, onToggle: () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text, fontSize = 16.sp, lineHeight = 28.sp, color = LocalRafiqColors.current.ink)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(source, fontSize = 11.sp, color = LocalRafiqColors.current.inkMed, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(rc.emeraldPastel).clickable(onClick = onToggle),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(14.dp)) {
                        val w = size.width; val h = size.height
                        val heart = Path().apply {
                            moveTo(w * 0.50f, h * 0.85f)
                            cubicTo(w * 0.10f, h * 0.55f, w * 0.10f, h * 0.20f, w * 0.50f, h * 0.35f)
                            cubicTo(w * 0.90f, h * 0.20f, w * 0.90f, h * 0.55f, w * 0.50f, h * 0.85f)
                            close()
                        }
                        drawPath(heart, rc.emerald)
                    }
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN DUA CATEGORIES SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun DuaCategoriesScreen(
    navController: NavHostController,
    viewModel: DuaViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // ═══ TOP BAR ═══
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PillBtn(onClick = { navController.navigate(RafiqRoute.EmotionalDua.route) }) {
                        IconSettings()
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الأدعية", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
                        Text("أدعية مأثورة من القرآن والسنة", fontSize = 11.sp, color = LocalRafiqColors.current.inkMed)
                    }
                }
            }

            // ═══ FAVORITES SECTION ═══
            if (state.favorites.isNotEmpty()) {
                item {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier.width(4.dp).height(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(rc.goldLight)
                        )
                        Text("المفضلة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                items(state.favorites) { dua ->
                    Box(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        FavoriteDuaCard(
                            text = dua.textAr,
                            source = dua.source,
                            onToggle = { viewModel.toggleFavorite(dua.id, dua.isFavorite) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .height(1.dp)
                            .background(rc.gold.copy(alpha = 0.10f))
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ═══ CATEGORIES HEADER ═══
            item {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier.width(4.dp).height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(rc.gold)
                    )
                    Text("التصنيفات", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark)
                }
                Spacer(Modifier.height(10.dp))
            }

            // ═══ 2×2 CATEGORY GRID — الأقسام التي لها أدعية فعلية فقط ═══
            val availableDefs = KNOWN_DUA_CATEGORIES.filter {
                (state.categoryCounts[it.key] ?: 0L) > 0L
            }
            val pairs = availableDefs.chunked(2)
            items(pairs.size) { idx ->
                val pair = pairs[idx]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    pair.forEach { def ->
                        Box(Modifier.weight(1f)) {
                            DuaCategoryGridCard(
                                def = def,
                                count = state.categoryCounts[def.key] ?: 0L,
                                onClick = {
                                    navController.navigate(RafiqRoute.DuaList.withCategory(def.key))
                                }
                            )
                        }
                    }
                    // Fill empty space if odd count
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ═══ DYNAMIC CATEGORIES FROM VM ═══
            val dynamicCats = state.categories.filter { cat ->
                KNOWN_DUA_CATEGORIES.none { it.key == cat }
            }
            if (dynamicCats.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)) }
                items(dynamicCats) { category ->
                    Box(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(rc.card)
                                .border(1.dp, rc.gold.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                                .clickable { navController.navigate(RafiqRoute.DuaList.withCategory(category)) }
                                .padding(16.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(rc.emeraldPastel),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CategoryIcon(type = 3, color = LocalRafiqColors.current.emerald, size = 24.dp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    duaCategoryLabel(category),
                                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                                    color = LocalRafiqColors.current.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                Canvas(Modifier.size(14.dp)) {
                                    val w = size.width; val h = size.height
                                    drawPath(Path().apply {
                                        moveTo(w * 0.65f, h * 0.15f)
                                        lineTo(w * 0.30f, h * 0.50f)
                                        lineTo(w * 0.65f, h * 0.85f)
                                    }, rc.inkLight, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
