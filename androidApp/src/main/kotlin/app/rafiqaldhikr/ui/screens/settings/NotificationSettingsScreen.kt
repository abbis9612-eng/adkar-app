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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun NotificationSettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
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
                title  = "إعدادات الإشعارات",
                onBack = {navController.popBackStack()},
            )

            // Content
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))
                
                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    val enabled by vm.notificationsEnabled.collectAsState()

                    // Main Toggle
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("تفعيل الإشعارات", color = rc.ink, style = RafiqType.body)
                            Text("تذكيرات الأذكار ومواقيت الصلاة", fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { vm.setNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = rc.card,
                                checkedTrackColor = rc.emerald,
                                uncheckedThumbColor = rc.inkLight,
                                uncheckedTrackColor = rc.divider
                            )
                        )
                    }
                    
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Items
                    listOf(
                        "أذكار الصباح" to "بعد صلاة الفجر",
                        "أذكار المساء" to "بعد صلاة العصر",
                        "مواقيت الصلاة" to "قبل الأذان بـ 5 دقائق"
                    ).forEachIndexed { index, (title, subtitle) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, color = if (enabled) rc.ink else rc.inkMed, style = RafiqType.body)
                                Text(subtitle, fontSize = 13.sp, color = if (enabled) rc.inkMed else rc.inkMed)
                            }
                        }
                        
                        if (index < 2) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}
