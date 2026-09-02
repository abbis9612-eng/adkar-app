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
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
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
    /*  سجلٌّ يصف ما في التطبيق، لا ما نُوي فيه.
     *
     *  كان يعلن سبعَ ميزاتٍ لا يصل إليها المستخدم: «وضع رمضان» و«التنفس
     *  والذكر» و«الحديقة الروحية» و«بطاقات المشاركة» و«التقرير الأسبوعي»
     *  و«إعدادات الـWidgets» — كلُّها موسومةٌ `@HiddenInV1` بلا مدخل —
     *  و«مشغّل صوت القرآن مع اختيار القارئ» وقد حُذف من المستودع كلِّه.
     *  فيقرأ المستخدم عن ميزةٍ ثمّ يبحث عنها فلا يجدها، ويظنّ التطبيق
     *  معطّلاً عنده.
     *
     *  وقد حُسمت الستُّ بعدُ: أربعٌ أُصلحت ورُفع إخفاؤها وصار لها مدخلٌ
     *  في «أوراقي» (التنفّس · الحديقة · المشاركة · التقرير)، وثلاثٌ
     *  حُذفت من المستودع (رمضان · إعدادات الودجت · الدعاء الشعوري).
     *  فما يُعلَن هنا اليومَ يوجد كلُّه ويُوصَل إليه.
     *
     *  والنسخةُ تُقرأ من البناء لا تُكتب يدوياً: كانت `1.2.0` ثابتةً
     *  هنا و`BuildConfig.VERSION_NAME` في شاشة «حول» — فيقرأ الرقمين
     *  مختلفَين في تطبيقٍ واحد.
     */
    val changelog = listOf(
        ChangelogEntry(
            app.rafiqaldhikr.BuildConfig.VERSION_NAME,
            stringResource(R.string.whatsnew_current_label),
            listOf(
                stringResource(R.string.whatsnew_mushaf),
                stringResource(R.string.whatsnew_search),
                stringResource(R.string.whatsnew_quran_text),
                stringResource(R.string.whatsnew_alarms),
                stringResource(R.string.whatsnew_qibla),
                stringResource(R.string.whatsnew_a11y),
            ),
            isLatest = true,
        ),
        ChangelogEntry(
            "1.0.0",
            stringResource(R.string.whatsnew_first_label),
            listOf(
                stringResource(R.string.whatsnew_first_release),
                stringResource(R.string.whatsnew_quran),
                stringResource(R.string.whatsnew_tasbeeh),
                stringResource(R.string.whatsnew_adhkar),
                stringResource(R.string.whatsnew_times),
                stringResource(R.string.whatsnew_profile),
                stringResource(R.string.whatsnew_theme),
            ),
        ),
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
                title  = stringResource(R.string.settings_whats_new),
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
