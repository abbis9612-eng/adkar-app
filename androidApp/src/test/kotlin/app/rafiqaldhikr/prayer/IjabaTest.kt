package app.rafiqaldhikr.prayer

import app.rafiqaldhikr.ui.screens.prayer.countdownRaw
import app.rafiqaldhikr.ui.screens.prayer.ijabaPairs
import app.rafiq.domain.model.parseAthar
import app.rafiqaldhikr.ui.screens.prayer.fromBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * جُمَلُ الإجابة تُقتطع من متن حديث عمر في `athar.json` — فيُختبر الاقتطاعُ
 * على الملفّ الحقيقيّ لا على نسخةٍ في الاختبار: من عدّل الملفَّ فانكسر
 * الاقتطاعُ عرف هنا، لا حين يرى صاحبُ التطبيق جملةً مبتورةً عند الأذان.
 */
class IjabaTest {

    private val ijaba by lazy {
        val f = listOf(File("src/main/assets/athar.json"), File("androidApp/src/main/assets/athar.json"))
            .first { it.exists() }
        requireNotNull(fromBook(parseAthar(f.readText(Charsets.UTF_8)))) { "تعذّر بناءُ الإجابة من athar.json" }
    }
    private val umar get() = ijaba.hadith.text

    @Test fun bothHadithsAreFromMuslimAndGraded() {
        assertTrue(ijaba.hadith.source.startsWith("رواه مسلم") && ijaba.hadith.source.endsWith("صحيح"))
        assertTrue(ijaba.after.text.contains("الْوَسِيلَةَ"))
    }

    @Test fun sevenPairsInOrder() {
        val pairs = ijabaPairs(umar)
        assertEquals(7, pairs.size)
        // كلُّ جملةٍ بلفظها في المتن — لا يُصاغ شيء
        pairs.forEach { assertTrue(it.muezzin in umar && it.reply in umar) }
    }

    @Test fun hayyaIsAnsweredWithLaHawla() {
        val pairs = ijabaPairs(umar)
        // بالحروف لا بالتشكيل: ترتيبُ الشدّة والفتحة يختلف بين النصوص
        assertTrue(bare(pairs[3].muezzin).startsWith("حي على الصلاة") && bare(pairs[3].reply).startsWith("لا حول"))
        assertTrue(bare(pairs[4].muezzin).startsWith("حي على الفلاح") && bare(pairs[4].reply).startsWith("لا حول"))
    }

    @Test fun lastReplyStopsBeforeTheReward() {
        // «مِنْ قَلْبِهِ دَخَلَ الْجَنَّةَ» تمامُ الحديث لا من لفظ الجواب
        val last = ijabaPairs(umar).last()
        assertTrue("قلبه" !in bare(last.reply) && "»" !in last.reply)
    }

    private fun bare(s: String) = s.replace(Regex("[\\u064B-\\u065F\\u0670]"), "")

    @Test fun malformedTextGivesNothing() {
        assertTrue(ijabaPairs("لا اقتباسَ هنا").isEmpty())
    }

    @Test fun countdownFormat() {
        assertEquals("2:07:53", countdownRaw(2 * 3600 + 7 * 60 + 53))
        assertEquals("0:00:00", countdownRaw(-5))
    }
}
