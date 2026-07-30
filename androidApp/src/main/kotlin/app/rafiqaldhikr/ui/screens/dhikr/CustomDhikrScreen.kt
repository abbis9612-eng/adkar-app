package app.rafiqaldhikr.ui.screens.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import app.rafiqaldhikr.ui.components.IcoPlus
import app.rafiqaldhikr.ui.components.IcoTrash
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun CustomDhikrScreen(
    navController: NavHostController,
    viewModel: CustomDhikrViewModel = org.koin.androidx.compose.koinViewModel()
) {
    var dhikrText by remember { mutableStateOf("") }
    var targetCount by remember { mutableStateOf("33") }
    
    val savedDhikrs by viewModel.customDhikrs.collectAsState()
    val rc = LocalRafiqColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            RafiqTopBar(
                title  = "ذكر مخصص",
                onBack = {navController.popBackStack()},
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Input card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value         = dhikrText,
                            onValueChange = { dhikrText = it },
                            label         = { Text("نص الذكر") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RafiqShape.item,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = rc.emerald,
                                unfocusedBorderColor = rc.divider,
                                cursorColor = rc.emerald,
                                focusedLabelColor = rc.emerald
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value         = targetCount,
                            onValueChange = { if (it.all { c -> c.isDigit() }) targetCount = it },
                            label         = { Text("العدد المستهدف") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RafiqShape.item,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = rc.emerald,
                                unfocusedBorderColor = rc.divider,
                                cursorColor = rc.emerald,
                                focusedLabelColor = rc.emerald
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (dhikrText.isNotBlank()) {
                                    val target = targetCount.toLongOrNull() ?: 33L
                                    viewModel.addDhikr(dhikrText, target)
                                    dhikrText = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape    = RafiqShape.item,
                            colors   = ButtonDefaults.buttonColors(containerColor = rc.emerald)
                        ) {
                            IcoPlus(20.dp, rc.bg)
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة ذكر", color = rc.bg, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("الأذكار المخصصة", 
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink, style = RafiqType.titleM)
                Spacer(Modifier.height(12.dp))

                savedDhikrs.forEach { dhikr ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RafiqShape.item)
                            .background(rc.card)
                            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.item)
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = dhikr.dhikr_text, 
                                color = rc.ink,
                                modifier = Modifier.weight(1f), style = RafiqType.titleM)
                            IconButton(onClick = { viewModel.deleteDhikr(dhikr.id) }) {
                                IcoTrash(22.dp, rc.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
