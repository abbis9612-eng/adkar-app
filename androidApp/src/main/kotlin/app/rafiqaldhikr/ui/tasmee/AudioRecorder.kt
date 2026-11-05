package app.rafiqaldhikr.ui.tasmee

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/*
 * المسجِّل — ميكروفونٌ ← عيّنات
 * ═══════════════════════════
 *
 * النموذجُ يريد ١٦ ك.هرتز أحاديّةً في [-1, 1]. والجهازُ يعطي ١٦ بتاً
 * صحيحةً. فهذا يترجم بينهما ولا يفعل غيرَ ذلك.
 *
 * ═══ ولماذا يُقصّ عند ثلاثين ثانية ═══
 *
 * نافذةُ النموذج ثلاثون ثانيةً لا تزيد. وما بعدها **لا يُسمَع أصلاً** —
 * فلو تُرك التسجيلُ مفتوحاً لظنّ القارئُ أنّه يُسمَّع وهو يقرأ في
 * الفراغ. فيُقطع عند الحدّ ويُقال له.
 *
 * ═══ ولا يُحفظ شيءٌ على القرص ═══
 *
 * التسجيلُ يبقى في الذاكرة ويُمحى بانتهائه. وصوتُ مسلمٍ يُسمِّع حفظَه
 * أمانةٌ لا تُكتب في ملفٍّ لا يعرف به.
 */

/** أقصى ما يُسجَّل — نافذةُ النموذج نفسُها. */
const val MAX_RECORD_SECONDS = 30

/**
 * تسجيلٌ من الميكروفون إلى عيّنات.
 *
 * ويُطلب إذنُ `RECORD_AUDIO` قبل [start] — ولا يُطلب في غير شاشة
 * التسميع، فالتطبيقُ كلُّه لا يحتاج ميكروفوناً.
 */
class AudioRecorder {

    private var record: AudioRecord? = null
    private val chunks = ArrayList<FloatArray>()
    private var total = 0

    /** هل التسجيلُ جارٍ؟ */
    val recording: Boolean get() = record != null

    /** كم ثانيةً سُجّلت. */
    val seconds: Float get() = total.toFloat() / MelSpec.SAMPLE_RATE

    /** هل بلغ الحدَّ فوجب القطع؟ */
    val full: Boolean get() = total >= MelSpec.SAMPLE_RATE * MAX_RECORD_SECONDS

    /**
     * يبدأ التسجيل.
     *
     * @return false إن تعذّر — بلا إذنٍ أو بميكروفونٍ مشغولٍ بنداءٍ
     *         هاتفيّ. ولا يُرمى استثناءٌ: تعذّرُ التسجيل حالةٌ تُعرَض
     *         للقارئ لا عطبٌ يُسقط الشاشة.
     */
    @SuppressLint("MissingPermission")
    fun start(): Boolean = runCatching {
        stop()
        chunks.clear()
        total = 0

        val min = AudioRecord.getMinBufferSize(
            MelSpec.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (min <= 0) return false
        val r = AudioRecord(
            //  `VOICE_RECOGNITION` لا `MIC`: يُعطّل معالجاتِ النداء
            //  الهاتفيّ التي تقطع المدَّ وتخفض الهامس — وكلاهما في
            //  التلاوة معنى.
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MelSpec.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            min * 4,
        )
        if (r.state != AudioRecord.STATE_INITIALIZED) {
            r.release()
            return false
        }
        r.startRecording()
        record = r
        true
    }.getOrDefault(false)

    /**
     * يقرأ ما وصل من الميكروفون — يُنادى في حلقةٍ على خيطٍ خلفيّ.
     *
     * @return false إن بلغ الحدَّ فوجب القطع
     */
    fun drain(): Boolean {
        val r = record ?: return false
        val buf = ShortArray(2048)
        val n = runCatching { r.read(buf, 0, buf.size) }.getOrDefault(0)
        if (n > 0) {
            val room = MelSpec.SAMPLE_RATE * MAX_RECORD_SECONDS - total
            val take = if (n < room) n else room
            if (take > 0) {
                //  ١٦ بتاً صحيحةً ← عوّامةٌ في [-1, 1]. والقاسمُ 32768
                //  لا 32767: مدى `Short` غيرُ متناظر، والقسمةُ على
                //  الأصغر تُشبع الأدنى.
                chunks.add(FloatArray(take) { buf[it] / 32768f })
                total += take
            }
        }
        return !full
    }

    /** يُنهي التسجيلَ ويرجع بالعيّنات كلِّها. */
    fun stop(): FloatArray {
        record?.let { r ->
            runCatching { r.stop() }
            runCatching { r.release() }
        }
        record = null
        if (total == 0) return FloatArray(0)
        val out = FloatArray(total)
        var at = 0
        for (c in chunks) {
            c.copyInto(out, at)
            at += c.size
        }
        chunks.clear()
        total = 0
        return out
    }
}
