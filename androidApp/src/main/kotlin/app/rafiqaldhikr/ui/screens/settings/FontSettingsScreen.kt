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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
    val scale by vm.fontScale.collectAsState()
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
            RafiqTopBar(
                title  = "إعدادات الخط",
                onBack = {navController.popBackStack()},
            )

            // Content
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                        .padding(16.dp)
                ) {
                    Text(
                        "معاينة",
                        fontSize = 14.sp,
                        color = rc.inkMed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                        fontSize = 24.sp * scale,
                        color = rc.ink,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(32.dp))
                    
                    Slider(
                        value = scale,
                        onValueChange = { vm.setFontScale(it) },
                        valueRange = 0.8f..1.6f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = rc.emerald,
                            activeTrackColor = rc.emerald,
                            inactiveTrackColor = rc.gold.copy(alpha = 0.2f)
                        )
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("أصغر", fontSize = 12.sp, color = rc.inkMed)
                        Text("${(scale * 100).toInt()}%", fontSize = 14.sp, color = rc.gold, fontWeight = FontWeight.Bold)
                        Text("أكبر", fontSize = 12.sp, color = rc.inkMed)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ═══ لغة الأرقام ═══
                val arabicNums by vm.arabicNumerals.collectAsState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    Text(
                        "لغة الأرقام",
                        fontSize = 14.sp,
                        color = rc.inkMed,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
                    )
                    listOf(
                        true to "عربية — ٠١٢٣٤٥٦٧٨٩",
                        false to "إنجليزية — 0123456789",
                    ).forEachIndexed { index, (isArabic, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.setNumerals(isArabic) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                fontSize = 16.sp,
                                color = rc.ink,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = arabicNums == isArabic,
                                onClick = { vm.setNumerals(isArabic) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = rc.gold,
                                    unselectedColor = rc.inkLight
                                )
                            )
                        }
                        if (index == 0) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
