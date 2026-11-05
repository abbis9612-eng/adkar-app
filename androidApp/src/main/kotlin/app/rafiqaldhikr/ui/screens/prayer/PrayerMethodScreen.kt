package app.rafiqaldhikr.ui.screens.prayer

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
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
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import androidx.compose.ui.draw.clip
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
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun PrayerMethodScreen(
    navController: NavHostController,
    settingsVM: SettingsViewModel = koinViewModel()
) {
    val methods = listOf(
        "mwl"         to stringResource(R.string.method_mwl_full),
        "umm_al_qura" to stringResource(R.string.method_umm_full),
        "egyptian"    to stringResource(R.string.method_egypt_full),
        "isna"        to stringResource(R.string.method_isna_full),
        "karachi"     to stringResource(R.string.method_karachi_full),
        "turkey"      to stringResource(R.string.method_turkey_full)
    )

    val madhabOptions = listOf(
        "shafi"  to stringResource(R.string.madhab_majority),
        "hanafi" to stringResource(R.string.madhab_hanafi_full)
    )

    val selected by settingsVM.prayerMethod.collectAsState()
    val selectedMadhab by settingsVM.madhab.collectAsState()
    val elevation by settingsVM.elevation.collectAsState()

    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

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
                title  = stringResource(R.string.method_title),
                onBack = {navController.popBackStack()},
            )

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
                        .rafiqCard()
                ) {
                    methods.forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setPrayerMethod(key) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = rc.ink, modifier = Modifier.weight(1f), style = RafiqType.body)
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
                Text(text = stringResource(R.string.madhab_section),
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp), style = RafiqType.titleM)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.madhab_note),
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    madhabOptions.forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setMadhab(key) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = rc.ink, modifier = Modifier.weight(1f), style = RafiqType.body)
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
                Text(text = stringResource(R.string.elevation_label),
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp), style = RafiqType.titleM)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.elevation_note),
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
                
                /*  ارتفاعاتٌ شائعة. وكان فيها «٨٥٠ متر (السليمانية)» —
                 *  مدينةٌ بعينها في قائمةٍ عامّة، بقيّةُ أثرِ الاحتياطيّ
                 *  الذي كان يفرض إحداثياتِها على كل من لا موقعَ له.  */
                val presetElevations = listOf(
                    0.0    to stringResource(R.string.elevation_sea),
                    200.0  to stringResource(R.string.elevation_metres, 200.localized(ar)),
                    500.0  to stringResource(R.string.elevation_metres, 500.localized(ar)),
                    700.0  to stringResource(R.string.elevation_metres, 700.localized(ar)),
                    850.0  to stringResource(R.string.elevation_metres, 850.localized(ar)),
                    1000.0 to stringResource(R.string.elevation_metres, 1000.localized(ar)),
                    1500.0 to stringResource(R.string.elevation_metres, 1500.localized(ar)),
                )
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    presetElevations.forEachIndexed { index, (value, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { settingsVM.setElevation(value) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = rc.ink, modifier = Modifier.weight(1f), style = RafiqType.body)
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
                    Text(text = "أو اختر يدوياً: ${elevation.toInt()} متر",
                        color = rc.ink,
                        modifier = Modifier.padding(horizontal = 16.dp), style = RafiqType.body)
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
                            text = stringResource(R.string.elevation_maghrib_corr, corrMinutes.localized(ar)),
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
                Text(text = stringResource(R.string.offsets_label),
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp), style = RafiqType.titleM)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.offsets_note),
                    fontSize = 13.sp,
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    val f = settingsVM.fajrOffset.collectAsState().value
                    val d = settingsVM.dhuhrOffset.collectAsState().value
                    val a = settingsVM.asrOffset.collectAsState().value
                    val m = settingsVM.maghribOffset.collectAsState().value
                    val i = settingsVM.ishaOffset.collectAsState().value
                    
                    OffsetRow(stringResource(R.string.prayer_fajr), f, rc) { settingsVM.setPrayerOffsets(it, d, a, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow(stringResource(R.string.prayer_dhuhr), d, rc) { settingsVM.setPrayerOffsets(f, it, a, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow(stringResource(R.string.prayer_asr), a, rc) { settingsVM.setPrayerOffsets(f, d, it, m, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow(stringResource(R.string.prayer_maghrib), m, rc) { settingsVM.setPrayerOffsets(f, d, a, it, i) }
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    OffsetRow(stringResource(R.string.prayer_isha), i, rc) { settingsVM.setPrayerOffsets(f, d, a, m, it) }
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
        Text(label, color = rc.ink, style = RafiqType.body)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RafiqShape.item)
                    .background(rc.bg)
                    .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.item)
                    .clickable { onValueChange(value - 1) },
                contentAlignment = Alignment.Center
            ) { Text("-", color = rc.emerald, style = RafiqType.titleM) }
            
            Text(text = if (value > 0) "+$value" else value.toString(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = rc.ink, style = RafiqType.body)
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RafiqShape.item)
                    .background(rc.bg)
                    .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.item)
                    .clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) { Text("+", color = rc.emerald, style = RafiqType.titleM) }
        }
    }
}
