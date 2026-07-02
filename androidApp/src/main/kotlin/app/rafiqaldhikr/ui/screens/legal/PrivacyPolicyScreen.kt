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
fun PrivacyPolicyScreen(navController: NavHostController) {
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
                    text = "سياسة الخصوصية",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
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
                    SectionTitle("مقدمة", rc)
                    SectionBody("نحن في تطبيق رفيق الذكر نحترم خصوصيتك ونلتزم بحماية بياناتك الشخصية. توضح هذه السياسة كيفية جمع واستخدام وحماية معلوماتك.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("البيانات التي نجمعها", rc)
                    SectionBody("""
                        • بيانات الاستخدام: تقدم الأذكار، عدد التسبيح، صفحات القرآن المقروءة
                        • الموقع الجغرافي: لحساب مواقيت الصلاة واتجاه القبلة (بإذنك فقط)
                        • التفضيلات: إعدادات المظهر والخط والإشعارات
                        
                        جميع هذه البيانات تُخزَّن محلياً على جهازك فقط.
                    """.trimIndent(), rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("البيانات التي لا نجمعها", rc)
                    SectionBody("""
                        • لا نجمع معلومات شخصية كالاسم أو البريد الإلكتروني
                        • لا نتتبع نشاطك عبر تطبيقات أخرى
                        • لا نبيع أو نشارك بياناتك مع أطراف ثالثة
                    """.trimIndent(), rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("التخزين والأمان", rc)
                    SectionBody("تُخزَّن بياناتك بشكل مشفر على جهازك باستخدام EncryptedSharedPreferences وقاعدة بيانات SQLite محلية. لا تُرسل أي بيانات إلى خوادمنا إلا عند تفعيل ميزة المزامنة.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("حقوقك", rc)
                    SectionBody("يمكنك في أي وقت حذف جميع بياناتك من خلال إعدادات التطبيق > تصدير/حذف البيانات.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("التحديثات", rc)
                    SectionBody("قد نقوم بتحديث هذه السياسة من وقت لآخر. سنُعلمك بأي تغييرات جوهرية عبر إشعار داخل التطبيق.", rc)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.1f))

                    SectionTitle("التواصل", rc)
                    SectionBody("لأي استفسار حول الخصوصية: privacy@rafiqaldhikr.app", rc)
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
