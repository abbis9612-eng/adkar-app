package app.rafiq.domain.usecase

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * اتّجاهُ القبلة.
 *
 * القيمُ المرجعية اتّجاهاتٌ معروفةٌ منشورة، لا مأخوذةٌ من هذه الدالّة —
 * فاختبارٌ يقارن الدالّةَ بنفسها لا يحرس شيئاً.
 *
 * والزاويةُ هنا من **الشمال الحقيقي**. وكانت البوصلةُ في التطبيق تقرأ من
 * الشمال المغناطيسي وتُقارَن بهذه مباشرةً، فتنحرف الإبرةُ بمقدار انحراف
 * المكان — أربعُ درجاتٍ إلى ستٍّ في العراق، وعشرٌ إلى عشرين في أمريكا.
 * التصحيحُ في `CompassManager` لا هنا، وهذا الاختبار يُثبّت الطرفَ الذي
 * يُقاس عليه.
 *
 * أسماءُ الدوال ASCII: العربيةُ بين backticks تُنتج ملفات `.class` بأسماء
 * عربية، وتفشل على محلّيةٍ غير UTF-8.
 */
class CalculateQiblaUseCaseTest {

    private val qibla = CalculateQiblaUseCase()

    /** يُقبل فرقٌ درجتين — المراجعُ المنشورة تختلف بينها بأقلَّ من ذلك. */
    private fun assertBearing(lat: Double, lng: Double, expected: Double, where: String) {
        val actual = qibla(lat, lng).toDouble()
        val diff = abs(((actual - expected + 540) % 360) - 180)
        assertTrue(diff <= 2.0, "$where: توقّعنا ~$expected° فجاء $actual° (فرق $diff°)")
    }

    /** القاهرة — القبلةُ جنوبٌ شرقيّ قليلاً. */
    @Test
    fun cairo() = assertBearing(30.0444, 31.2357, 136.0, "القاهرة")

    /** لندن — شرقٌ مائلٌ إلى الجنوب. */
    @Test
    fun london() = assertBearing(51.5074, -0.1278, 118.9, "لندن")

    /** جاكرتا — غربٌ مائلٌ إلى الشمال، فهي شرقَ مكّة. */
    @Test
    fun jakarta() = assertBearing(-6.2088, 106.8456, 295.1, "جاكرتا")

    /** نيويورك — شمالٌ شرقيّ، وهو ما يفاجئ من يظنّها جنوباً شرقياً. */
    @Test
    fun newYork() = assertBearing(40.7128, -74.0060, 58.5, "نيويورك")

    /** السليمانية — بلدُ أوّل مستعملي التطبيق. */
    @Test
    fun sulaymaniyah() = assertBearing(35.5558, 45.4436, 200.9, "السليمانية")

    /** كلُّ اتّجاهٍ داخل المدى ‎[0، 360). */
    @Test
    fun alwaysInRange() {
        for (lat in -80..80 step 20) {
            for (lng in -180..170 step 30) {
                val b = qibla(lat.toDouble(), lng.toDouble())
                assertTrue(b >= 0f && b < 360f, "خارج المدى عند $lat,$lng: $b")
            }
        }
    }

    /** من مكّة نفسِها لا معنى للاتّجاه — لكنّه لا يرمي ولا يُخرج NaN. */
    @Test
    fun atMeccaDoesNotThrow() {
        val b = qibla(21.4225, 39.8262)
        assertTrue(!b.isNaN(), "أعطى NaN عند الكعبة نفسِها")
    }
}
