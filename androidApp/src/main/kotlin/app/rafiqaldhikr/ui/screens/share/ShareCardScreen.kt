package app.rafiqaldhikr.ui.screens.share

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShareCardScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val rc = LocalRafiqColors.current

    var selectedCard by remember { mutableIntStateOf(0) }

    val cards = listOf(
        ShareCardData("🔥 سلسلتي", "${state.streak.current} يوم متواصل!", "الحمد لله على التوفيق"),
        ShareCardData("📖 ختمتي", "${state.todayProgress?.quranPages ?: 0} صفحات اليوم", "خير ما يجالسه المرء"),
        ShareCardData("📿 تسبيحي", "${state.todayProgress?.tasbeehCount ?: 0} تسبيحة", "وسبح بحمد ربك")
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
            // u2550u2550u2550 HEADER u2550u2550u2550
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width; val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f); lineTo(w * 0.70f, h * 0.50f); lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }

                Text(
                    text = "مشاركة بطاقة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card preview
                val card = cards[selectedCard]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(rc.emerald, rc.gold)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.bg)
                        Spacer(Modifier.height(12.dp))
                        Text(card.value, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = rc.bg)
                        Spacer(Modifier.height(8.dp))
                        Text(card.subtitle, fontSize = 14.sp, color = rc.bg.copy(alpha = 0.8f))
                        Spacer(Modifier.height(16.dp))
                        Text("رفيق الذكر 🌙", fontSize = 12.sp, color = rc.bg.copy(alpha = 0.6f))
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Card type selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    cards.forEachIndexed { i, c ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(if (selectedCard == i) 2.dp else 0.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedCard == i) rc.emerald else rc.card)
                                .border(1.dp, if (selectedCard == i) rc.emerald else rc.gold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable { selectedCard = i }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = c.title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedCard == i) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCard == i) rc.bg else rc.ink
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Share button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(rc.emerald)
                        .clickable {
                            val shareText = "${cards[selectedCard].title}\n${cards[selectedCard].value}\n${cards[selectedCard].subtitle}\n\nعبر تطبيق رفيق الذكر 🌙"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة"))
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = rc.bg)
                        Spacer(Modifier.width(8.dp))
                        Text("مشاركة البطاقة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rc.bg)
                    }
                }
            }
        }
    }
}

private data class ShareCardData(val title: String, val value: String, val subtitle: String)
