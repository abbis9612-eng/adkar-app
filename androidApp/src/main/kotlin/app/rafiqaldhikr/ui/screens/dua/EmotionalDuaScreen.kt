package app.rafiqaldhikr.ui.screens.dua

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

/* ═══════════════════════════════════════════════════════════════════
   مؤجَّلة — نصوص دعاء بلا مصدر

   الأدعية الستّة أدناه مكتوبة في هذا الملف بلا source ولا source_grade،
   وتُعرض بخطّ النسخ ١٨sp تماماً كما تُعرض الأدعية الموثَّقة — فيراها
   المستخدم بنفس الحجّية وهي بلا إسناد. وبعضها صيغة مختصرة من دعاء أطول.

   قاعدة AGENTS.md: المحتوى الإسلامي من مصادر موثَّقة فقط، ولا يُختصر
   ولا يُعاد صوغه. وكل دعاء آخر في التطبيق يمرّ من Dua في قاعدة البيانات
   بحقلَي المصدر والدرجة، ويُعرضان في DuaListScreen.

   الطريق إلى إعادتها: نقل النصوص إلى duas.json بمصدر ودرجة لكلٍّ منها
   من حصن المسلم أو الأذكار للنووي، فتخضع لحارس check_religious_sources.
   لم أفعل ذلك لأن إسناد نصّ ديني قرارٌ لا يُتّخذ من طرفي.
═══════════════════════════════════════════════════════════════════ */
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
            RafiqTopBar(
                title  = stringResource(R.string.emotional_title),
                onBack = {navController.popBackStack()},
            )

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
                            .rafiqCard()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 32.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(label,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rc.emerald, style = RafiqType.titleM)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                dua,
                                fontSize  = 18.sp,
                                fontFamily = app.rafiqaldhikr.ui.theme.NaskhFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 34.sp,
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
