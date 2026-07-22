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
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun ThemeSettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
    val theme by vm.theme.collectAsState()
    val dynamic by vm.dynamicColor.collectAsState()
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
                Text(
                    text = "إعدادات المظهر",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
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
                    listOf("system" to "تلقائي (النظام)", "light" to "فاتح", "dark" to "داكن").forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.setTheme(key, dynamic) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                fontSize = 16.sp,
                                color = rc.ink,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = theme == key,
                                onClick = { vm.setTheme(key, dynamic) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = rc.gold,
                                    unselectedColor = rc.inkLight
                                )
                            )
                        }
                        if (index < 2) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "ألوان ديناميكية",
                                fontSize = 16.sp,
                                color = rc.ink
                            )
                            Text(
                                "استخدام ألوان الخلفية (Android 12+)",
                                fontSize = 13.sp,
                                color = rc.inkMed
                            )
                        }
                        Switch(
                            checked = dynamic,
                            onCheckedChange = { vm.setTheme(theme, it) },
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
