package app.rafiqaldhikr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Unified design-token palette for Rafiq Al-Dhikr.
 *
 * Every screen MUST reference [LocalRafiqColors] instead of defining its own
 * private color object. Shorthand: `val rc = LocalRafiqColors.current`
 */
@Immutable
data class RafiqPalette(
    /* ── Backgrounds ── */
    val bg: Color,
    val card: Color,
    /** خلفية بطاقة "مؤداة/مكتملة" — لون معتم (لا شفافية) حتى لا يظهر الظِل من خلفها. */
    val cardPrayed: Color,

    /* ── Primary — Emerald Green ── */
    val emerald: Color,
    val emeraldMed: Color,
    val emeraldLight: Color,
    val emeraldPastel: Color,
    val emeraldPastel2: Color,

    /* ── Accent — Gold ── */
    val gold: Color,
    val goldLight: Color,

    /* ── Text / Ink ── */
    val ink: Color,
    val inkDark: Color,
    val inkMed: Color,
    val inkLight: Color,

    /* ── Divider & Overlay ── */
    val divider: Color,
    val overlay: Color,

    /* ── Hero Card Gradient ── */
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,

    /* ── Quran Badges ── */
    val meccanBg: Color,
    val meccanText: Color,
    val madaniBg: Color,
    val madaniText: Color,

    /* ── Dhikr Semantic ── */
    val brownAccent: Color,
    val blueAccent: Color,

    /* ── Status ── */
    val success: Color,
    val warning: Color,
    val error: Color,

    /* ── Navigation ── */
    val navGlass: Color,
    val navSelected: Color,

    /* ── Semantic Rings & Badges ── */
    val purple: Color,
    val purpleSleep: Color,
    val morningRing: Color,
    val eveningRing: Color,
    val sleepRing: Color,
    val istighfarRing: Color,

    /* ── Category Accents (bg/fg pairs) ── */
    val accentGoldBg: Color,
    val accentGold: Color,
    val accentIndigoBg: Color,
    val accentIndigo: Color,
    val accentPurpleBg: Color,
    val accentPurple: Color,
    val accentBrownBg: Color,
    val accentBrown: Color,
    val accentOrangeBg: Color,
    val accentOrange: Color,

    /* ── Small chip background (hijri date, tags) ── */
    val chipBg: Color,
)

/* ═══════════════════════════════════════════
   LIGHT PALETTE
═══════════════════════════════════════════ */

val LightRafiqPalette = RafiqPalette(
    bg             = Color(0xFFF5F0E8),
    card           = Color(0xFFFFFFFF),
    cardPrayed     = Color(0xFFE9E8DF), // = emerald@5% مركّب فوق bg، لكن معتماً

    emerald        = Color(0xFF09472B),
    emeraldMed     = Color(0xFF0B5E38),
    emeraldLight   = Color(0xFF0D7446),
    emeraldPastel  = Color(0xFFE0EFE7),
    emeraldPastel2 = Color(0xFFC4DFCF),

    gold           = Color(0xFFB07C20),
    goldLight      = Color(0xFFC99230),

    ink            = Color(0xFF1A1408),
    inkDark        = Color(0xFF33280F),
    inkMed         = Color(0xFF7A7060),
    inkLight       = Color(0xFFB5A88E),

    divider        = Color(0x1AB07C20),
    overlay        = Color(0x08000000),

    heroStart      = Color(0xFF062917),
    heroMid        = Color(0xFF09472B),
    heroEnd        = Color(0xFF0B5934),

    meccanBg       = Color(0xFFFEF8EC),
    meccanText     = Color(0xFF8B6914),
    madaniBg       = Color(0xFFE0EFE7),
    madaniText     = Color(0xFF09472B),

    brownAccent    = Color(0xFF8B6914),
    blueAccent     = Color(0xFF1A3A5C),

    success        = Color(0xFF2E7D32),
    warning        = Color(0xFFF9A825),
    error          = Color(0xFFC62828),

    navGlass       = Color(0xFFFDF8F0),
    navSelected    = Color(0xFF09472B),

    purple         = Color(0xFF9B6DBF),
    purpleSleep    = Color(0xFF7B5EA7),
    morningRing    = Color(0xFFD4A030),
    eveningRing    = Color(0xFFC5BAE8),
    sleepRing      = Color(0xFF9B6DBF),
    istighfarRing  = Color(0xFF4A9E6E),

    accentGoldBg   = Color(0xFFFEF8EC),
    accentGold     = Color(0xFFB07C20),
    accentIndigoBg = Color(0xFFEEF0FB),
    accentIndigo   = Color(0xFF4B5BC4),
    accentPurpleBg = Color(0xFFF3F0FD),
    accentPurple   = Color(0xFF7C4DC9),
    accentBrownBg  = Color(0xFFFDF6E8),
    accentBrown    = Color(0xFF8B6914),
    accentOrangeBg = Color(0xFFFEF3DC),
    accentOrange   = Color(0xFFE8780A),

    chipBg         = Color(0xFFEDE8DC),
)

/* ═══════════════════════════════════════════
   DARK PALETTE
═══════════════════════════════════════════ */

val DarkRafiqPalette = RafiqPalette(
    bg             = Color(0xFF0F0D08),
    card           = Color(0xFF1E1A12),
    cardPrayed     = Color(0xFF17231C), // نسخة داكنة معتمة بلمسة خضراء لحالة "مؤداة"

    emerald        = Color(0xFF4CAF7B),
    emeraldMed     = Color(0xFF3D9B6A),
    emeraldLight   = Color(0xFF5CC18C),
    emeraldPastel  = Color(0xFF1A2E24),
    emeraldPastel2 = Color(0xFF1E3429),

    gold           = Color(0xFFDAA520),
    goldLight      = Color(0xFFE8B84D),

    ink            = Color(0xFFE8E0D0),
    inkDark        = Color(0xFFD4C8B4),
    inkMed         = Color(0xFFA09480),
    inkLight       = Color(0xFF7A7060),

    divider        = Color(0x26DAA520),
    overlay        = Color(0x15FFFFFF),

    heroStart      = Color(0xFF0A1F14),
    heroMid        = Color(0xFF0F3322),
    heroEnd        = Color(0xFF144430),

    meccanBg       = Color(0xFF2A2010),
    meccanText     = Color(0xFFDAA520),
    madaniBg       = Color(0xFF1A2E24),
    madaniText     = Color(0xFF4CAF7B),

    brownAccent    = Color(0xFFDAA520),
    blueAccent     = Color(0xFF5B8CB8),

    success        = Color(0xFF66BB6A),
    warning        = Color(0xFFFFCA28),
    error          = Color(0xFFEF5350),

    navGlass       = Color(0xFF1E1A12),
    navSelected    = Color(0xFF4CAF7B),

    purple         = Color(0xFF7B8BF4),
    purpleSleep    = Color(0xFFA67DF9),
    morningRing    = Color(0xFFB89030),
    eveningRing    = Color(0xFF6660A0),
    sleepRing      = Color(0xFF7050A0),
    istighfarRing  = Color(0xFF3A7050),

    accentGoldBg   = Color(0xFF2A2010),
    accentGold     = Color(0xFFDAA520),
    accentIndigoBg = Color(0xFF1B2038),
    accentIndigo   = Color(0xFF8B96E8),
    accentPurpleBg = Color(0xFF251C38),
    accentPurple   = Color(0xFFA98BE8),
    accentBrownBg  = Color(0xFF2A2210),
    accentBrown    = Color(0xFFC9A44D),
    accentOrangeBg = Color(0xFF2E1F0C),
    accentOrange   = Color(0xFFF09A4A),

    chipBg         = Color(0xFF2A2519),
)

/* ═══════════════════════════════════════════
   COMPOSITION LOCAL
═══════════════════════════════════════════ */

val LocalRafiqColors = compositionLocalOf { LightRafiqPalette }
