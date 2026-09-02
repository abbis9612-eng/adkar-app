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

// حُذفت `moonName`: أسماءُ الأطوار الثمانية مكتوبةٌ بالعربية ولا يناديها
// أحد. والقمرُ يُرسم في `SkyCanvas` بطوره لا باسمه.

private fun Double.mod(m: Double): Double = ((this % m) + m) % m

/* ── موضعُ القمر ───────────────────────────────────────────────

   الطورُ وحدَه لا يكفي: كان القمرُ يُرسم في مكانٍ ثابتٍ من الشاشة، فلا
   يطلع ولا يغيب، ويبقى معلَّقاً في السماء والشمسُ في كبد السماء.

   وهذا حسابٌ منخفضُ الدقّة من خوارزمية Meeus المختصرة — خطؤه دون نصف
   درجة، وهو أدقُّ بكثيرٍ ممّا يحتاجه قرصٌ قطرُه ستُّ نقاطٍ على الشاشة.
   ولا شبكةَ فيه ولا حزمة: الحسابُ نفسُه الذي تقوم عليه المواقيت.
──────────────────────────────────────────────────────────────── */

/** ارتفاعُ القمر وسَمْتُه بالدرجات، وزاويةُ ميلِ الهلال. */
data class MoonPosition(
    val altitude: Double,
    val azimuth: Double,
    /**
     * زاويةُ وضع الحدّ المضيء: اتّجاهُ قرن الهلال في السماء.
     *
     * وهي التي تجعل هلالَ بغداد يميل غيرَ ميل هلال خطّ الاستواء —
     * وكان الهلالُ يُرسم عمودياً دائماً في كل بلد.
     */
    val brightLimbAngle: Double,
)

fun moonPosition(epochMillis: Long, lat: Double, lng: Double): MoonPosition {
    val d = epochMillis / 86_400_000.0 - 10957.5      // أيّامٌ منذ J2000.0

    //  إحداثيّاتُ القمر الكسوفية — الحدودُ الكبرى وحدَها
    val L = (218.316 + 13.176396 * d) * DEG           // الطولُ الوسطيّ
    val M = (134.963 + 13.064993 * d) * DEG           // الشذوذُ الوسطيّ
    val F = (93.272 + 13.229350 * d) * DEG            // مسافةُ العقدة

    val lonEc = L + 6.289 * DEG * sin(M)
    val latEc = 5.128 * DEG * sin(F)

    val obl = (23.439 - 0.0000004 * d) * DEG
    val ra = atan2(sin(lonEc) * cos(obl) - tan(latEc) * sin(obl), cos(lonEc))
    val dec = asin(sin(latEc) * cos(obl) + cos(latEc) * sin(obl) * sin(lonEc))

    val gmst = (18.697374558 + 24.06570982441908 * d).mod(24.0)
    val lst = ((gmst + lng / 15.0).mod(24.0)) * 15.0 * DEG
    val ha = lst - ra
    val latR = lat * DEG

    val alt = asin(sin(latR) * sin(dec) + cos(latR) * cos(dec) * cos(ha))
    val az = atan2(-sin(ha), tan(dec) * cos(latR) - sin(latR) * cos(ha))

    //  زاويةُ الحدّ المضيء تُقاس من شمال الأفق نحو الشرق، ثمّ تُحوَّل
    //  إلى ميلٍ على الشاشة بطرح زاوية الوضع المتوازي.
    val sun = sunPosition(epochMillis, lat, lng)
    val sunAz = sun.azimuth * DEG
    val sunAlt = sun.altitude * DEG
    val dAz = sunAz - (az / DEG + 360.0).mod(360.0) * DEG
    val limb = atan2(
        cos(sunAlt) * sin(dAz),
        cos(alt) * sin(sunAlt) - sin(alt) * cos(sunAlt) * cos(dAz),
    )

    return MoonPosition(
        altitude = alt / DEG,
        azimuth = (az / DEG + 360.0).mod(360.0),
        brightLimbAngle = limb / DEG,
    )
}
