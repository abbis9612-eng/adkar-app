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
