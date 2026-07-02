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
)

/* ═══════════════════════════════════════════
   LIGHT PALETTE
═══════════════════════════════════════════ */

val LightRafiqPalette = RafiqPalette(
    bg             = Color(0xFFF8F4EB),
    card           = Color(0xFFFFFDF8),

    emerald        = Color(0xFF0B4A2F),
    emeraldMed     = Color(0xFF11603D),
    emeraldLight   = Color(0xFF177A4E),
    emeraldPastel  = Color(0xFFE4F1E9),
    emeraldPastel2 = Color(0xFFCBE3D5),

    gold           = Color(0xFFA97A17),
    goldLight      = Color(0xFFCC9A33),

    ink            = Color(0xFF201A0C),
    inkDark        = Color(0xFF362B11),
    inkMed         = Color(0xFF75695A),
    inkLight       = Color(0xFFB1A58C),

    divider        = Color(0x1FA97A17),
    overlay        = Color(0x08000000),

    heroStart      = Color(0xFF05301C),
    heroMid        = Color(0xFF0B4A2F),
    heroEnd        = Color(0xFF14623C),

    meccanBg       = Color(0xFFFDF6E7),
    meccanText     = Color(0xFF8B6914),
    madaniBg       = Color(0xFFE4F1E9),
    madaniText     = Color(0xFF0B4A2F),

    brownAccent    = Color(0xFF8B6914),
    blueAccent     = Color(0xFF1A3A5C),

    success        = Color(0xFF2E7D32),
    warning        = Color(0xFFF9A825),
    error          = Color(0xFFC0392B),

    navGlass       = Color(0xFFFCF9F2),
    navSelected    = Color(0xFF0B4A2F),

    purple         = Color(0xFF8E5FBA),
    purpleSleep    = Color(0xFF6F55A3),
    morningRing    = Color(0xFFD9A733),
    eveningRing    = Color(0xFFC5BAE8),
    sleepRing      = Color(0xFF8E5FBA),
    istighfarRing  = Color(0xFF3E9467),
)

/* ═══════════════════════════════════════════
   DARK PALETTE
═══════════════════════════════════════════ */

val DarkRafiqPalette = RafiqPalette(
    bg             = Color(0xFF0C110D),
    card           = Color(0xFF161F18),

    emerald        = Color(0xFF57C08C),
    emeraldMed     = Color(0xFF45AC7A),
    emeraldLight   = Color(0xFF6BD09D),
    emeraldPastel  = Color(0xFF1B2E23),
    emeraldPastel2 = Color(0xFF21382B),

    gold           = Color(0xFFDFAB3B),
    goldLight      = Color(0xFFEDC468),

    ink            = Color(0xFFECE5D5),
    inkDark        = Color(0xFFD8CDB8),
    inkMed         = Color(0xFFA39A87),
    inkLight       = Color(0xFF7C7463),

    divider        = Color(0x26DFAB3B),
    overlay        = Color(0x15FFFFFF),

    heroStart      = Color(0xFF071F13),
    heroMid        = Color(0xFF0E3220),
    heroEnd        = Color(0xFF17462D),

    meccanBg       = Color(0xFF2B2312),
    meccanText     = Color(0xFFDFAB3B),
    madaniBg       = Color(0xFF1B2E23),
    madaniText     = Color(0xFF57C08C),

    brownAccent    = Color(0xFFDFAB3B),
    blueAccent     = Color(0xFF5B8CB8),

    success        = Color(0xFF66BB6A),
    warning        = Color(0xFFFFCA28),
    error          = Color(0xFFEF5350),

    navGlass       = Color(0xFF131B15),
    navSelected    = Color(0xFF57C08C),

    purple         = Color(0xFFA98BE0),
    purpleSleep    = Color(0xFF8E74C9),
    morningRing    = Color(0xFFC29A38),
    eveningRing    = Color(0xFF6660A0),
    sleepRing      = Color(0xFF7458A8),
    istighfarRing  = Color(0xFF3E7D58),
)

/* ═══════════════════════════════════════════
   COMPOSITION LOCAL
═══════════════════════════════════════════ */

val LocalRafiqColors = compositionLocalOf { LightRafiqPalette }
