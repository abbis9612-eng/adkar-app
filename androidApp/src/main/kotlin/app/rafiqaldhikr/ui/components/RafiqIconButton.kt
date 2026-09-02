package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape

/**
 * زرّ أيقونة مربّع — كان مكرّراً باسم `PillBtn` في خمس شاشات بخمس نسخ
 * شبه متطابقة، فلم يكن أيّ إصلاح فيه يصل إلى كلّها.
 *
 * الشكل 40dp لكن مساحة اللمس 48dp عبر [minimumInteractiveComponentSize] —
 * الحدّ الأدنى الواجب مهما صغُر الشكل البصري.
 *
 * @param label وصف الزرّ لقارئ الشاشة. مطلوب: زرّ أيقونة بلا وصف هو
 *        زرّ لا وجود له عند من يستعمل TalkBack.
 */
@Composable
fun RafiqIconButton(
    onClick:  () -> Unit,
    label:    String,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit,
) {
    val rc = LocalRafiqColors.current
    /*  سطحٌ زجاجيّ لا مربّعٌ مصمت.
     *
     *  كان rc.card — أي رقعةً معتمة بلون البطاقة، فتبدو مربّعاً رمادياً
     *  ملصوقاً على الورق. والزجاج في أندرويد بلا طمسٍ حقيقي (RenderEffect
     *  من API 31 فقط، وminSdk هنا 23) يُبنى بطبقتين: حبرٌ شديد الشفافية
     *  يعطي عمقاً، وحدٌّ شعرة أوضح منه قليلاً يرسم الحافّة.
     *
     *  والشفافية تعني أن الزرّ يأخذ لون ما تحته مهما كان — فيعمل على
     *  الورق وعلى البطاقة وعلى الزمرّد سواء، ولا يحتاج نسخةً لكل سطح.
     */
    Box(
        modifier
            .size(40.dp)
            .clip(RafiqShape.item)
            .background(rc.ink.copy(alpha = 0.045f))
            .border(1.dp, rc.ink.copy(alpha = 0.10f), RafiqShape.item)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .semantics {
                contentDescription = label
                // بلا Role يقرؤه TalkBack نصّاً لا زرّاً، فلا يقول «انقر مرّتين».
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) { content() }
}
