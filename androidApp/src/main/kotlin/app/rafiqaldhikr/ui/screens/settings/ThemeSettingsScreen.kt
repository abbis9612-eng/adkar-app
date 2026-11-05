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
import app.rafiqaldhikr.R
import androidx.compose.ui.res.stringResource
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
            RafiqTopBar(
                title  = stringResource(R.string.settings_theme),
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
                    listOf("system" to stringResource(R.string.theme_system_full), "light" to stringResource(R.string.theme_light), "dark" to stringResource(R.string.theme_dark)).forEachIndexed { index, (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.setTheme(key, dynamic) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label,
                                color = rc.ink,
                                modifier = Modifier.weight(1f), style = RafiqType.body)
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
                        .rafiqCard()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.theme_dynamic),
                                color = rc.ink, style = RafiqType.body)
                            Text(
                                stringResource(R.string.theme_dynamic_desc),
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
