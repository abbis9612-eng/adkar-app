package app.rafiqaldhikr.ui.screens.daycompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.StationIcon
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel

@Composable
fun DayCompanionScreen(
    navController: NavHostController,
    viewModel: DayCompanionViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RafiqTopBar(
                title    = "رفيق اليوم",
                subtitle = "«أحبُّ الأعمال إلى الله أدومُها وإن قلّ» — متفق عليه",
                onBack   = { navController.popBackStack() },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ═══ ملخص التقدم — بطل الصفحة بحلقة تقدّم ═══
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(rc.heroStart, rc.heroMid, rc.heroEnd)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "رحلة يومك مع الذكر",
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (state.doneCount == 0) "ابدأ أول محطة — خطوة صغيرة تفتح يوماً عظيماً"
                                    else if (state.doneCount == state.stations.size) "ما شاء الله! أتممت رحلة اليوم كاملة"
                                    else "أحسنت — واصل، بقيت ${(state.stations.size - state.doneCount).localized(LocalArabicNumerals.current)} محطات",
                                    fontSize = 12.sp, lineHeight = 18.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            // حلقة تقدّم دائرية
                            Box(contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Canvas(Modifier.size(72.dp)) {
                                    val sw = 7.dp.toPx()
                                    val r = (size.minDimension - sw) / 2f
                                    val tl = androidx.compose.ui.geometry.Offset(
                                        size.width / 2f - r, size.height / 2f - r)
                                    val sz = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                                    drawArc(Color.White.copy(alpha = 0.22f), 0f, 360f, false, tl, sz,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            sw, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                    val frac = if (state.stations.isEmpty()) 0f
                                        else state.doneCount / state.stations.size.toFloat()
                                    if (frac > 0.005f) {
                                        drawArc(rc.goldLight, -90f, 360f * frac, false, tl, sz,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                sw, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                    }
                                }
                                Text(
                                    "${state.doneCount.localized(LocalArabicNumerals.current)}/${state.stations.size.localized(LocalArabicNumerals.current)}",
                                    style = app.rafiqaldhikr.ui.theme.NumbersStyle,
                                    fontSize = 16.sp, color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                items(state.stations) { station ->
                    StationCard(
                        station = station,
                        isLast  = station.id == state.stations.lastOrNull()?.id,
                        onStart = { station.route?.let { navController.navigate(it) } },
                        onDone  = { viewModel.completeStation(station.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: DayCompanionViewModel.StationUi,
    isLast:  Boolean,
    onStart: () -> Unit,
    onDone:  () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val active = station.status == StationStatus.ACTIVE
    val done   = station.status == StationStatus.DONE

    val passed = station.status == StationStatus.PASSED

    Row(Modifier.height(IntrinsicSize.Min)) {
        // ═══ خط الزمن — عقدة أيقونية واضحة ═══
        Column(
            Modifier.width(44.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done   -> rc.emerald
                            active -> rc.card
                            else   -> rc.divider.copy(alpha = 0.45f)
                        }
                    )
                    .border(
                        width = if (active) 2.dp else 1.dp,
                        color = when {
                            active -> rc.gold
                            done   -> rc.emerald
                            else   -> rc.divider
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) app.rafiqaldhikr.ui.components.IcoCheck(15.dp, Color.White)
                else app.rafiqaldhikr.ui.components.StationGlyph(station.id, if (active) 20.dp else 18.dp, tint = if (active) rc.emerald else rc.inkLight)
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.5.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (done) rc.gold.copy(alpha = 0.6f) else rc.divider)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // ═══ بطاقة المحطة ═══
        Column(
            Modifier
                .weight(1f)
                .padding(bottom = 14.dp)
                .shadow(if (active) 5.dp else 1.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(rc.card)
                .background(if (active) rc.gold.copy(alpha = 0.06f) else Color.Transparent)
                .border(
                    width = if (active) 1.5.dp else 1.dp,
                    color = when {
                        active -> rc.gold.copy(alpha = 0.55f)
                        done   -> rc.emerald.copy(alpha = 0.25f)
                        else   -> rc.divider
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        station.title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (done || passed) rc.inkMed else rc.ink
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(station.timeLabel, fontSize = 11.sp, color = rc.inkLight)
                }
                when {
                    active -> Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(rc.emerald)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("الآن", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    done -> Text("تمّت", fontSize = 11.sp, color = rc.emerald, fontWeight = FontWeight.Bold)
                    passed -> Text("فاتت", fontSize = 11.sp, color = rc.inkLight)
                    else -> {}
                }
            }

            if (!done) {
                Spacer(Modifier.height(8.dp))
                Text(station.description, fontSize = 13.sp, lineHeight = 21.sp, color = rc.inkMed)
                if (station.virtue.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(rc.gold.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        app.rafiqaldhikr.ui.components.IcoStar(12.dp, rc.gold)
                        Spacer(Modifier.width(6.dp))
                        Text(station.virtue, fontSize = 11.sp, lineHeight = 17.sp,
                            color = rc.brownAccent, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (station.route != null) {
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(rc.emerald, rc.emeraldMed))
                                )
                                .clickable(onClick = onStart)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ابدأ", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.2.dp, rc.emerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onDone)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text("تمّ", fontSize = 13.sp, color = rc.emerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
