package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*

/* Colors are now provided by LocalRafiqColors from RafiqPalette.kt */

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

@Composable
private fun IconSearch(size: Dp = 17.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val st = Stroke(w * 0.08f, cap = StrokeCap.Round)
        drawCircle(color, w * 0.30f, Offset(w * 0.42f, w * 0.42f), style = st)
        drawLine(color, Offset(w * 0.62f, w * 0.62f), Offset(w * 0.88f, w * 0.88f), w * 0.08f, StrokeCap.Round)
    }
}

@Composable
private fun IconBookmark(size: Dp = 17.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val bm = Path().apply {
            moveTo(w * 0.22f, h * 0.12f)
            lineTo(w * 0.78f, h * 0.12f)
            lineTo(w * 0.78f, h * 0.88f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.22f, h * 0.88f)
            close()
        }
        drawPath(bm, color.copy(alpha = 0.10f))
        drawPath(bm, color, style = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun IconPlay(size: Dp = 22.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val tri = Path().apply {
            moveTo(w * 0.28f, h * 0.15f)
            lineTo(w * 0.82f, h * 0.50f)
            lineTo(w * 0.28f, h * 0.85f)
            close()
        }
        drawPath(tri, color)
    }
}

@Composable
private fun IconPrev(size: Dp = 16.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val tri = Path().apply {
            moveTo(w * 0.65f, h * 0.20f)
            lineTo(w * 0.25f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.80f)
            close()
        }
        drawPath(tri, color)
        drawLine(color, Offset(w * 0.22f, h * 0.20f), Offset(w * 0.22f, h * 0.80f), w * 0.07f)
    }
}

@Composable
private fun IconNext(size: Dp = 16.dp, color: Color = Color.White) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val tri = Path().apply {
            moveTo(w * 0.35f, h * 0.20f)
            lineTo(w * 0.75f, h * 0.50f)
            lineTo(w * 0.35f, h * 0.80f)
            close()
        }
        drawPath(tri, color)
        drawLine(color, Offset(w * 0.78f, h * 0.20f), Offset(w * 0.78f, h * 0.80f), w * 0.07f)
    }
}

@Composable
private fun IconArrowLeft(size: Dp = 14.dp, color: Color = LocalRafiqColors.current.inkLight) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val arrow = Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.85f)
        }
        drawPath(arrow, color, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION (reused from HomeScreen pattern)
══════════════════════════════════════════════════════════════ */

@Composable
private fun GeomDecoration(
    sizeDp: Dp = 160.dp,
    color: Color = LocalRafiqColors.current.gold.copy(alpha = 0.10f),
    spinDuration: Int = 90_000,
    modifier: Modifier = Modifier,
) {
    val tr = rememberInfiniteTransition(label = "geom")
    val rotation by tr.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(spinDuration, easing = LinearEasing)),
        label = "geomRot"
    )
    Canvas(modifier = modifier.size(sizeDp)) {
        val sz = this.size.width
        val cx = sz / 2f; val cy = sz / 2f
        rotate(rotation, pivot = Offset(cx, cy)) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (i * 60 - 90) * PI.toFloat() / 180f
                    val r = sz * 0.43f
                    val px = cx + r * cos(a); val py = cy + r * sin(a)
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
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
        modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ══════════════════════════════════════════════════════════════
   DAILY RECITATION CARD (Hero)
══════════════════════════════════════════════════════════════ */

@Composable
private fun DailyRecitationCard() {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp),
                ambientColor = LocalRafiqColors.current.emerald.copy(alpha = 0.22f),
                spotColor = LocalRafiqColors.current.emerald.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Background gradient
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(
                    listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
        )

        // Rotating geometric decoration
        GeomDecoration(
            sizeDp = 220.dp,
            color = LocalRafiqColors.current.goldLight.copy(alpha = 0.14f),
            spinDuration = 80_000,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-55).dp, y = (-55).dp)
                .graphicsLayer { alpha = 0.3f },
        )
        GeomDecoration(
            sizeDp = 150.dp,
            color = Color.White.copy(alpha = 0.05f),
            spinDuration = 100_000,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 40.dp)
                .graphicsLayer { alpha = 0.15f },
        )

        Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
            Text("تلاوة اليوم", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Text("سورة الكهف", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 34.sp)
            Spacer(Modifier.height(4.dp))
            Text("صفحة ٢٩٣ · ١١٠ آيات", fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)).clickable { }, contentAlignment = Alignment.Center) { IconPrev(16.dp) }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(48.dp).clip(CircleShape).background(rc.goldLight).clickable { }, contentAlignment = Alignment.Center) { IconPlay(22.dp) }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)).clickable { }, contentAlignment = Alignment.Center) { IconNext(16.dp) }
            }

            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.14f))) {
                Box(Modifier.fillMaxWidth(0.35f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Brush.horizontalGradient(listOf(rc.gold, rc.goldLight))))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("١٢:٣٤", fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
                Text("٣٥:١٢", fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   SEARCH BAR
══════════════════════════════════════════════════════════════ */

@Composable
private fun QuranSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val rc = LocalRafiqColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        placeholder = {
            Text("ابحث عن سورة...", color = LocalRafiqColors.current.inkLight, fontSize = 14.sp)
        },
        leadingIcon = {
            IconSearch(17.dp, rc.inkMed)
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LocalRafiqColors.current.gold.copy(alpha = 0.5f),
            unfocusedBorderColor = LocalRafiqColors.current.gold.copy(alpha = 0.12f),
            focusedContainerColor = LocalRafiqColors.current.card,
            unfocusedContainerColor = LocalRafiqColors.current.card,
            focusedTextColor = LocalRafiqColors.current.ink,
            unfocusedTextColor = LocalRafiqColors.current.ink,
            cursorColor = LocalRafiqColors.current.emerald,
        )
    )
}

/* ══════════════════════════════════════════════════════════════
   SURAH ROW CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun SurahCard(
    number: Int,
    nameAr: String,
    nameEn: String,
    ayahCount: Int,
    revelation: String,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(rc.emeraldPastel),
                contentAlignment = Alignment.Center,
            ) {
                Text("$number".localizedDigits(LocalArabicNumerals.current), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(nameAr, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.ink)
                Spacer(Modifier.height(2.dp))
                Text("$ayahCount آية · صفحة ${(number * 5 + 1)}".localizedDigits(LocalArabicNumerals.current), fontSize = 12.sp, color = LocalRafiqColors.current.inkMed)
            }
            Box(
                Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (revelation == "meccan") rc.meccanBg else rc.madaniBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (revelation == "meccan") "مكية" else "مدنية",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (revelation == "meccan") rc.meccanText else rc.madaniText,
                )
            }
            Spacer(Modifier.width(8.dp))
            IconArrowLeft(14.dp, rc.inkLight)
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN QURAN LIST SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun QuranListScreen(
    navController: NavHostController,
    viewModel: QuranListViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ TOP BAR ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBtn(onClick = { navController.navigate(RafiqRoute.QuranBookmarks.route) }) {
                        IconBookmark()
                    }
                    PillBtn(onClick = { navController.navigate(RafiqRoute.QuranSearch.route) }) {
                        IconSearch()
                    }
                }
                Text("القرآن الكريم", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            }

            when {
                state.isLoading -> LoadingState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // ═══ DAILY RECITATION CARD ═══
                    item {
                        DailyRecitationCard()
                        Spacer(Modifier.height(18.dp))
                    }

                    // ═══ SEARCH BAR ═══
                    item {
                        QuranSearchBar(
                            query = state.query,
                            onQueryChange = { viewModel.search(it) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // ═══ SECTION HEADER ═══
                    item {
                        Row(
                            Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier.width(4.dp).height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(rc.gold)
                            )
                            Text("السور", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark)
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // ═══ SURAH LIST ═══
                    items(state.filtered) { surah ->
                        Box(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            SurahCard(
                                number = surah.number,
                                nameAr = surah.nameAr,
                                nameEn = surah.nameEn,
                                ayahCount = surah.ayahCount,
                                revelation = surah.revelation,
                                onClick = {
                                    navController.navigate(RafiqRoute.QuranReading.withSurah(surah.number))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
