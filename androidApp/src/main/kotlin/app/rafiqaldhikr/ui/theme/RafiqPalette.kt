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

    /* ── الأخضر الممتلئ ──
       الزمرّدي الداكن يُقرأ نصّاً على الورق (7.61) ولا يُقرأ ملءاً — والأبيض
       فوقه وحده يعمل. والأخضر الفاتح عكسه تماماً: ملءٌ ممتاز (8.54 بحبرٍ
       داكن فوقه) ونصٌّ لا يُقرأ إطلاقاً (1.66 على الورق).
       فلونان لدورين، لا لونٌ واحد يُجبَر على الدورين. */
    val emeraldFill: Color,
    val onEmeraldFill: Color,

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
    // ورق لا خلفية. الكريمي الفاتح يقول «خلفية تطبيق»؛ هذا يقول ورقاً،
    // والبطاقة فوقه ورقةٌ أفتح لا بياضاً نقياً.
    // سلّم النبرات: كان الورق ↔ البطاقة 1.09:1 — أي غير مرئي (العين تبدأ
    // بالتمييز عند 1.2 تقريباً). عُمِّق الورق وأُنصعت البطاقة معاً فصار 1.29:1.
    // رفعُ أحدهما وحده لا يكفي — لهذا تحرّك الطرفان.
    bg             = Color(0xFFF4F1E7),
    card           = Color(0xFFDCD9D0),
    cardPrayed     = Color(0xFFE4E2D9),
    chipBg         = Color(0xFFE8E6DC),  // inkMed فوقه 5.70:1

    emerald        = Color(0xFF6F541E),
    emeraldMed     = Color(0xFF61491A),
    emeraldLight   = Color(0xFF795A20),
    emeraldPastel  = Color(0xFFF0E7D3),

    emeraldFill    = Color(0xFFE0A83C),  // ملء فقط — نصّاً يهبط إلى 1.66
    onEmeraldFill  = Color(0xFF14201A),  // 8.54:1 فوق الملء (الأبيض هناك 1.97)

    gold           = Color(0xFF7A5807),  // 5.89:1 على الورق الجديد
    goldLight      = Color(0xFFB8860B),

    onEmerald      = Color(0xFFF7F2E6),  // 10.4:1
    onGold         = Color(0xFFF7F2E6),  //  3.5:1 — أيقونات ونصّ كبير

    // حبر أخضر داكن لا أسود — فيبقى الزمرّدي هويّةً حتى داخل النصّ.
    ink            = Color(0xFF14201A),   // 12.76:1
    inkDark        = Color(0xFF2B3B31),
    inkMed         = Color(0xFF505851),   //  4.54:1
    // مقصور على الأيقونات وعناصر التحكّم الخاملة؛ النصّ الخافت يستعمل inkMed.
    inkLight       = Color(0xFF70766E),   //  3.79:1

    // خطّ المسطرة — لا فاصل رمادي
    divider        = Color(0xFFD0D0C6),

    heroStart      = Color(0xFF062917),
    heroMid        = Color(0xFF09472B),
    heroEnd        = Color(0xFF0B5934),

    meccanBg       = Color(0xFFFEF8EC),
    meccanText     = Color(0xFF8B6914),
    madaniBg       = Color(0xFFE0EFE7),
    madaniText     = Color(0xFF09472B),

    lightDusk      = Color(0xFF9C5109),  // 4.70:1 على الورق الأغمق
    lightNight     = Color(0xFF3A4A78),  // 6.95:1

    tintGold       = Color(0xFFF6EDD9),
    tintDusk       = Color(0xFFF6E8D4),
    tintNight      = Color(0xFFE7E7EF),

    success        = Color(0xFF2E7D32),
    warning        = Color(0xFFF9A825),
    error          = Color(0xFFC62828),
)

/* ═══════════════════════════════════════════
   DARK PALETTE
═══════════════════════════════════════════ */

val DarkRafiqPalette = RafiqPalette(
    bg             = Color(0xFF12100A),
    card           = Color(0xFF312F2A),
    cardPrayed     = Color(0xFF26241D),
    chipBg         = Color(0xFF221F19),

    emerald        = Color(0xFFE0A83C),
    emeraldMed     = Color(0xFFD49B30),
    emeraldLight   = Color(0xFFEDBB5C),
    emeraldPastel  = Color(0xFF2E2718),

    gold           = Color(0xFFDAA520),
    goldLight      = Color(0xFFE8B84D),

    emeraldFill    = Color(0xFFE0A83C),
    onEmeraldFill  = Color(0xFF14201A),  // 9.14:1

    onEmerald      = Color(0xFF14201A),  // 9.14:1 — الأبيض هنا 2.72:1 فقط
    onGold         = Color(0xFF14201A),  // 8.6:1 — الأبيض هنا 2.24:1 فقط

    ink            = Color(0xFFEFE8D8),
    inkDark        = Color(0xFFD6CFBE),
    inkMed         = Color(0xFFA7A194),
    inkLight       = Color(0xFF837E73),

    divider        = Color(0xFF35332B),

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
