package app.rafiq.domain.repository

import app.rafiq.domain.model.KhatmaPlan
import kotlinx.coroutines.flow.Flow

/** الختمةُ النشطةُ وسجلُّ ما مضى. */
interface KhatmaRepository {

    /** الختمةُ النشطة — واحدةٌ أو لا شيء. */
    fun active(): Flow<KhatmaPlan?>

    /** كلُّ الختمات، أحدثُها أوّلاً — سجلٌّ يُرى في «سنتك». */
    fun all(): Flow<List<KhatmaPlan>>

    /** يبدأ ختمةً جديدةً ويُنهي ما قبلها — واحدةٌ نشطةٌ في كلّ وقت. */
    suspend fun start(
        days: Int,
        fromPage: Int = 1,
        toPage: Int = 604,
        meeqat: String = "dhuhr",
        continuous: Boolean = false,
    )

    /**
     * يسجّل أنّ القارئ بلغ [page].
     *
     * ولا ينقص: من سجّل صفحةً أقلَّ ممّا بلغ لا يُمحى تقدّمُه — قد يكون
     * رجع يراجع، والمراجعةُ لا تُلغي ما قُرئ.
     */
    suspend fun markRead(page: Int)

    /** يُنهي الختمةَ النشطة — ويبدأ غيرَها إن كانت مستمرّة. */
    suspend fun finish()

    /** يحذف ختمةً من السجلّ. */
    suspend fun remove(id: Long)
}
