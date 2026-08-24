package app.rafiq.domain.model

/**
 * كلمةُ اليوم — نصٌّ قصير يتصدّر الشاشة الرئيسية.
 *
 * مكانها في shared لا في androidApp للسبب نفسه الذي وضع [City] هنا:
 * تحليل JSON يحتاج kotlinx.serialization، وهي مُعرَّفة في هذه الوحدة وحدها.
 *
 * ولكلّ كلمة [source] و[grade] ظاهران للمستخدم تحت النصّ — لا في حاشية.
 * وحارس `tools/check_religious_sources.py` يرفض البناء إن نقص أحدهما.
 */
data class Wisdom(
    val id:     String,
    val text:   String,
    val author: String,
    val source: String,
    val grade:  String,
)
