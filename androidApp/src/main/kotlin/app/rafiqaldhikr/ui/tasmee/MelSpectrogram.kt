package app.rafiqaldhikr.ui.tasmee

import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.PI

/*
 * مِطْيافُ التسميع — صوتٌ ← مدخلُ النموذج
 * ════════════════════════════════════
 *
 * النموذجُ لا يرى صوتاً. يرى مصفوفةَ ٨٠×٣٠٠٠: ثمانين نطاقاً من سلّم مل
 * على ثلاثةِ آلاف إطارٍ زمنيّ. وهذا الملفُّ يصنعها.
 *
 * ═══ وكلُّ رقمٍ هنا مقيسٌ لا مُقدَّر ═══
 *
 * نقلاً عن `tools/tasmee_reference.py` — وهو يعمل على الأصول المشحونة
 * نفسِها. و`MelSpectrogramTest` يقابل أرقامَ هذا الملفّ بأرقامِ ذاك على
 * **جَيْبٍ يُولَّد في اللغتين بالصيغة نفسِها**. فمن غيّر هنا رقماً خالف
 * المرجعَ فسقط الاختبار.
 *
 * ═══ ولماذا تحويلٌ بطولِ ٤٠٠ لا ٥١٢ ═══
 *
 * ٤٠٠ ليست قوّةَ اثنين، فلا يعمل عليها التحويلُ السريعُ المعتاد. والحلُّ
 * السهلُ أن يُحشى الإطارُ إلى ٥١٢ — **وهو خطأ**: يُغيّر تردّدَ كلِّ خانةٍ
 * فتصير مصفوفةُ المرشّحات (المبنيّةُ على ٢٠١ خانةً من تحويل ٤٠٠) على
 * غير موضعها، ويصير مدخلُ النموذج غيرَ ما دُرّب عليه. والنموذجُ لا يشكو:
 * يُخرج كلاماً أسوأَ بصمت.
 *
 * فالتحويلُ يُقسَم ما دام الطولُ زوجيّاً (٤٠٠ ← ٢٠٠ ← ١٠٠ ← ٥٠ ← ٢٥)،
 * ثمّ تحويلٌ مباشرٌ على ٢٥. وهذا ما يفعله `whisper.cpp` نفسُه، وكلفتُه
 * مقبولةٌ لأنّ التسميعَ مرّةٌ واحدةٌ لا إطارٌ كلَّ لحظة.
 */

/** مواصفاتُ whisper الصوتيّة — من `MODEL.json` لا من ذاكرة. */
object MelSpec {
    const val SAMPLE_RATE = 16000
    const val N_FFT = 400
    const val HOP = 160
    const val N_MELS = 80
    const val N_FRAMES = 3000

    /** ثلاثون ثانية — طولُ نافذة النموذج، لا يزيد ولا ينقص. */
    const val N_SAMPLES = 480000

    /** خاناتُ الطيف: نصفُ طول التحويل زائدَ واحد. */
    const val N_FREQ = N_FFT / 2 + 1
}

/**
 * يحوّل الصوتَ إلى مدخل النموذج.
 *
 * @param audio عيّناتٌ ١٦ ك.هرتز أحاديّةٌ في [-1, 1] — تُقصَّر أو تُحشى
 *              بالأصفار إلى ثلاثين ثانية
 * @param filters مصفوفةُ المرشّحات ٨٠×٢٠١ من `assets/tasmee/mel_filters.bin`
 * @return مصفوفةٌ مسطَّحةٌ ٨٠×٣٠٠٠ بترتيب [mel][frame] — كما يريدها ONNX
 */
fun logMel(audio: FloatArray, filters: FloatArray): FloatArray {
    //  رسالةُ مطوِّرٍ بالإنجليزيّة لا بالعربيّة: لا تصل مستخدماً أبداً،
    //  ونصٌّ عربيٌّ في الكود يُحشى في ملفّ الترجمة بما ليس منه.
    require(filters.size == MelSpec.N_MELS * MelSpec.N_FREQ) {
        "mel filters ${filters.size}, expected ${MelSpec.N_MELS * MelSpec.N_FREQ}"
    }

    val pad = MelSpec.N_FFT / 2
    val n = MelSpec.N_SAMPLES
    val have = if (audio.size < n) audio.size else n

    /*  نافذةُ هان متناظرةٌ بطول ٤٠١ مقصوصةٌ إلى ٤٠٠ — وهي ما يستعمله
     *  whisper. ونافذةُ ٤٠٠ الدوريّة تُزيح كلَّ خانةٍ قليلاً. */
    val window = DoubleArray(MelSpec.N_FFT) { i ->
        0.5 - 0.5 * cos(2.0 * PI * i / MelSpec.N_FFT)
    }

    //  طاقةُ كلّ إطار: ٣٠٠٠ × ٢٠١
    val power = Array(MelSpec.N_FRAMES) { DoubleArray(MelSpec.N_FREQ) }
    val re = DoubleArray(MelSpec.N_FFT)
    val im = DoubleArray(MelSpec.N_FFT)
    val outRe = DoubleArray(MelSpec.N_FFT)
    val outIm = DoubleArray(MelSpec.N_FFT)

    for (t in 0 until MelSpec.N_FRAMES) {
        val start = t * MelSpec.HOP - pad
        for (k in 0 until MelSpec.N_FFT) {
            re[k] = sampleAt(audio, have, start + k) * window[k]
            im[k] = 0.0
        }
        fft(re, im, MelSpec.N_FFT, outRe, outIm)
        val row = power[t]
        for (k in 0 until MelSpec.N_FREQ) {
            row[k] = outRe[k] * outRe[k] + outIm[k] * outIm[k]
        }
    }

    /*  المرشّحاتُ ثمّ اللوغاريتم. والقصُّ عند **الأقصى العامّ ناقصَ ٨** —
     *  عامٌّ على المصفوفة كلِّها لا على كلّ إطار. ولو قُصّ كلُّ إطارٍ
     *  بأقصاه لصار الصمتُ كالكلام في الصخب. */
    val out = FloatArray(MelSpec.N_MELS * MelSpec.N_FRAMES)
    var maxLog = -Double.MAX_VALUE
    for (m in 0 until MelSpec.N_MELS) {
        val base = m * MelSpec.N_FREQ
        for (t in 0 until MelSpec.N_FRAMES) {
            var acc = 0.0
            val row = power[t]
            for (k in 0 until MelSpec.N_FREQ) acc += filters[base + k] * row[k]
            val v = log10(max(acc, 1e-10))
            out[m * MelSpec.N_FRAMES + t] = v.toFloat()
            if (v > maxLog) maxLog = v
        }
    }

    val floor = (maxLog - 8.0).toFloat()
    for (i in out.indices) {
        val v = if (out[i] < floor) floor else out[i]
        out[i] = (v + 4.0f) / 4.0f
    }
    return out
}

/**
 * العيّنةُ عند موضعٍ قد يخرج عن الصوت.
 *
 * والخارجُ عن حدود الصوت **ينعكس** لا يُصفَّر: ذاك ما يفعله الحشوُ
 * الانعكاسيُّ في whisper قبل التحويل، وتصفيرُه يُحدث قفزةً في أوّل
 * إطارٍ وآخره تظهر في الطيف.
 */
private fun sampleAt(audio: FloatArray, have: Int, i: Int): Double {
    val n = MelSpec.N_SAMPLES
    val k = when {
        i < 0 -> -i
        i >= n -> 2 * (n - 1) - i
        else -> i
    }
    //  ما بعد الصوت أصفارٌ — فالنافذةُ ثلاثون ثانيةً والتلاوةُ أقصر
    return if (k in 0 until have) audio[k].toDouble() else 0.0
}

/**
 * تحويلُ فورييه — يُقسَم ما دام الطولُ زوجيّاً، ثمّ مباشرٌ على الفرديّ.
 *
 * وهو نهجُ `whisper.cpp`: ٤٠٠ تُقسَم أربعَ مرّاتٍ إلى ٢٥، وعلى ٢٥
 * تحويلٌ مباشر. فلا يُحشى الطولُ إلى قوّةِ اثنين، ولا يُدفع ثمنُ
 * تحويلٍ مختلط الأساس.
 */
private fun fft(re: DoubleArray, im: DoubleArray, n: Int, outRe: DoubleArray, outIm: DoubleArray) {
    if (n == 1) {
        outRe[0] = re[0]
        outIm[0] = im[0]
        return
    }
    if (n % 2 != 0) {
        dft(re, im, n, outRe, outIm)
        return
    }

    val half = n / 2
    val evenRe = DoubleArray(half)
    val evenIm = DoubleArray(half)
    val oddRe = DoubleArray(half)
    val oddIm = DoubleArray(half)
    for (i in 0 until half) {
        evenRe[i] = re[2 * i]
        evenIm[i] = im[2 * i]
        oddRe[i] = re[2 * i + 1]
        oddIm[i] = im[2 * i + 1]
    }
    val eRe = DoubleArray(half)
    val eIm = DoubleArray(half)
    val oRe = DoubleArray(half)
    val oIm = DoubleArray(half)
    fft(evenRe, evenIm, half, eRe, eIm)
    fft(oddRe, oddIm, half, oRe, oIm)

    for (k in 0 until half) {
        val ang = -2.0 * PI * k / n
        val wr = cos(ang)
        val wi = sin(ang)
        val tr = wr * oRe[k] - wi * oIm[k]
        val ti = wr * oIm[k] + wi * oRe[k]
        outRe[k] = eRe[k] + tr
        outIm[k] = eIm[k] + ti
        outRe[k + half] = eRe[k] - tr
        outIm[k + half] = eIm[k] - ti
    }
}

/** تحويلٌ مباشرٌ — للطول الفرديّ وحدَه، وهو ٢٥ هنا. */
private fun dft(re: DoubleArray, im: DoubleArray, n: Int, outRe: DoubleArray, outIm: DoubleArray) {
    for (k in 0 until n) {
        var sr = 0.0
        var si = 0.0
        for (t in 0 until n) {
            val ang = -2.0 * PI * k * t / n
            val wr = cos(ang)
            val wi = sin(ang)
            sr += re[t] * wr - im[t] * wi
            si += re[t] * wi + im[t] * wr
        }
        outRe[k] = sr
        outIm[k] = si
    }
}
