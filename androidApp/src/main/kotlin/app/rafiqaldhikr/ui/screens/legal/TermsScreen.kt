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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.RafiqBackButton

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "شروط الاستخدام",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

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
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    SectionTitle("القبول", rc)
                    SectionBody("باستخدامك لتطبيق رفيق الذكر فإنك توافق على هذه الشروط. إذا لم توافق على أي جزء منها، يُرجى عدم استخدام التطبيق.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("الاستخدام المسموح", rc)
                    SectionBody("""
                        • استخدام التطبيق للعبادة والتقرب إلى الله
                        • مشاركة محتوى التطبيق مع الآخرين
                        • الاستخدام الشخصي غير التجاري
                    """.trimIndent(), rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("المحتوى الديني", rc)
                    SectionBody("نحرص على دقة المحتوى الديني من قرآن كريم وأحاديث وأذكار. جميع الأحاديث مُخرَّجة من مصادر موثوقة. مع ذلك، ننصح بالتأكد من أي حكم شرعي مع عالم متخصص.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("مواقيت الصلاة", rc)
                    SectionBody("مواقيت الصلاة المحسوبة في التطبيق تقريبية ومبنية على حسابات فلكية. ننصح دائماً بالتأكد من مسجدك المحلي.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("الملكية الفكرية", rc)
                    SectionBody("جميع الحقوق محفوظة لفريق رفيق الذكر. النصوص القرآنية والأحاديث ملك للأمة الإسلامية.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("إخلاء المسؤولية", rc)
                    SectionBody("التطبيق مُقدَّم كما هو دون ضمانات. لا نتحمل مسؤولية أي أضرار ناتجة عن استخدام التطبيق.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("التعديلات", rc)
                    SectionBody("نحتفظ بحق تعديل هذه الشروط. ستُعلَم بأي تغييرات جوهرية عبر التطبيق.", rc)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, rc: RafiqPalette) {
    Spacer(Modifier.height(16.dp))
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun SectionBody(text: String, rc: RafiqPalette) {
    Text(text, fontSize = 14.sp, color = rc.inkMed, lineHeight = 22.sp)
}
