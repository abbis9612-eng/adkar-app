package app.rafiqaldhikr.ui.screens.help

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.RafiqBackButton

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RafiqBackButton(onClick = { navController.popBackStack() })

                Text(
                    text = "المساعدة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    FaqItem("كيف أستخدم المسبحة؟", "اضغط على الدائرة المركزية للعد. يمكنك تغيير الذكر والعدد المستهدف من الأيقونات أعلاه.", rc, isLast = false)
                    FaqItem("كيف أضيف علامة في القرآن؟", "أثناء القراءة، اضغط على أيقونة العلامة بجانب أي آية.", rc, isLast = false)
                    FaqItem("هل التطبيق يعمل بدون إنترنت؟", "نعم! جميع الأذكار والأدعية والقرآن متاحة بدون اتصال.", rc, isLast = false)
                    FaqItem("كيف يتم حساب مواقيت الصلاة؟", "يتم حسابها حسب الموقع الجغرافي وطريقة الحساب المختارة في الإعدادات.", rc, isLast = false)
                    FaqItem("كيف أتواصل معكم؟", "يمكنك مراسلتنا عبر البريد الإلكتروني: support@rafiqaldhikr.app", rc, isLast = true)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String, rc: RafiqPalette, isLast: Boolean) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(question, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
        Spacer(Modifier.height(6.dp))
        Text(answer, fontSize = 14.sp, color = rc.inkMed, lineHeight = 22.sp)
        if (!isLast) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))
        }
    }
}
