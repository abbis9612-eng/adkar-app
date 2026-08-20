package app.rafiqaldhikr.ui.screens.help

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
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
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun HelpScreen(navController: NavHostController) {
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
                title  = stringResource(R.string.help_title),
                onBack = {navController.popBackStack()},
            )

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
                    FaqItem(stringResource(R.string.help_q1), stringResource(R.string.help_a1), rc, isLast = false)
                    FaqItem(stringResource(R.string.help_q2), stringResource(R.string.help_a2), rc, isLast = false)
                    FaqItem(stringResource(R.string.help_q3), stringResource(R.string.help_a3), rc, isLast = false)
                    FaqItem(stringResource(R.string.help_q4), stringResource(R.string.help_a4), rc, isLast = false)
                    FaqItem(stringResource(R.string.help_q5), stringResource(R.string.help_a5), rc, isLast = true)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String, rc: RafiqPalette, isLast: Boolean) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(question, fontWeight = FontWeight.Bold, color = rc.emerald, style = RafiqType.body)
        Spacer(Modifier.height(6.dp))
        Text(answer, color = rc.inkMed, lineHeight = 22.sp, style = RafiqType.bodyS)
        if (!isLast) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))
        }
    }
}
