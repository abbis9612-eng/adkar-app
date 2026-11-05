package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.BorderActive
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape

/**
 * سطح البطاقة الموحّد.
 *
 * كان كل صفّ وبطاقة في التطبيق يعيد كتابة نفس السلسلة يدوياً
 * (`clip` + `background(rc.card)` + `border`)، أحياناً بظلّ وأحياناً
 * بحدّ وأحياناً بالاثنين، وبنصف قطر مختلف كل مرة. هذا المُعدِّل هو
 * المصدر الوحيد لهيئة البطاقة بعد اليوم.
 *
 * ═══ الحدُّ والظلُّ يرسمانها، لا التعبئة ═══
 *
 * الورق #FFFDF5 شبه أبيض، والبطاقة بيضاءُ نقيّة — نبرتُها عنه 1.018،
 * أي غير مرئية بالتعبئة إطلاقاً. وكان الحلّ السابق تعبئةً رمادية
 * (#EAE3D2) تُرى فعلاً، لكنها تهبط بالنصّ من 17.56 إلى 13.98 وتُقرأ
 * رقعةً ملصوقة على الورق.
 *
 * فصارت التعبئة بيضاء (النصّ فوقها 17.89 — أعلى من الورق نفسه)،
 * ويرسمها حدٌّ بنبرة 1.24 وظلٌّ ناعم من طبقتين: قريبةٌ تفصلها عن
 * الورق، وبعيدةٌ ترفعها. وهذا ما تفعله iOS 17 وMaterial 3 المحدَّثة.
 *
 * والحدُّ من [RafiqPalette.cardBorder] لا من gold: الذهبي صار عنبريّاً
 * فبدا الحدّ لهجةً لا حافّة.
 *
 * @param active الحالة الوحيدة التي يلمع فيها الحدّ (البطاقة الحاضرة الآن).
 */
fun Modifier.rafiqCard(
    active: Boolean = false,
    shape:  Shape = RafiqShape.card,
): Modifier = composed {
    val rc = LocalRafiqColors.current
    this
        .shadow(
            elevation = if (active) 6.dp else 3.dp,
            shape = shape,
            ambientColor = ShadowTint,
            spotColor    = ShadowTint,
        )
        .clip(shape)
        .background(rc.card)
        .border(
            1.dp,
            if (active) rc.emeraldFill.copy(alpha = BorderActive * 3f) else rc.cardBorder,
            shape,
        )
}

/** ظلٌّ دافئ لا رمادي — الرماديّ على ورقٍ كريميّ يُقرأ اتّساخاً. */
private val ShadowTint = androidx.compose.ui.graphics.Color(0xFF4A3A16)


/*  حُذف من هنا مكوّنان لا يناديهما سطرٌ واحد في التطبيق:
 *
 *  · `Modifier.rafiqGlass` — كُتب «للبطاقات السينمائية» ولا وجود لها.
 *  · `RafiqCard` المركَّب — كلُّ الشاشات تستعمل `Modifier.rafiqCard`
 *    مباشرةً، وهو الصواب لأنّه يعمل مع Row وBox لا Column وحدَه.
 *
 *  ومكوّنٌ موجودٌ بلا مستدعٍ ليس محايداً: يُقرأ خياراً متاحاً فيُستعمل
 *  في مكانٍ بعد شهر، فيصير في التطبيق نمطان لبطاقةٍ واحدة.
 */
