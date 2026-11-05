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
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun AccessibilitySettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
    val reducedMotion by vm.reducedMotion.collectAsStateWithLifecycle()
    val highContrast by vm.highContrast.collectAsStateWithLifecycle()
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
                title  = stringResource(R.string.accessibility),
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
                    // Item 1
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.a11y_reduced_motion), color = rc.ink, style = RafiqType.body)
                            Text(stringResource(R.string.a11y_reduced_motion_desc), fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            checked = reducedMotion,
                            onCheckedChange = { vm.setAccessibility(it, highContrast) },
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
                            Text(stringResource(R.string.a11y_high_contrast), color = rc.ink, style = RafiqType.body)
                            Text(stringResource(R.string.a11y_high_contrast_desc), fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            // كان `checked = false` مكتوبةً في الكود: المفتاح
                            // يُحفظ ولا يُقرأ، فيعود مطفأً في كل فتحة.
                            checked = highContrast,
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
