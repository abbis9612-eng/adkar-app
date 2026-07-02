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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ذكر مخصص",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Input card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value         = dhikrText,
                            onValueChange = { dhikrText = it },
                            label         = { Text("نص الذكر") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
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
                            shape         = RoundedCornerShape(12.dp),
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
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = rc.emerald)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = rc.bg)
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة ذكر", color = rc.bg, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "الأذكار المخصصة", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink
                )
                Spacer(Modifier.height(12.dp))

                savedDhikrs.forEach { dhikr ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(rc.card)
                            .border(1.dp, rc.gold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dhikr.dhikr_text, 
                                fontSize = 18.sp, 
                                color = rc.ink,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteDhikr(dhikr.id) }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "حذف", 
                                    tint = rc.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
