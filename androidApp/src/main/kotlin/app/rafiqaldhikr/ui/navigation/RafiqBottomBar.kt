package app.rafiqaldhikr.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.GlyphTrigram
import app.rafiqaldhikr.ui.components.GlyphLines
import app.rafiqaldhikr.ui.components.GlyphHouse
import app.rafiqaldhikr.ui.components.GlyphHeart
import app.rafiqaldhikr.ui.components.GlyphDotted
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.tourAnchor
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.progressSpec
import app.rafiqaldhikr.ui.theme.RafiqShape

data class BottomNavItem(
    val labelRes: Int,
    // أيقونة مخصصة من مكتبة RafiqIcons الموحّدة (حجم، لون)
    val icon:     @Composable (Dp, Color) -> Unit,
    val route:    RafiqRoute,
    /** مفتاحُ مرساة الجولة التعريفيّة — تُثقَب الشاشةُ فوق هذا التبويب. */
    val tourKey:  String,
)

@Composable
fun RafiqBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(R.string.nav_home,    { s, c -> GlyphHouse(s, c) },   RafiqRoute.Home, "nav_home"),
        // تبويبُ المصحف يفتح المصحف نفسَه لا قائمةَ السور: الصفحةُ هي
        // المقصود، والقائمةُ بابٌ داخلَها لمن أراد الانتقالَ بالسورة.
        BottomNavItem(R.string.nav_quran,   { s, c -> GlyphLines(s, c) },   RafiqRoute.Mushaf, "nav_quran"),
        BottomNavItem(R.string.nav_tasbeeh, { s, c -> GlyphDotted(s, c) },  RafiqRoute.AdhkarCategories, "nav_adhkar"),
        BottomNavItem(R.string.nav_dua,     { s, c -> GlyphHeart(s, c) },   RafiqRoute.DuaCategories, "nav_dua"),
        BottomNavItem(R.string.nav_profile, { s, c -> GlyphTrigram(s, c) }, RafiqRoute.Profile, "nav_profile"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val rc = LocalRafiqColors.current

    // كان السطح bg بشفافية 92% فوق bg — أي نفس لون الصفحة بالضبط، فلم
    // يفصلهما إلا ظلّ 12dp: لطخة رمادية بعرض الشاشة قرأتها العين لونين.
    // الآن ورقةٌ أفتح فوق الصفحة يفصلها خطّ مسطرة — وهي قاعدة م2 نفسها
    // (الفصل بالحدّ لا بالظلّ) مطبَّقةً على الشريط بدل استثنائه منها.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        // سطحٌ واحد: الورق يمتدّ من أعلى الشاشة إلى أسفلها، والفصلُ بالحدّ
        // الرفيع وحده. كان rc.card فيظهر الشريط لوحاً منفصلاً بلونٍ آخر.
        color = rc.bg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(rc.divider)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .navigationBarsPadding(),
                // weight(1f) لكل عنصر بدل SpaceEvenly: الأخير كان يتجاوز
                // نصيبه فيُقصّ عند حافة الشاشة («أوراقي» مقطوعة).
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route.route
                    BottomBarItemEnhanced(
                        modifier = Modifier.weight(1f),
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItemEnhanced(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current

    /*  رمزٌ **بجانب** الاسم على سطرٍ واحد — كما يرسمها النموذج:
     *  `<span>⌂ اليوم</span>`. وكانت الأيقونةُ فوق الاسم في حبّةٍ
     *  خضراءَ وتحته نقطةٌ ذهبيّةٌ والعنصرُ كلُّه يقفز عند الاختيار.
     *  ثلاثُ زينةٍ لا يعرفها النموذج، وكلُّها تقول ما يقوله اللونُ
     *  وحدَه: أين أنت. فبقي اللون.
     *
     *  والفرقُ الوحيدُ المقصود: ارتفاعٌ ثمانٍ وأربعون نقطةً حدّاً أدنى.
     *  النموذجُ صفحةٌ تُنقر بالفأرة، وهذا شريطٌ يُلمس بالإصبع — والمساحةُ
     *  لا تُقاس بحجم الحرف. وهي مساحةٌ غيرُ مرئيّةٍ حول الرمز والاسم،
     *  فالشكلُ كما هو. */
    val tint by animateColorAsState(
        targetValue = if (isSelected) rc.emerald else rc.inkLight,
        animationSpec = progressSpec(300),
        label = "navTint",
    )

    Row(
        modifier = modifier
            //  المرساةُ على الصفّ كلِّه لا على الأيقونة: الثقبُ يجب أن
            //  يُظهر الرمزَ والاسمَ معاً، فالاسمُ هو ما يُقرأ.
            .tourAnchor(item.tourKey)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.icon(12.dp, tint)
        Spacer(Modifier.width(5.dp))
        Text(
            stringResource(item.labelRes),
            style = RafiqType.metaS.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = tint,
            maxLines = 1,
        )
    }
}
