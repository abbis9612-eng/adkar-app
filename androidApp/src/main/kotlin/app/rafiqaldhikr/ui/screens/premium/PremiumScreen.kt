package app.rafiqaldhikr.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

@Composable
fun PremiumScreen(navController: NavHostController) {
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
                    "الترقية",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
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
                modifier            = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))
                // Star Icon
                androidx.compose.foundation.Canvas(Modifier.size(72.dp)) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val outerRadius = w / 2f
                    val innerRadius = w / 4f
                    val path = androidx.compose.ui.graphics.Path()
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerRadius else innerRadius
                        val a = (i * 36 - 90) * (Math.PI / 180).toFloat()
                        val x = cx + r * kotlin.math.cos(a)
                        val y = cy + r * kotlin.math.sin(a)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, rc.gold)
                }

                Spacer(Modifier.height(16.dp))
                Text("رفيق الذكر المميز", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Spacer(Modifier.height(8.dp))
                Text("ادعم المشروع واستمتع بمزايا حصرية", fontSize = 16.sp, textAlign = TextAlign.Center, color = rc.inkMed)
                Spacer(Modifier.height(32.dp))

                listOf("إزالة الإعلانات", "مظاهر حصرية", "أصوات مسبحة إضافية", "ويدجت مخصصة", "تقارير أسبوعية").forEach { feature ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = rc.emerald, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(feature, fontSize = 16.sp, color = rc.ink)
                    }
                }

                Spacer(Modifier.weight(1f))
                
                // Subscribe Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(rc.emerald, rc.emeraldMed)))
                        .clickable { /* RevenueCat M2 */ }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("اشترك الآن", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.height(8.dp))
                
                TextButton(onClick = { /* restore */ }) {
                    Text("استعادة المشتريات", color = rc.emerald)
                }
            }
        }
    }
}
