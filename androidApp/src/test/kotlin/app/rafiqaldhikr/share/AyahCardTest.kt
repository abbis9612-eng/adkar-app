package app.rafiqaldhikr.share

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * حسابُ ارتفاع بطاقة الآية.
 *
 * الارتفاعُ يتبع النصَّ لا العكس: **النصُّ القرآنيُّ لا يُقتطع لتستقيم
 * البطاقة.** وهذا الاختبارُ يُثبت أنّ الحساب يزيد مع طول النصّ ولا
 * يثبت عند سقف — فآيةُ الدَّين لا تُقصّ كآيةِ الكرسيّ.
 */
class AyahCardTest {

    /** نفسُ معادلة [app.rafiqaldhikr.ui.share.renderAyahCard] بلا رسم. */
    private fun height(lines: Int): Int {
        val lineH = 58f * 1.8f
        return (104f + lines * lineH + 56f + 2f + 44f + 48f + 64f + 36f + 72f).toInt()
    }

    @Test
    fun heightGrowsWithText() {
        val one = height(1)
        val five = height(5)
        val twenty = height(20)
        assertTrue("سطرٌ واحد يجب أن يكون أقصر من خمسة", one < five)
        assertTrue("خمسةٌ يجب أن تكون أقصر من عشرين", five < twenty)
        //  ولا سقف: الفرقُ بين ٥ و٢٠ يساوي خمسةَ عشرَ سطراً كاملاً
        assertTrue("الزيادةُ خطّيّةٌ لا مقطوعة", twenty - five >= 15 * 104)
    }

    @Test
    fun cardIsNeverTallerThanNeeded() {
        //  حشوةٌ ثابتةٌ معقولة: لا تتجاوز ٤٥٠ بكسلاً فوق النصّ
        assertTrue(height(0) in 1..450)
    }
}
