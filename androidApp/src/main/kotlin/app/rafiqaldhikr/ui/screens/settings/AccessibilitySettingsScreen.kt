package app.rafiqaldhikr.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccessibilitySettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
    val reducedMotion by vm.reducedMotion.collectAsState()
    val rc = LocalRafiqColors.current

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // u2550u2550u2550 HEADER u2550u2550u2550
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width; val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f); lineTo(w * 0.70f, h * 0.50f); lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }

                Text(
                    text = "إمكانية الوصول",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            // Content
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    // Item 1
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("تقليل الحركة", fontSize = 16.sp, color = rc.ink)
                            Text("تقليل الرسوم المتحركة", fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            checked = reducedMotion,
                            onCheckedChange = { vm.setAccessibility(it, false) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = rc.card,
                                checkedTrackColor = rc.emerald,
                                uncheckedThumbColor = rc.inkLight,
                                uncheckedTrackColor = rc.divider
                            )
                        )
                    }
                    
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Item 2
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("تباين عالي", fontSize = 16.sp, color = rc.ink)
                            Text("زيادة وضوح الألوان والنصوص", fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = { vm.setAccessibility(reducedMotion, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = rc.card,
                                checkedTrackColor = rc.emerald,
                                uncheckedThumbColor = rc.inkLight,
                                uncheckedTrackColor = rc.divider
                            )
                        )
                    }
                }
            }
        }
    }
}
