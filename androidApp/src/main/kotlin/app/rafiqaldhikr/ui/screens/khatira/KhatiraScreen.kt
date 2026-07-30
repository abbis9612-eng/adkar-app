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
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

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
            RafiqTopBar(
                title  = stringResource(R.string.khatira),
                onBack = {navController.popBackStack()},
            )

            when {
                state.isLoading -> LoadingState()
                state.khatira == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد خاطرة اليوم", color = rc.inkMed, style = RafiqType.body)
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
                                .rafiqCard()
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
                        Text(text  = "💭 تأمل",
                            fontWeight = FontWeight.SemiBold,
                            color = rc.emerald, style = RafiqType.titleM)
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
                                    .clip(RafiqShape.item)
                                    .background(rc.emeraldPastel)
                                    .border(1.dp, rc.emerald, RafiqShape.item)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("موسم: ${k.season}", color = rc.emerald, style = RafiqType.caption)
                            }
                        }
                    }
                }
            }
        }
    }
}
