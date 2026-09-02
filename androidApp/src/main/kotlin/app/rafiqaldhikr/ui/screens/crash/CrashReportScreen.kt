package app.rafiqaldhikr.ui.screens.crash

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.util.CrashLog

/* ══════════════════════════════════════════════════════════════
   شاشةُ تقرير الانهيار

   بلا سمةٍ ولا Koin ولا نموذجِ عرض — كلُّ واحدٍ منها قد يكون هو العطبَ
   نفسَه، وشاشةٌ تشرح الانهيار لا يصحّ أن تنهار به. فألوانٌ ثابتةٌ
   مكتوبةٌ هنا، ونصٌّ يُمرَّر، وزرّان.

   وهي مؤقّتةٌ بطبعها: تُحذف حين يستقرّ التطبيق. وُضعت لأنّ «يفتح
   ويُغلق فوراً» ليست معلومةً يُشخَّص بها شيء.
══════════════════════════════════════════════════════════════ */

private val Paper  = Color(0xFFF7F2E6)
private val Ink    = Color(0xFF1A1A17)
private val InkMed = Color(0xFF6B6455)
private val Rust   = Color(0xFF8C3B2E)
private val Card   = Color(0xFFFFFDF7)

@Composable
fun CrashReportScreen(report: String) {
    val ctx = LocalContext.current
    var dismissed by remember { mutableStateOf(false) }

    // بعد التجاهل: ورقةٌ خالية. والفتحُ التالي يبدأ التطبيقَ سليماً.
    if (dismissed) {
        Box(Modifier.fillMaxSize().background(Paper))
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
    ) {
        Text("توقّف التطبيق", color = Rust, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "هذا سببُ التوقّف. أرسِله لي وسأُصلحه — لا حاجة أن تصفه بكلماتك.",
            color = InkMed, fontSize = 14.sp,
        )

        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Card)
                .border(1.dp, Color(0x1A8C6B2E), RoundedCornerShape(14.dp))
                .padding(12.dp),
        ) {
            Text(
                report,
                color = Ink,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                // أثرُ النداءات يُقرأ بخطٍّ ثابت العرض لا غير.
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn("أرسِل التقرير", Rust, Color.White, Modifier.weight(1f)) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "رفيق الذكر — تقرير توقّف")
                    putExtra(Intent.EXTRA_TEXT, report)
                }
                // بلا تطبيقِ مشاركةٍ لا يسقط: التقريرُ معروضٌ فوقُ ويُنسخ بالضغط.
                runCatching { ctx.startActivity(Intent.createChooser(send, "أرسِل التقرير")) }
            }
            Btn("تجاهُل ومتابعة", Card, Ink, Modifier.weight(1f)) {
                CrashLog.clear(ctx)
                dismissed = true
            }
        }
    }
}

@Composable
private fun Btn(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, Color(0x1A8C6B2E), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = fg, fontWeight = FontWeight.Bold,
             fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
