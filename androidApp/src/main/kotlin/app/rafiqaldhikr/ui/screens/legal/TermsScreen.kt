package app.rafiqaldhikr.ui.screens.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun TermsScreen(navController: NavHostController) {
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
                title  = stringResource(R.string.settings_terms),
                onBack = {navController.popBackStack()},
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                        .padding(20.dp)
                ) {
                    val sections = listOf(
                        R.string.tos_accept_t to R.string.tos_accept_b,
                        R.string.tos_allowed_t to R.string.tos_allowed_b,
                        R.string.tos_religious_t to R.string.tos_religious_b,
                        R.string.tos_times_t to R.string.tos_times_b,
                        R.string.tos_ip_t to R.string.tos_ip_b,
                        R.string.tos_disclaimer_t to R.string.tos_disclaimer_b,
                        R.string.tos_changes_t to R.string.tos_changes_b,
                    )
                    sections.forEachIndexed { i, (title, bodyRes) ->
                        if (i > 0) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))
                        }
                        SectionTitle(stringResource(title), rc)
                        SectionBody(stringResource(bodyRes), rc)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, rc: RafiqPalette) {
    Spacer(Modifier.height(16.dp))
    Text(text, fontWeight = FontWeight.Bold, color = rc.emerald, style = RafiqType.titleM)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun SectionBody(text: String, rc: RafiqPalette) {
    Text(text, color = rc.inkMed, lineHeight = 22.sp, style = RafiqType.bodyS)
}
