package app.rafiqaldhikr.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon

@Composable
fun AboutScreen(navController: NavHostController) {
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
                    text = "حول التطبيق",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RafiqIcon(RIcon.Moon, 64.dp, rc.emerald)
                Spacer(Modifier.height(16.dp))
                Text("رفيق الذكر", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Text("الإصدار " + app.rafiqaldhikr.BuildConfig.VERSION_NAME,
                    fontSize = 14.sp, color = rc.inkMed)
                Spacer(Modifier.height(24.dp))
                Text(
                    "رفيقك اليومي في رحلة الإيمان.\nأذكار، قرآن، أدعية، مسبحة، ومواقيت الصلاة\nكل ذلك في تطبيق واحد.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    color = rc.inkMed
                )
                Spacer(Modifier.height(32.dp))
                Text("صُنع بـ ❤️ لله", fontSize = 14.sp, color = rc.emerald)
            }
        }
    }
}
