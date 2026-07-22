package app.rafiqaldhikr.ui.screens.widget

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
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.IcoMisbaha

@Composable
fun WidgetSettingsScreen(navController: NavHostController) {
    var prayerWidgetEnabled by remember { mutableStateOf(true) }
    var tasbeehWidgetEnabled by remember { mutableStateOf(false) }
    var adhkarWidgetEnabled by remember { mutableStateOf(false) }

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
            // ═══ HEADER ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "إعدادات الـ Widgets",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    "الأدوات المصغّرة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "اضغط مطولاً على الشاشة الرئيسية > الأدوات المصغرة > رفيق الذكر لإضافة widget",
                    fontSize = 14.sp,
                    color = rc.inkMed
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    WidgetOption(
                        icon    = { IcoMosque(24.dp, rc.emerald) },
                        title   = "مواقيت الصلاة Widget",
                        desc    = "يعرض الصلاة القادمة ووقتها",
                        enabled = prayerWidgetEnabled,
                        onToggle = { prayerWidgetEnabled = it },
                        rc = rc
                    )
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))

                    WidgetOption(
                        icon    = { IcoMisbaha(24.dp, rc.emerald) },
                        title   = "المسبحة Widget",
                        desc    = "عداد سريع من الشاشة الرئيسية",
                        enabled = tasbeehWidgetEnabled,
                        onToggle = { tasbeehWidgetEnabled = it },
                        rc = rc
                    )
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))

                    WidgetOption(
                        icon    = { RafiqIcon(RIcon.Document, 24.dp, rc.emerald) },
                        title   = "ذكر اليوم Widget",
                        desc    = "يعرض ذكراً متجدداً كل ساعة",
                        enabled = adhkarWidgetEnabled,
                        onToggle = { adhkarWidgetEnabled = it },
                        rc = rc
                    )
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(rc.emerald.copy(alpha = 0.05f))
                        .border(1.dp, rc.emerald.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RafiqIcon(RIcon.Bulb, 18.dp, rc.gold)
                            Text("ملاحظة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "widget مواقيت الصلاة يتحدث تلقائياً كل 30 دقيقة. قد تختلف النتائج قليلاً عن التطبيق.",
                            fontSize = 13.sp,
                            color = rc.inkMed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetOption(
    icon: @Composable () -> Unit, title: String, desc: String,
    enabled: Boolean, onToggle: (Boolean) -> Unit,
    rc: app.rafiqaldhikr.ui.theme.RafiqPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = rc.ink)
            Text(desc, fontSize = 13.sp, color = rc.inkMed)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = rc.bg,
                checkedTrackColor = rc.emerald,
                uncheckedThumbColor = rc.inkMed,
                uncheckedTrackColor = rc.bg,
                uncheckedBorderColor = rc.inkLight
            )
        )
    }
}
