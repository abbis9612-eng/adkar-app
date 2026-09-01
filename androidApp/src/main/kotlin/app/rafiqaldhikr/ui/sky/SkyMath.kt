package app.rafiqaldhikr.ui.sky

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/* ══════════════════════════════════════════════════════════════
   حسابُ السماء — أربعون سطراً، بلا شبكةٍ ولا صورةٍ ولا حزمة.

   السماءُ في الرئيسية ليست خلفيةً مرسومة: موضعُ الشمس يُحسب من خطِّ
   العرض والطول والدقيقة، وهو الحسابُ نفسه الذي تُشتقّ منه المواقيت
   (الفجرُ ارتفاعٌ ‎−١٨°، والمغربُ ‎−٠٫٨٣°). فما يُرسم هو البياناتُ
   نفسُها معروضةً، لا زينةً فوقها.

   ورسمُ مسجدٍ ثابتٍ هو نفسُه في السويد وفي عُمان، وفي الفجر وفي
   العشاء. وهذه تختلف بالمدينة وباليوم وبالدقيقة.

   المرجع: NOAA Solar Calculator مبسَّطاً — دقّتُه دقائقُ قوسية،
   وهي أدقُّ بكثيرٍ ممّا يحتاجه رسمٌ عرضُه ٣٦٠dp.
══════════════════════════════════════════════════════════════ */

private const val DEG = PI / 180.0

/** ارتفاعُ الشمس وسَمْتُها بالدرجات. [altitude] موجبٌ فوق الأفق. */
data class SunPosition(val altitude: Double, val azimuth: Double)

/**
 * @param epochMillis لحظةُ الحساب · @param lat خطُّ العرض · @param lng خطُّ الطول
 */
fun sunPosition(epochMillis: Long, lat: Double, lng: Double): SunPosition {
    // أيّامٌ منذ J2000.0 — والمعامل 10957.5 فرقُ يوليان بين حقبة يونكس وJ2000
    val n = epochMillis / 86_400_000.0 - 10957.5

    val meanLong  = (280.460 + 0.9856474 * n).mod(360.0)
    val meanAnom  = (357.528 + 0.9856003 * n).mod(360.0) * DEG
    val ecliptic  = (meanLong + 1.915 * sin(meanAnom) + 0.020 * sin(2 * meanAnom)) * DEG
    val obliquity = (23.439 - 0.0000004 * n) * DEG

    val declination = asin(sin(obliquity) * sin(ecliptic))
    val rightAsc    = atan2(cos(obliquity) * sin(ecliptic), cos(ecliptic))

    val gmst      = (18.697374558 + 24.06570982441908 * n).mod(24.0)
    val siderial  = ((gmst + lng / 15.0).mod(24.0)) * 15.0 * DEG
    val hourAngle = siderial - rightAsc
    val latRad    = lat * DEG

    val alt = asin(
        sin(latRad) * sin(declination) +
            cos(latRad) * cos(declination) * cos(hourAngle),
    ) / DEG
    val az = atan2(
        -sin(hourAngle),
        tan(declination) * cos(latRad) - sin(latRad) * cos(hourAngle),
    ) / DEG

    return SunPosition(alt, (az + 360.0).mod(360.0))
}

/* ── القمر ──────────────────────────────────────────────────────

   طورُه من عمره في الشهر الاقترانيّ. وهذا ما يجعل ليلةَ استهلال
   رمضان تعرض الهلالَ الذي فوق رأس صاحبه تلك الليلة، لا رسمَ هلالٍ
   ثابتٍ كالذي في كلِّ تطبيق.
──────────────────────────────────────────────────────────────── */

/** طولُ الشهر الاقترانيّ بالأيّام. */
private const val SYNODIC = 29.530588853

/** محاقٌ مرجعيّ: 2000-01-06 18:14 UTC. */
private const val NEW_MOON_EPOCH = 947_182_440_000L

/**
 * @param age عمرُ القمر بالأيّام · [phase] صفرٌ محاقاً ونصفٌ بدراً ·
 *   [illumination] نسبةُ القرص المضاء · [waxing] متزايدٌ أم متناقص.
 */
data class MoonPhase(
    val age: Double,
    val phase: Double,
    val illumination: Double,
    val waxing: Boolean,
)

fun moonPhase(epochMillis: Long): MoonPhase {
    val age = ((epochMillis - NEW_MOON_EPOCH) / 86_400_000.0).mod(SYNODIC)
    val phase = age / SYNODIC
    return MoonPhase(
        age = age,
        phase = phase,
        illumination = (1 - cos(2 * PI * phase)) / 2,
        waxing = phase < 0.5,
    )
}

/** اسمُ الطور — ثمانيةٌ كما هي في كلام العرب. */
fun moonName(p: MoonPhase): String = when ((p.phase * 8 + 0.5).toInt() % 8) {
    0 -> "محاق"; 1 -> "هلالٌ أوّل"; 2 -> "تربيعٌ أوّل"; 3 -> "أحدبُ متزايد"
    4 -> "بدر";  5 -> "أحدبُ متناقص"; 6 -> "تربيعٌ أخير"; else -> "هلالٌ أخير"
}

private fun Double.mod(m: Double): Double = ((this % m) + m) % m
