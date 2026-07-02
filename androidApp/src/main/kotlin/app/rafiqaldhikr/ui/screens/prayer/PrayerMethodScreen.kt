package app.rafiqaldhikr.ui.screens.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.settings.SettingsViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun PrayerMethodScreen(
    navController: NavHostController,
    settingsVM: SettingsViewModel = koinViewModel()
) {
    val methods = listOf(
        "mwl"         to "رابطة العالم الإسلامي (مُوصى به)",
        "umm_al_qura" to "أم القرى (مكة)",
        "egyptian"    to "الهيئة المصرية العامة للمساحة",
        "isna"        to "الجمعية الإسلامية لأمريكا الشمالية",
        "karachi"     to "جامعة العلوم الإسلامية كراتشي",
        "turkey"      to "رئاسة الشؤون الدينية التركية"
    )

    val madhabOptions = listOf(
        "shafi"  to "الشافعي / المالكي / الحنبلي",
        "hanafi" to "الحنفي"
    )

    val selected by settingsVM.prayerMethod.collectAsState()
    val selectedMadhab by settingsVM.madhab.collectAsState()
    val elevation by settingsVM.elevation.collectAsState()

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
                RafiqBackButton(onClick = { navController.popBackStack() })

                Text(
                    text = "طريقة الحساب",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            // Content
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                
                // ═══ طريقة الحساب ═══
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    methods.forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setPrayerMethod(key) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 16.sp, color = rc.ink, modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = selected == key,
                                onClick = { settingsVM.setPrayerMethod(key) },
                                colors = RadioButtonDefaults.colors(selectedColor = rc.emerald, unselectedColor = rc.inkLight)
                            )
                        }
                        if (index < methods.lastIndex) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // ═══ المذهب الفقهي (العصر) ═══
                Text(
                    text = "المذهب الفقهي (وقت العصر)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "يؤثر المذهب على حساب وقت صلاة العصر.",
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    madhabOptions.forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setMadhab(key) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 16.sp, color = rc.ink, modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = selectedMadhab == key,
                                onClick = { settingsVM.setMadhab(key) },
                                colors = RadioButtonDefaults.colors(selectedColor = rc.emerald, unselectedColor = rc.inkLight)
                            )
                        }
                        if (index < madhabOptions.lastIndex) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // ═══ الارتفاع عن سطح البحر ═══
                Text(
                    text = "الارتفاع عن سطح البحر (بالمتر)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "يُصحّح وقت المغرب والشروق. مثال: السليمانية ≈ 850 متر.",
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                
                val presetElevations = listOf(
                    0.0    to "0 — مستوى البحر",
                    200.0  to "200 متر",
                    500.0  to "500 متر",
                    700.0  to "700 متر",
                    850.0  to "850 متر (السليمانية)",
                    1000.0 to "1000 متر",
                    1500.0 to "1500 متر"
                )
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    presetElevations.forEachIndexed { index, (value, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setElevation(value) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 16.sp, color = rc.ink, modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = elevation == value,
                                onClick = { settingsVM.setElevation(value) },
                                colors = RadioButtonDefaults.colors(selectedColor = rc.emerald, unselectedColor = rc.inkLight)
                            )
                        }
                        HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    
                    // Custom elevation slider
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "أو اختر يدوياً: ${elevation.toInt()} متر",
                        fontSize = 16.sp,
                        color = rc.ink,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Slider(
                        value = elevation.toFloat(),
                        onValueChange = { settingsVM.setElevation(it.toDouble()) },
                        valueRange = 0f..2000f,
                        steps = 39,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = rc.emerald,
                            activeTrackColor = rc.emerald,
                            inactiveTrackColor = rc.gold.copy(alpha = 0.2f)
                        )
                    )
                    
                    // حساب تأثير الارتفاع
                    if (elevation > 0) {
                        val corrMinutes = ((elevation / 100.0) * 37.5 / 60.0).toInt()
                        Text(
                            text = "⏱ تصحيح المغرب: +$corrMinutes دقيقة",
                            fontSize = 13.sp,
                            color = rc.gold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 12.dp)
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // ═══ التصحيح اليدوي ═══
                Text(
                    text = "التصحيح المخصص للأوقات (بالدقائق)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "يمكنك إضافة أو إنقاص دقائق لتطابق توقيت مدينتك أو المسجد المحلي.",
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    val f = settingsVM.fajrOffset.collectAsState().value
                    val d = settingsVM.dhuhrOffset.collectAsState().value
                    val a = settingsVM.asrOffset.collectAsState().value
                    val m = settingsVM.maghribOffset.collectAsState().value
                    val i = settingsVM.ishaOffset.collectAsState().value
                    
                    OffsetRow("الفجر", f, rc) { settingsVM.setPrayerOffsets(it, d, a, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow("الظهر", d, rc) { settingsVM.setPrayerOffsets(f, it, a, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow("العصر", a, rc) { settingsVM.setPrayerOffsets(f, d, it, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow("المغرب", m, rc) { settingsVM.setPrayerOffsets(f, d, a, it, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow("العشاء", i, rc) { settingsVM.setPrayerOffsets(f, d, a, m, it) }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OffsetRow(label: String, value: Int, rc: RafiqPalette, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = rc.ink)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rc.bg)
                    .border(1.dp, rc.gold.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onValueChange(value - 1) },
                contentAlignment = Alignment.Center
            ) { Text("-", fontSize = 18.sp, color = rc.emerald) }
            
            Text(
                text = if (value > 0) "+$value" else value.toString(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = rc.ink
            )
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rc.bg)
                    .border(1.dp, rc.gold.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) { Text("+", fontSize = 18.sp, color = rc.emerald) }
        }
    }
}
