package app.rafiqaldhikr.ui.components

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * طلبُ موقعٍ صالحٍ للاستعمال — لا قراءةُ مخزونٍ قد يكون فارغاً.
 *
 * ═══ العطل الذي أصلحته هذه الدالّة ═══
 * كان التطبيق يستعمل `client.lastLocation` وحده في خمسة مواضع.
 * وlastLocation يقرأ آخر موقعٍ خزّنه النظام، **ولا يطلب موقعاً**. فإن
 * لم يطلب أيُّ تطبيقٍ موقعاً مؤخّراً — أو بعد إعادة تشغيل الهاتف، أو
 * على جهازٍ جديد، أو على جهازٍ يقتصد الطاقة — يُرجع null.
 *
 * وكان الكود عندها يعامل الـnull معاملةَ الرفض: يعرض «تعذّر الحصول على
 * الموقع… امنح الإذن» لمستخدمٍ منح الإذن فعلاً. فيضغط ويضغط ولا يحدث
 * شيء، ولا يفهم لماذا.
 *
 * ═══ الترتيب هنا ═══
 * ١) المخزون أوّلاً — فوريّ وبلا استهلاك، ويكفي في أغلب الأحيان.
 * ٢) فإن كان فارغاً: طلبُ موقعٍ جديد فعلاً بـgetCurrentLocation.
 *
 * وPRIORITY_BALANCED_POWER_ACCURACY لا PRIORITY_HIGH: حسابُ مواقيت
 * الصلاة لا يحتاج دقّة المتر — بضع مئاتٍ من الأمتار لا تغيّر دقيقةً
 * واحدة. والمتوازنة تعمل بالشبكة والواي-فاي داخل المباني حيث لا يصل
 * GPS، وهي حالةُ أكثر من يفتح تطبيق أذكار.
 *
 * (ولا علاقة لهذا بإذن الإنترنت: تحديدُ الموقع بالشبكة يمرّ عبر خدمة
 *  النظام لا عبر اتّصال التطبيق — فيعمل والتطبيق بلا إذن إنترنت.)
 *
 * @param onResult يُستدعى دائماً — بموقعٍ أو بـnull. لا مسارَ صامت.
 */
@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.requestUsableLocation(onResult: (Location?) -> Unit) {
    fun fresh() {
        getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token,
        )
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }
    lastLocation
        .addOnSuccessListener { cached -> if (cached != null) onResult(cached) else fresh() }
        .addOnFailureListener { fresh() }
}
