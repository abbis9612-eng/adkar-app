package app.rafiqaldhikr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import app.rafiqaldhikr.R

/**
 * خط الواجهة الموحّد: Noto Sans Arabic — خط عربي اعتيادي واضح ومألوف،
 * يُستخدم لكل نصوص الواجهة والأرقام. (استبدل Cairo وIBM Plex).
 */
val UiFamily = FontFamily(
    Font(R.font.noto_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.noto_sans_arabic_medium,  FontWeight.Medium),
    Font(R.font.noto_sans_arabic_medium,  FontWeight.SemiBold),
    Font(R.font.noto_sans_arabic_bold,    FontWeight.Bold),
)

/** خط المصحف: Scheherazade New — نسخ قرآني واضح على الشاشات. */
val QuranFamily = FontFamily(
    Font(R.font.scheherazade_regular,  FontWeight.Normal),
    Font(R.font.scheherazade_semibold, FontWeight.Medium),
    Font(R.font.scheherazade_semibold, FontWeight.SemiBold),
    Font(R.font.scheherazade_bold,     FontWeight.Bold),
)

/**
 * خط الأذكار والأدعية المشكولة: Noto Naskh Arabic — الحركات مرتبة لا تتزاحم.
 * (يبقى مخصصاً للنص المشكول فقط، وباقي الواجهة على UiFamily).
 */
val NaskhFamily = FontFamily(
    Font(R.font.noto_naskh_regular,  FontWeight.Normal),
    Font(R.font.noto_naskh_semibold, FontWeight.Medium),
    Font(R.font.noto_naskh_semibold, FontWeight.SemiBold),
    Font(R.font.noto_naskh_bold,     FontWeight.Bold),
)

/** خط البسملة الزخرفي: Amiri — لعبارة البسملة فقط (نص قرآني قصير). */
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold,    FontWeight.Bold),
)

/* ═══════════════════════════════════════════════════════════════════
   ارتفاع السطر — القاعدة الأهم في الطباعة العربية

   الحروف العربية تمتد تحت الـbaseline وفوق الـcap line بشكل لا يفعله
   اللاتيني، والتشكيل يحتاج مساحة عمودية. المرجع: body عربي 1.7–1.85
   مقابل 1.5–1.6 لاتيني، وعناوين 1.3–1.4 مقابل 1.1–1.2.

   [ArabicLineHeight] هو الافتراضي المطبَّق على كل نص في التطبيق عبر
   RafiqTheme، بوحدة em حتى يتناسب مع أي مقاس يحدّده النص نفسه.

   [TrimmedLeading] يقصّ الفراغ الزائد فوق أول سطر وتحت آخر سطر فقط،
   فتبقى العناوين والتسميات ذات السطر الواحد بارتفاعها الضيّق كما هي،
   بينما تأخذ الفقرات المتعددة الأسطر كامل التنفّس بين سطورها.
   بدونه كان رفع الـline-height سينفخ ارتفاع كل تسمية في التطبيق.
═══════════════════════════════════════════════════════════════════ */

val ArabicLineHeight = 1.75.em

val TrimmedLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim      = LineHeightStyle.Trim.Both,
)

private fun arabic(
    family: FontFamily = UiFamily,
    weight: FontWeight = FontWeight.Normal,
    size:   Int,
    line:   Int,
    features: String? = null,
) = TextStyle(
    fontFamily          = family,
    fontWeight          = weight,
    fontSize            = size.sp,
    lineHeight          = line.sp,
    lineHeightStyle     = TrimmedLeading,
    fontFeatureSettings = features,
)

/* ═══════════════════════════════════════════════════════════════════
   RafiqType — سلّم الطباعة الموحّد

   كل نص في التطبيق يستعمل نمطاً مسمّى من هنا. النمط يحمل الخط والوزن
   والمقاس وارتفاع السطر معاً، فلا يُكتب `fontSize =` عارياً بعد اليوم.

   النِّسب مقصودة: النص المشكول أوسع (1.80–1.92) لأن الحركات تحتاج
   مساحة، والعناوين أضيق (1.38–1.50) لأنها سطر أو سطران.
═══════════════════════════════════════════════════════════════════ */

object RafiqType {

    /* ── النص الشرعي ── */

    /** المصحف — التشكيل العثماني يحتاج أوسع نسبة في التطبيق (1.92). */
    val quran = arabic(QuranFamily, FontWeight.Normal, 24, 46)

    /** نص الذكر والدعاء المشكول (1.80). */
    val dhikr = arabic(NaskhFamily, FontWeight.Normal, 20, 36)

    /** البسملة والآية المقتبسة القصيرة (1.62). */
    val ayah = arabic(AmiriFamily, FontWeight.Normal, 26, 42)

    /* ── الواجهة ── */

    /** عنوانُ شاشة الترحيب وحده — أميري عريض، أكبرُ كلامٍ في التطبيق.
        كان `ayah` (26) يُستعمل هناك فبدا العنوانُ اقتباساً في وسط فراغ:
        الشاشةُ نصفها خالٍ فوقه، والعينُ تقيس الحجم بما حوله لا بذاته. */
    val hero = arabic(AmiriFamily, FontWeight.Bold, 32, 48)

    /** الرقم الضخم — عدّاد المسبحة وحده. */
    val display = arabic(UiFamily, FontWeight.Bold, 32, 44, "tnum")

    /** الفعلُ الحاضر في الرئيسية — أكبر ما يُقرأ بعد الكلمة، وأصغر من
        [display] لأنه كلامٌ لا رقم. بينهما فجوة كانت تُسدّ بـtitleL فيبدو
        الفعل بحجم عنوان الشريط. */
    val titleXL = arabic(UiFamily, FontWeight.Bold, 26, 38)

    /** عنوان الشاشة في الشريط العلوي. */
    val titleL = arabic(UiFamily, FontWeight.Bold, 22, 32)

    /** عنوان بطاقة. */
    val titleM = arabic(UiFamily, FontWeight.Bold, 18, 27)

    /** وصفٌ تحت عنوانٍ ضخم — درجةٌ واحدة فوق [body]، لشاشة الترحيب.
        نصُّ 16 تحت عنوان 32 يبدو هامشاً لا جواباً. */
    val bodyL = arabic(UiFamily, FontWeight.Normal, 18, 32)

    /** النص العادي — الافتراضي (1.75). */
    val body = arabic(UiFamily, FontWeight.Normal, 16, 28)

    /** النص الثانوي (1.79). */
    val bodyS = arabic(UiFamily, FontWeight.Normal, 14, 25)

    /** عنوان قسم ونص زر. */
    val label = arabic(UiFamily, FontWeight.Medium, 15, 23)

    /** تسمية صغيرة. */
    val caption = arabic(UiFamily, FontWeight.Normal, 12, 20)

    /** شارة — الحدّ الأدنى المطلق للمقروئية العربية. */
    val micro = arabic(UiFamily, FontWeight.Medium, 11, 17)
}

/* ═══════════════════════════════════════════════════════════════════
   سلّم Material — يغذّي مكوّنات Material3 الجاهزة (الأزرار، الحقول،
   الشيتات) بنفس أرقام RafiqType حتى لا يوجد سلّمان متنافسان.
═══════════════════════════════════════════════════════════════════ */

val RafiqTypography = Typography(
    displayLarge  = arabic(UiFamily, FontWeight.Bold,   28, 40),
    headlineLarge = RafiqType.titleL,
    headlineSmall = arabic(UiFamily, FontWeight.Medium, 20, 30),
    titleLarge    = RafiqType.titleM,
    titleMedium   = arabic(UiFamily, FontWeight.Medium, 16, 26),
    bodyLarge     = RafiqType.body,
    bodyMedium    = RafiqType.bodyS,
    labelLarge    = arabic(UiFamily, FontWeight.Medium, 14, 22),
    labelSmall    = RafiqType.caption,
)

/**
 * نمط الأرقام (العدّادات، الأوقات، الإحصائيات): نفس خط الواجهة الاعتيادي
 * مع tnum — أرقام بعرض ثابت فلا تهتز أثناء العدّ.
 *
 * المقاس يحدّده الاستدعاء؛ ارتفاع السطر يتبع المقاس بنسبة عربية.
 */
val NumbersStyle = TextStyle(
    fontFamily          = UiFamily,
    fontWeight          = FontWeight.Bold,
    fontFeatureSettings = "tnum",
    lineHeight          = 1.30.em,
    lineHeightStyle     = TrimmedLeading,
)
