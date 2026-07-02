package app.rafiqaldhikr.ui.screens.khatira

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun KhatiraScreen(
    navController: NavHostController,
    viewModel: KhatiraViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
                    stringResource(R.string.khatira),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            when {
                state.isLoading -> LoadingState()
                state.khatira == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد خاطرة اليوم", fontSize = 16.sp, color = rc.inkMed)
                    }
                }
                else -> {
                    val k = state.khatira!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // Verse or Hadith
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(rc.card)
                                .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text      = k.verseOrHadith,
                                    fontSize  = 24.sp,
                                    lineHeight = 40.sp,
                                    color     = rc.ink,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text      = "— ${k.source}",
                                    fontSize  = 14.sp,
                                    textAlign = TextAlign.End,
                                    modifier  = Modifier.fillMaxWidth(),
                                    color     = rc.inkMed
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Reflection
                        Text(
                            text  = "💭 تأمل",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = rc.emerald
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text      = k.reflection,
                            fontSize  = 16.sp,
                            lineHeight = 28.sp,
                            color     = rc.ink,
                            textAlign = TextAlign.Start
                        )

                        if (k.season != "normal") {
                            Spacer(Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rc.emeraldPastel)
                                    .border(1.dp, rc.emerald, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("موسم: ${k.season}", fontSize = 12.sp, color = rc.emerald)
                            }
                        }
                    }
                }
            }
        }
    }
}
