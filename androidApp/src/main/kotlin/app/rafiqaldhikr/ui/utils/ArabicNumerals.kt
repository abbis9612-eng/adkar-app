package app.rafiqaldhikr.ui.utils

/**
 * Converts Western Arabic numerals (0-9) to Eastern Arabic numerals (٠-٩).
 * All other characters are preserved.
 */
fun String.toEasternArabicNumerals(): String = buildString {
    this@toEasternArabicNumerals.forEach { ch ->
        append(
            when (ch) {
                '0' -> '٠'
                '1' -> '١'
                '2' -> '٢'
                '3' -> '٣'
                '4' -> '٤'
                '5' -> '٥'
                '6' -> '٦'
                '7' -> '٧'
                '8' -> '٨'
                '9' -> '٩'
                else -> ch
            }
        )
    }
}

fun Long.toEasternArabic(): String = toString().toEasternArabicNumerals()
fun Int.toEasternArabic(): String = toString().toEasternArabicNumerals()

/**
 * Format time as Eastern Arabic HH:MM
 */
fun formatTimeEastern(hours: Int, minutes: Int): String {
    return "%02d:%02d".format(hours, minutes).toEasternArabicNumerals()
}

/**
 * Format countdown as Eastern Arabic HH:MM:SS
 */
fun formatCountdownEastern(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s).toEasternArabicNumerals()
}
