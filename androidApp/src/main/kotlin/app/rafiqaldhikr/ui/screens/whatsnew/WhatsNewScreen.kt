package app.rafiqaldhikr.ui.screens.whatsnew

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
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar

data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>,
    val isLatest: Boolean = false
)

@Composable
fun WhatsNewScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current
    val changelog = listOf(
        ChangelogEntry("1.2.0", "رمضان 1447", listOf(
            "وضع رمضان — برنامج يومي مخصص",
            "التنفس والذكر — تمارين تأمل مع الأذكار",
            "الحديقة الروحية — نبتة تنمو بأعمالك",
            "نظام الإنجازات",
            "التقرير الأسبوعي التفصيلي",
            "بطاقات المشاركة",
            "ذكر مخصص",
            "سياسة الخصوصية وشروط الاستخدام"
        ), isLatest = true),
        ChangelogEntry("1.1.0", "محرم 1447", listOf(
            "مشغل صوت القرآن مع اختيار القارئ",
            "تفسير الآيات",
            "متابعة الصلاة اليومية",
            "إعدادات الـ Widgets",
            "تصدير وإدارة البيانات"
        )),
        ChangelogEntry("1.0.0", "ذو الحجة 1446", listOf(
            "الإصدار الأول!",
            "القرآن الكريم مع البحث والعلامات",
            "المسبحة الإلكترونية",
            "الأدعية والأذكار",
            "مواقيت الصلاة واتجاه القبلة",
            "الإحصائيات والملف الشخصي",
            "المظهر الداكن والفاتح",
            "خاطرة اليوم"
        ))
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
            RafiqTopBar(
                title  = "ما الجديد",
                onBack = {navController.popBackStack()},
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                changelog.forEach { entry ->
                    ChangelogCard(entry, rc)
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry, rc: RafiqPalette) {
    val borderColor = if (entry.isLatest) rc.emerald.copy(alpha = 0.3f) else rc.gold.copy(alpha = 0.08f)
    val bgColor = if (entry.isLatest) rc.cardPrayed else rc.card
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(bgColor)
            .border(1.dp, borderColor, RafiqShape.card)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("v${entry.version}",
                    fontWeight = FontWeight.Bold,
                    color = rc.ink, style = RafiqType.titleM)
                if (entry.isLatest) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(RafiqShape.item)
                            .background(rc.emeraldFill)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("الأحدث", fontWeight = FontWeight.Bold, color = rc.onEmeraldFill, style = RafiqType.micro)
                    }
                }
            }
            Text(entry.date,
                color = rc.inkMed, style = RafiqType.caption)
        }
        
        Spacer(Modifier.height(16.dp))
        
        entry.changes.forEach { change ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .padding(top = 7.dp)
                        .size(6.dp)
                        .clip(RafiqShape.chip)
                        .background(rc.gold)
                )
                Text(change,
                    color = rc.inkMed,
                    lineHeight = 22.sp, style = RafiqType.bodyS)
            }
        }
    }
}
