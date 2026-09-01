package app.rafiq.domain.repository

import app.rafiq.domain.model.Wisdom

interface WisdomRepository {
    /** كلمةُ يومٍ بعينه. [epochDay] عددُ الأيام منذ ١٩٧٠-٠١-٠١. */
    suspend fun forDay(epochDay: Long): Wisdom?
}
