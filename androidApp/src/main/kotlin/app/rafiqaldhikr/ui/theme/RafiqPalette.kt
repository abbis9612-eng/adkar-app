package app.rafiqaldhikr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * لوحة التوكنز الموحّدة لتطبيق «رفيق الذكر».
 *
 * كل شاشة تقرأ من [LocalRafiqColors] ولا تعرّف ألواناً خاصة بها.
 * اختصار: `val rc = LocalRafiqColors.current`
 *
 * ── الانضباط اللوني ─────────────────────────────────────────────
 * كانت اللوحة 48 توكناً و12 لون أساس (زمرّدي · ذهبي · بنفسجي ·
 * بنفسجي-نوم · نيلي · برتقالي · بنّي · أزرق + أربع حلقات دلالية).
 * هذه ليست هويّة، هذه قوس قزح — وتطبيقٌ عن السكينة لا يملك ستّ درجات
 * تمييز.
 *
 * الآن ثلاث عائلات لا غير:
 *
 *  ١) الهويّة   : زمرّدي · ذهبي · ورق · حبر
 *  ٢) سلّم الضوء: goldLight (فجر وضحى) · lightDusk (عصر ومغرب)
 *                 · lightNight (عشاء ونوم)
 *  ٣) الحالة    : نجاح · تنبيه · خطأ
 *
 * سلّم الضوء ليس زينة: أسماء الصلوات كلّها أسماء حالات ضوء، فالوقت
 * هنا بنية. هذه التوكنز الثلاثة كانت مبعثرة في ستّة ألوان مخصصة
 * (morningRing · eveningRing · purpleSleep · sleepRing · accentOrange
 * · blueAccent) تُستعمل في السماء الحيّة وأيقونات المحطات وشارات
 * المساء. جُمعت هنا لتكون بذرة طبقة «الميقات».
 *
 * تمييز الأقسام لا يأتي بلون جديد لكل قسم — بل بالأيقونة ودرجة الضوء.
 */
@Immutable
data class RafiqPalette(
    /* ── الأسطح ── */
    val bg: Color,
    val card: Color,
    /** خلفية بطاقة «مؤداة/مكتملة» — لون معتم (لا شفافية) حتى لا يظهر الظِل من خلفها. */
    val cardPrayed: Color,
    /** خلفية الحبّة الصغيرة (التاريخ الهجري، الوسوم). */
    val chipBg: Color,

    /* ── الأساس: الزمرّدي ── */
    val emerald: Color,
    val emeraldMed: Color,
    val emeraldLight: Color,
    val emeraldPastel: Color,

    /* ── التمييز: الذهبي ── */
    val gold: Color,
    val goldLight: Color,

    /* ── لون المحتوى فوق الأسطح المملوءة ──
       في الوضع الداكن يصير الزمرّدي والذهبي فاتحَين، فالأبيض فوقهما
       يهبط إلى 2.72:1 و2.24:1 — دون كل عتبة. المحتوى هناك يكون داكناً. */
    val onEmerald: Color,
    val onGold: Color,

    /* ── الحبر ── */
    val ink: Color,
    val inkDark: Color,
    val inkMed: Color,
    val inkLight: Color,

    /* ── الفاصل ── */
    val divider: Color,

    /* ── تدرّج بطاقة البطل (زمرّدي، ليس لوناً جديداً) ── */
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,

    /* ── شارات المصحف: مكيّة/مدنية — تمييز بيانات حقيقي ── */
    val meccanBg: Color,
    val meccanText: Color,
    val madaniBg: Color,
    val madaniText: Color,

    /* ═══ سلّم الضوء — بذرة طبقة الميقات ═══
       الفجر والضحى يستعملان goldLight أعلاه؛ لا حاجة للون رابع. */
    /** ضوء العصر والمغرب — نحاسي دافئ. */
    val lightDusk: Color,
    /** ضوء العشاء والنوم — المقابل البارد الوحيد في التطبيق. */
    val lightNight: Color,

    /* ── خلفيات باهتة مشتقّة من الثلاثة أعلاه ── */
    val tintGold: Color,
    val tintDusk: Color,
    val tintNight: Color,

    /* ── الحالة ── */
    val success: Color,
    val warning: Color,
    val error: Color,
)

/* ═══════════════════════════════════════════
   LIGHT PALETTE
═══════════════════════════════════════════ */

val LightRafiqPalette = RafiqPalette(
    // #F5F0E8 السابق كان على بُعد خطوة من الكريمي المعروف كعلامة مخرجات AI.
    // هذا أدفأ وأقلّ حياداً، وأقرب إلى ورق المخطوط.
    bg             = Color(0xFFF2EDE1),
    card           = Color(0xFFFFFFFF),
    cardPrayed     = Color(0xFFE7E5DB),
    chipBg         = Color(0xFFEAE4D6),

    emerald        = Color(0xFF09472B),
    emeraldMed     = Color(0xFF0B5E38),
    emeraldLight   = Color(0xFF0D7446),
    emeraldPastel  = Color(0xFFE0EFE7),

    gold           = Color(0xFFB07C20),
    goldLight      = Color(0xFFC99230),

    onEmerald      = Color(0xFFFFFFFF),  // 10.78:1
    onGold         = Color(0xFFFFFFFF),  //  3.65:1 — أيقونات ونصّ كبير

    ink            = Color(0xFF1A1408),
    inkDark        = Color(0xFF33280F),
    inkMed         = Color(0xFF6E6455),
    // 2.01:1 سابقاً — أدنى من كل عتبة. الآن 3.42:1، ومقصور على
    // الأيقونات وعناصر التحكّم الخاملة؛ النصّ الخافت يستعمل inkMed.
    inkLight       = Color(0xFF8A7E66),

    divider        = Color(0x1AB07C20),

    heroStart      = Color(0xFF062917),
    heroMid        = Color(0xFF09472B),
    heroEnd        = Color(0xFF0B5934),

    meccanBg       = Color(0xFFFEF8EC),
    meccanText     = Color(0xFF8B6914),
    madaniBg       = Color(0xFFE0EFE7),
    madaniText     = Color(0xFF09472B),

    lightDusk      = Color(0xFFA5560A),  // 4.57:1 على الورق — يصلح نصّاً لا أيقونةً فقط
    lightNight     = Color(0xFF3A4A78),

    tintGold       = Color(0xFFFBF3E3),
    tintDusk       = Color(0xFFFBEFE0),
    tintNight      = Color(0xFFEDEEF7),

    success        = Color(0xFF2E7D32),
    warning        = Color(0xFFF9A825),
    error          = Color(0xFFC62828),
)

/* ═══════════════════════════════════════════
   DARK PALETTE
═══════════════════════════════════════════ */

val DarkRafiqPalette = RafiqPalette(
    bg             = Color(0xFF0F0D08),
    card           = Color(0xFF1E1A12),
    cardPrayed     = Color(0xFF17231C),
    chipBg         = Color(0xFF2A2519),

    emerald        = Color(0xFF4CAF7B),
    emeraldMed     = Color(0xFF3D9B6A),
    emeraldLight   = Color(0xFF5CC18C),
    emeraldPastel  = Color(0xFF1A2E24),

    gold           = Color(0xFFDAA520),
    goldLight      = Color(0xFFE8B84D),

    onEmerald      = Color(0xFF0F0D08),  // 7.14:1 — الأبيض هنا 2.72:1 فقط
    onGold         = Color(0xFF0F0D08),  // 8.68:1 — الأبيض هنا 2.24:1 فقط

    ink            = Color(0xFFE8E0D0),
    inkDark        = Color(0xFFD4C8B4),
    inkMed         = Color(0xFFA09480),
    inkLight       = Color(0xFF7A7060),

    divider        = Color(0x26DAA520),

    heroStart      = Color(0xFF0A1F14),
    heroMid        = Color(0xFF0F3322),
    heroEnd        = Color(0xFF144430),

    meccanBg       = Color(0xFF2A2010),
    meccanText     = Color(0xFFDAA520),
    madaniBg       = Color(0xFF1A2E24),
    madaniText     = Color(0xFF4CAF7B),

    lightDusk      = Color(0xFFE0954A),
    lightNight     = Color(0xFF8B96E8),

    tintGold       = Color(0xFF2A2010),
    tintDusk       = Color(0xFF2E1F0C),
    tintNight      = Color(0xFF1B2038),

    success        = Color(0xFF66BB6A),
    warning        = Color(0xFFFFCA28),
    error          = Color(0xFFEF5350),
)

/* ═══════════════════════════════════════════
   COMPOSITION LOCAL
═══════════════════════════════════════════ */

val LocalRafiqColors = compositionLocalOf { LightRafiqPalette }
