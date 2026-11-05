package app.rafiqaldhikr.ui.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * شاشةٌ تطلب الورقةَ خالصةً — بلا شريطٍ سفليّ.
 *
 * وُجد للمصحف. تصميمُ الشاشة كلُّه قائمٌ على أن تكون ورقةً لا شيء فوقها
 * ولا تحتها، ولمسةٌ واحدةٌ في متنها تُظهر الأدوات وتُخفيها — ثمّ يبقى
 * شريطُ التنقّل ظاهراً دائماً فوقها فيبطل المعنى، ويقتطع من ارتفاع
 * الورقة ما يضيّق الأسطرَ الخمسة عشر.
 *
 * ولا يُحذف الشريطُ حذفاً: المصحفُ تبويبٌ رئيسيّ، ومن لا شريطَ عنده لا
 * سبيل له إلى بقيّة التطبيق. فيختفي مع الأدوات ويعود معها — ضغطةٌ واحدةٌ
 * على الورقة تُرجعه.
 */
val LocalImmersive: androidx.compose.runtime.ProvidableCompositionLocal<MutableState<Boolean>> =
    compositionLocalOf { mutableStateOf(false) }

/* ══════════════════════════════════════════════════════════════
   الورقةُ كاملةً — بلا أشرطة النظام

   المصحفُ الورقيُّ صفحةٌ تامّة، لا يقتطع من أسفلها مثلّثٌ ودائرةٌ
   ومربّع. وكان شريطُ تنقّل النظام يبقى فوق الورقة دائماً فيأكل من
   أسفلها نحو أربعٍ وعشرين نقطة — ويقصّ سطرَ «الصفحة · الحزب» الذي
   يُطبع في ذيل كل صفحةٍ مصحفية.

   فتُخفى أشرطةُ النظام ما دامت شاشةُ المصحف قائمة، وتعود كما كانت
   عند مغادرتها. ولا يُحبَس المستخدم: `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`
   يُظهرها بسحبةٍ من الحافّة متى شاء.

   **وظهورُها العابر لا يُغيّر مقاسَ الورقة**: الأشرطةُ العابرة تُرسم
   طبقةً فوق المحتوى ولا تدخل في حساب الحواف — وهذا هو الشرط، لأنّ
   الصفحةَ المصحفية تشتقّ مقاسَ خطّها من الارتفاع المتاح، فأيُّ تغيّرٍ
   فيه يُعيد رسمَ النصّ أمام عينَي القارئ.
══════════════════════════════════════════════════════════════ */

@androidx.compose.runtime.Composable
fun FullBleedReading() {
    val view = androidx.compose.ui.platform.LocalView.current
    if (view.isInEditMode) return
    val window = view.context.findActivity()?.window ?: return

    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(window, view, owner) {
        val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
        val bars = androidx.core.view.WindowInsetsCompat.Type.systemBars()

        fun hide() {
            controller.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(bars)
        }
        hide()

        /*  ويُعاد الإخفاءُ عند كل عودةٍ من الخلفية.
         *
         *  النظامُ يستعيد أشرطتَه حين يُستأنف النشاط — فمن خرج إلى تطبيقٍ
         *  آخر ثمّ رجع، وجد المثلّثَ والدائرةَ والمربّعَ فوق ورقته ثانيةً،
         *  ونداءُ `hide()` مرّةً واحدةً عند التركيب لا يمنع ذلك. */
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) hide()
        }
        owner.lifecycle.addObserver(observer)

        onDispose {
            owner.lifecycle.removeObserver(observer)
            controller.show(bars)
        }
    }
}

/**
 * ‏`LocalView.current.context` ليس النشاطَ دائماً — هو غلافُ سياقٍ
 * مركَّب. فيُفكّ حتى يُبلَغ النشاط، وإلّا فـnull ولا شيء يقع.
 */
private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity     -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
