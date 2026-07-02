package app.rafiqaldhikr.ui.screens.dua

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun EmotionalDuaScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current

    val emotions = listOf(
        Triple("😔", "عند الحزن", "اللهم إني أعوذ بك من الهم والحزن"),
        Triple("😰", "عند القلق", "لا إله إلا أنت سبحانك إني كنت من الظالمين"),
        Triple("😡", "عند الغضب", "أعوذ بالله من الشيطان الرجيم"),
        Triple("😨", "عند الخوف", "حسبنا الله ونعم الوكيل"),
        Triple("🥲", "عند الوحدة", "يا حي يا قيوم برحمتك أستغيث"),
        Triple("🤕", "عند المرض", "أذهب البأس رب الناس واشف أنت الشافي")
    )

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
                    text = "أدعية حسب المشاعر",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                emotions.forEach { (emoji, label, dua) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(rc.card)
                            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 32.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rc.emerald
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                dua,
                                fontSize  = 17.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.End,
                                color     = rc.ink,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
