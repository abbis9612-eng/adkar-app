package app.rafiqaldhikr.ui.screens.ramadan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette

@Composable
fun RamadanHomeScreen(navController: NavHostController) {
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
                    text = "وضع رمضان 🌙",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                // Back Button
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
                        val w = size.width
                        val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f)
                            lineTo(w * 0.70f, h * 0.50f)
                            lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Ramadan header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(rc.emerald)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌙", fontSize = 48.sp)
                        Text(
                            "رمضان كريم", 
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "شهر القرآن والرحمة", 
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Quick actions for Ramadan
                Text(
                    "برنامج رمضان", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink
                )
                Spacer(Modifier.height(16.dp))

                RamadanActionCard("📖", "ورد القرآن اليومي", "اقرأ 20 صفحة لختم القرآن في رمضان", rc) {
                    navController.navigate(RafiqRoute.QuranList.route)
                }
                RamadanActionCard("🤲", "أدعية الصائم", "أدعية الإفطار والسحور", rc) {
                    navController.navigate(RafiqRoute.DuaCategories.route)
                }
                RamadanActionCard("🕌", "مواقيت الصلاة", "الإمساك والإفطار", rc) {
                    navController.navigate(RafiqRoute.PrayerTimes.route)
                }
                RamadanActionCard("📿", "التسبيح", "استغل وقت ما قبل الإفطار", rc) {
                    navController.navigate(RafiqRoute.Tasbeeh.route)
                }
                RamadanActionCard("🌅", "أذكار الصباح", "لا تنسَ أذكارك", rc) {
                    navController.navigate(RafiqRoute.DhikrReading.withCategory("morning"))
                }

                Spacer(Modifier.height(32.dp))

                // Tip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(rc.emerald.copy(alpha = 0.05f))
                        .border(1.dp, rc.emerald.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "💡 نصيحة رمضانية", 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold,
                            color = rc.emerald
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\"خيركم من تعلم القرآن وعلمه\" — حديث البخاري",
                            fontSize = 14.sp,
                            color = rc.inkMed,
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun RamadanActionCard(emoji: String, title: String, desc: String, rc: RafiqPalette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(rc.emerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    desc, 
                    fontSize = 13.sp, 
                    color = rc.inkMed
                )
            }
        }
    }
}
