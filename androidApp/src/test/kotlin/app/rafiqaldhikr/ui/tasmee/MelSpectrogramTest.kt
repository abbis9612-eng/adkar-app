package app.rafiqaldhikr.ui.tasmee

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * مِطْيافُ التسميع يُقابَل بالمرجع.
 *
 * `tools/tasmee_reference.py` يحسب المِطْيافَ بـnumpy على **جَيْبٍ عند
 * ٤٤٠ هرتز**، واختير الجَيْبُ لأنّه يُولَّد في اللغتين بالصيغة نفسِها
 * حرفاً بحرف. والأرقامُ أدناه مطبوعةٌ من تشغيلِ ذاك المرجع، لا مقدَّرة.
 *
 * ولو انزاح حرفٌ في النافذة أو في الحشو الانعكاسيّ أو في حدّ القصّ
 * لتغيّرت هذه الأرقام — **والنموذجُ لا يشكو من مدخلٍ مختلّ، يُخرج كلاماً
 * أسوأَ بصمت**. فهذا الاختبارُ هو ما يمنع ذلك.
 */
class MelSpectrogramTest {

    //  مطبوعةٌ من `python3 tools/tasmee_reference.py`
    private val toneMean = -0.557662f
    private val toneMin = -0.561796f
    private val toneMax = 1.438204f
    private val tone00 = 0.983279f
    private val tone4050 = -0.561796f

    private val filtersFile = listOf(
        File("src/main/assets/tasmee/mel_filters.bin"),
        File("androidApp/src/main/assets/tasmee/mel_filters.bin"),
    ).firstOrNull { it.exists() }

    private fun filters(): FloatArray {
        val f = requireNotNull(filtersFile) { "mel_filters.bin غير موجود" }
        val bytes = f.readBytes()
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / 4) { buf.getFloat(it * 4) }
    }

    /** جَيْبٌ عند ٤٤٠ هرتز ثانيةً — الصيغةُ نفسُها التي في المرجع. */
    private fun tone(): FloatArray = FloatArray(MelSpec.SAMPLE_RATE) { i ->
        (0.5 * sin(2.0 * PI * 440.0 * i / MelSpec.SAMPLE_RATE)).toFloat()
    }

    /** المرشّحاتُ المشحونةُ بالحجم والمجموع المنتظرَين. */
    @Test
    fun shippedFiltersHaveExpectedShape() {
        val fb = filters()
        assertEquals(MelSpec.N_MELS * MelSpec.N_FREQ, fb.size)
        val sum = fb.fold(0.0) { a, v -> a + v }
        //  المرجع: 1.999024
        assertTrue("مجموعُ المرشّحات $sum", abs(sum - 1.999024) < 1e-4)
    }

    /** شكلُ المخرَج: ٨٠ نطاقاً × ٣٠٠٠ إطار. */
    @Test
    fun outputShapeIsEightyByThreeThousand() {
        val feats = logMel(FloatArray(MelSpec.SAMPLE_RATE), filters())
        assertEquals(MelSpec.N_MELS * MelSpec.N_FRAMES, feats.size)
    }

    /**
     * **والقابلةُ الكبرى: أرقامُ كوتلن أرقامُ المرجع.**
     *
     * والسماحُ ‎1e-3‎ لأنّ كوتلن تحسب بـ`Double` وnumpy بـ`float32` في
     * بعض الخطوات — والفرقُ هنا من ذلك لا من اختلافِ خطوة.
     */
    @Test
    fun matchesPythonReferenceOnTone() {
        val feats = logMel(tone(), filters())
        val mean = feats.fold(0.0) { a, v -> a + v } / feats.size

        assertTrue("المتوسّط $mean والمرجع $toneMean", abs(mean - toneMean) < 1e-3)
        assertTrue("الأدنى ${feats.min()}", abs(feats.min() - toneMin) < 1e-3)
        assertTrue("الأقصى ${feats.max()}", abs(feats.max() - toneMax) < 1e-3)
        assertTrue(
            "[0][0] ${feats[0]} والمرجع $tone00",
            abs(feats[0 * MelSpec.N_FRAMES + 0] - tone00) < 1e-3,
        )
        assertTrue(
            "[40][50] ${feats[40 * MelSpec.N_FRAMES + 50]} والمرجع $tone4050",
            abs(feats[40 * MelSpec.N_FRAMES + 50] - tone4050) < 1e-3,
        )
    }

    /**
     * حدُّ القصُّ **عامٌّ على المصفوفة لا على كلّ إطار**.
     *
     * فلو قُصّ كلُّ إطارٍ بأقصاه لصار أدنى قيمةٍ في كلّ إطارٍ واحداً —
     * وهذا يُثبت أنّ الأدنى واحدٌ في المصفوفة كلِّها: ‎(max−8+4)/4‎.
     */
    @Test
    fun clampFloorIsGlobalNotPerFrame() {
        val feats = logMel(tone(), filters())
        val floor = feats.min()
        val expected = (feats.max() * 4f - 4f - 8f + 4f) / 4f
        assertTrue("الأدنى $floor والمنتظر $expected", abs(floor - expected) < 1e-3)
    }

    /** صمتٌ تامّ: لا قيمةَ شاذّةٌ ولا NaN — فالمصفوفةُ تُسلَّم للنموذج. */
    @Test
    fun silenceProducesFiniteValues() {
        val feats = logMel(FloatArray(MelSpec.SAMPLE_RATE), filters())
        assertTrue(feats.all { it.isFinite() })
    }

    /** صوتٌ أقصرُ من النافذة يُحشى ولا يُرفض. */
    @Test
    fun shortAudioIsPaddedNotRejected() {
        val feats = logMel(FloatArray(800) { 0.1f }, filters())
        assertEquals(MelSpec.N_MELS * MelSpec.N_FRAMES, feats.size)
        assertTrue(feats.all { it.isFinite() })
    }
}
