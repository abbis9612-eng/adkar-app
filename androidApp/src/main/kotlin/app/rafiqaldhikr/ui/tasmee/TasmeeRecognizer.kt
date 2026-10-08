package app.rafiqaldhikr.ui.tasmee

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlin.math.exp
import kotlin.math.ln

/*
 * المُنصِت — صوتٌ ← كلامٌ وثقة
 * ═══════════════════════════
 *
 * نقلاً عن `tools/tasmee_reference.py` خطوةً بخطوة. وذاك يعمل على
 * الأصول المشحونة نفسِها، فمن شكّ في خطوةٍ هنا شغّله وقابل.
 *
 * ═══ ومصيدتان أُسقطتا في بايثون قبل أن تُسقطا يوماً هنا ═══
 *
 * كلتاهما تسقط **في الخطوة الثانية** لا الأولى — أي بعد أن يبدو كلُّ
 * شيءٍ سليماً — ورسالتُها تتكلّم عن `MatMul` في طبقةٍ داخليّة فلا تدلّ
 * على السبب أبداً:
 *
 * **١ · بُعدٌ رمزيٌّ واحدٌ يُربط بقيمتين.** ذاكرةُ المرمِّز تحمل البُعدَ
 * `encoder_sequence_length` نفسَه الذي يحمله التمثيلُ المخفيّ (١٥٠٠).
 * فمن مرّرها فارغةً بطول صفرٍ رُبط البُعدُ بصفرٍ وبألفٍ وخمسِمئةٍ معاً.
 * فتُمرَّر في الممرّ الأوّل **أصفاراً بطول ١٥٠٠** — لا تُقرأ، ولكنّ
 * شكلَها يجب أن يوافق. وذاكرةُ المفكِّك وحدَها تبدأ فارغة.
 *
 * **٢ · ذاكرةُ المرمِّز تُؤخذ من الممرّ الأوّل مرّةً وتبقى.** فالمفكِّكُ
 * المدموج لا يُعيد حسابها في ممرّ الذاكرة، ويُخرج مكانها موتوراً
 * مشوَّهاً `(0, 6, 1, 64)`. ومن أعاده إليه سقط.
 *
 * ═══ والثقةُ ليست زينة ═══
 *
 * [Heard.confidence] هي ما يُبنى عليه حكمُ [app.rafiq.domain.model.tasmee]:
 * دونها حدٌّ معلومٌ يقول التطبيقُ «لم أتبيّن» ولا يقول «أخطأت». وهي
 * متوسّطُ لوغاريتم احتمال الرموز مرفوعاً — أي: كم كان النموذجُ واثقاً
 * في كلّ رمزٍ أخرجه، لا كم كان كلامُه معقولاً.
 */

/** ما سمعه النموذج. */
data class Heard(val text: String, val confidence: Float)

/**
 * مُنصِتٌ يعمل على الجهاز بلا شبكة.
 *
 * ثقيلُ الإنشاء (أربعون ميغابايت تُحمَّل)، فيُنشأ مرّةً ويُغلَق بـ[close].
 */
class TasmeeRecognizer private constructor(
    private val env: OrtEnvironment,
    private val encoder: OrtSession,
    private val decoder: OrtSession,
    private val filters: FloatArray,
    private val tokens: WhisperTokens,
    private val ids: SpecialIds,
) : AutoCloseable {

    /** أرقامُ الرموز الخاصّة — من `MODEL.json` لا مكتوبةً في الكود. */
    data class SpecialIds(
        val startOfTranscript: Int,
        val arabic: Int,
        val transcribe: Int,
        val noTimestamps: Int,
        val endOfText: Int,
    )

    /**
     * يُنصِت إلى تلاوةٍ ويرجع بما سمعه وبثقته.
     *
     * @param audio عيّناتٌ ١٦ ك.هرتز أحاديّةٌ في [-1, 1]
     * @param maxTokens سقفُ الرموز — آيةٌ طويلةٌ نحو ستّين رمزاً
     */
    fun listen(audio: FloatArray, maxTokens: Int = 96): Heard {
        val feats = logMel(audio, filters)

        val hidden: FloatArray
        OnnxTensor.createTensor(
            env, FloatBuffer.wrap(feats),
            longArrayOf(1, MelSpec.N_MELS.toLong(), MelSpec.N_FRAMES.toLong()),
        ).use { input ->
            encoder.run(mapOf("input_features" to input)).use { out ->
                hidden = floats(out.get(0) as OnnxTensor)
            }
        }

        val encLen = hidden.size / D_MODEL
        val hiddenShape = longArrayOf(1, encLen.toLong(), D_MODEL.toLong())

        val produced = ArrayList<Int>(maxTokens)
        var logProbSum = 0.0

        //  تُجمع كلُّ الموتورات المفتوحة لتُغلَق معاً — فنسيانُ واحدٍ
        //  يُبقي عشراتَ الميغابايت محجوزةً خارج كومة الجهاز.
        val open = ArrayList<OnnxTensor>()
        try {
            val hiddenTensor = OnnxTensor.createTensor(
                env, FloatBuffer.wrap(hidden), hiddenShape,
            ).also(open::add)

            //  أصفارٌ بطول ١٥٠٠ — المصيدةُ الأولى. وموتورٌ واحدٌ يُشار
            //  إليه من ثمانية مداخل: يُقرأ ولا يُكتب فيه.
            val zeroEnc = OnnxTensor.createTensor(
                env, FloatBuffer.allocate(HEADS * encLen * HEAD_DIM),
                longArrayOf(1, HEADS.toLong(), encLen.toLong(), HEAD_DIM.toLong()),
            ).also(open::add)
            val emptyDec = OnnxTensor.createTensor(
                env, FloatBuffer.allocate(0),
                longArrayOf(1, HEADS.toLong(), 0, HEAD_DIM.toLong()),
            ).also(open::add)

            var inputIds: LongArray = longArrayOf(
                ids.startOfTranscript.toLong(), ids.arabic.toLong(),
                ids.transcribe.toLong(), ids.noTimestamps.toLong(),
            )
            var decPast: Array<FloatArray>? = null
            var decPastLen = 0
            var encPast: Array<FloatArray>? = null

            for (step in 0 until maxTokens) {
                val feed = HashMap<String, OnnxTensor>(24)
                feed["encoder_hidden_states"] = hiddenTensor
                feed["input_ids"] = OnnxTensor.createTensor(
                    env, LongBuffer.wrap(inputIds),
                    longArrayOf(1, inputIds.size.toLong()),
                ).also(open::add)
                feed["use_cache_branch"] = OnnxTensor.createTensor(
                    env, booleanArrayOf(decPast != null),
                ).also(open::add)

                for (l in 0 until LAYERS) {
                    for ((j, kv) in KV.withIndex()) {
                        val dIdx = l * 2 + j
                        feed["past_key_values.$l.decoder.$kv"] = decPast?.let {
                            OnnxTensor.createTensor(
                                env, FloatBuffer.wrap(it[dIdx]),
                                longArrayOf(1, HEADS.toLong(), decPastLen.toLong(), HEAD_DIM.toLong()),
                            ).also(open::add)
                        } ?: emptyDec
                        feed["past_key_values.$l.encoder.$kv"] = encPast?.let {
                            OnnxTensor.createTensor(
                                env, FloatBuffer.wrap(it[dIdx]),
                                longArrayOf(1, HEADS.toLong(), encLen.toLong(), HEAD_DIM.toLong()),
                            ).also(open::add)
                        } ?: zeroEnc
                    }
                }

                var next: Int
                decoder.run(feed).use { out ->
                    val logits = out.get("logits").get() as OnnxTensor
                    val buf = logits.floatBuffer
                    val vocab = buf.remaining() / inputIds.size
                    val base = (inputIds.size - 1) * vocab

                    //  أقصى احتمالٍ وسوفتماكس في مرورين — لا يُبنى
                    //  مصفوفٌ بحجم المعجم في كلّ خطوة.
                    var top = 0
                    var topLogit = -Float.MAX_VALUE
                    for (v in 0 until vocab) {
                        val x = buf.get(base + v)
                        if (x > topLogit) { topLogit = x; top = v }
                    }
                    var sum = 0.0
                    for (v in 0 until vocab) sum += exp((buf.get(base + v) - topLogit).toDouble())
                    next = top
                    if (top != ids.endOfText) logProbSum += -ln(sum)

                    if (top != ids.endOfText) {
                        //  ذاكرةُ المفكِّك تُؤخذ كلَّ خطوة
                        val d = Array(LAYERS * 2) { FloatArray(0) }
                        for (l in 0 until LAYERS) {
                            for ((j, kv) in KV.withIndex()) {
                                d[l * 2 + j] = floats(
                                    out.get("present.$l.decoder.$kv").get() as OnnxTensor,
                                )
                            }
                        }
                        //  وذاكرةُ المرمِّز **من الممرّ الأوّل وحدَه** —
                        //  المصيدةُ الثانية.
                        if (encPast == null) {
                            val e = Array(LAYERS * 2) { FloatArray(0) }
                            for (l in 0 until LAYERS) {
                                for ((j, kv) in KV.withIndex()) {
                                    e[l * 2 + j] = floats(
                                        out.get("present.$l.encoder.$kv").get() as OnnxTensor,
                                    )
                                }
                            }
                            encPast = e
                        }
                        decPastLen += inputIds.size
                        decPast = d
                    }
                }

                if (next == ids.endOfText) break
                produced.add(next)
                inputIds = longArrayOf(next.toLong())
            }
        } finally {
            for (t in open) runCatching { t.close() }
        }

        val confidence =
            if (produced.isEmpty()) 0f
            else exp(logProbSum / produced.size).toFloat()
        return Heard(tokens.decode(produced), confidence)
    }

    override fun close() {
        runCatching { decoder.close() }
        runCatching { encoder.close() }
    }

    companion object {
        private const val LAYERS = 4
        private const val HEADS = 6
        private const val HEAD_DIM = 64
        private const val D_MODEL = 384
        private val KV = listOf("key", "value")

        /** مجلّدُ الأصول في الحزمة. */
        private const val DIR = "tasmee"

        /**
         * يُنشئ المُنصِت من الأصول المشحونة — أو `null` إن لم تُشحن.
         *
         * و`null` ليست عطباً: التسميعُ أصولُه أربعون ميغابايت، ومن بنى
         * المستودعَ قبل أن يُشغّل `tools/build_tasmee_model.py` لا تكون
         * عنده. فتُخفى الميزةُ ولا يسقط التطبيق.
         */
        fun createOrNull(ctx: Context): TasmeeRecognizer? = runCatching {
            val meta0 = org.json.JSONObject(
                ctx.assets.open("$DIR/MODEL.json").use { it.readBytes().toString(Charsets.UTF_8) },
            )
            val sums = meta0.getJSONObject("sha256")
            val enc = unpack(ctx, "encoder_int8.onnx", sums.getString("encoder_int8.onnx"))
            val dec = unpack(ctx, "decoder_int8.onnx", sums.getString("decoder_int8.onnx"))

            val filters = ctx.assets.open("$DIR/mel_filters.bin").use { it.readBytes() }
                .let { bytes ->
                    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                    FloatArray(bytes.size / 4) { buf.getFloat(it * 4) }
                }
            val tokens = ctx.assets.open("$DIR/tokens.txt").use {
                WhisperTokens.parse(it.readBytes().toString(Charsets.UTF_8))
            }
            val meta = meta0.getJSONObject("tokens")
            val ids = SpecialIds(
                startOfTranscript = meta.getInt("startoftranscript"),
                arabic = meta.getInt("ar"),
                transcribe = meta.getInt("transcribe"),
                noTimestamps = meta.getInt("notimestamps"),
                endOfText = meta.getInt("endoftext"),
            )

            val env = OrtEnvironment.getEnvironment()
            val opts = OrtSession.SessionOptions().apply {
                //  نواتان: الجهازُ قد يكون في يد صاحبه وهو يصلّي، ولا
                //  يُحمَّل المعالجُ كلُّه لتسميعِ آية.
                setIntraOpNumThreads(2)
            }
            TasmeeRecognizer(
                env = env,
                encoder = env.createSession(enc.absolutePath, opts),
                decoder = env.createSession(dec.absolutePath, opts),
                filters = filters,
                tokens = tokens,
                ids = ids,
            )
        }.getOrNull()

        /**
         * ينسخ نموذجاً من الحزمة إلى القرص مرّةً.
         *
         * و`OrtSession` تقبل البايتات، لكنّ ثلاثين ميغابايتاً في كومة
         * جافا لا تُطلب: تُنسخ مرّةً ثمّ يُقرأ الملفُّ بالمسار فتُسقَط
         * النسخةُ عن الكومة.
         *
         * **والبصمةُ في الاسم** لا في مقارنةِ حجمٍ ولا تاريخ: فنموذجٌ
         * يُستبدل في تحديثٍ يصير اسمُه غيرَ اسمه، فتُنسخ النسخةُ
         * الجديدةُ ولا يبقى التطبيقُ يُنصِت بنموذجٍ قديمٍ على القرص.
         * وما بقي من القديم يُحذف هنا.
         */
        private fun unpack(ctx: Context, name: String, sha: String): File {
            val dir = File(ctx.filesDir, DIR).apply { mkdirs() }
            val stem = name.removeSuffix(".onnx")
            val dst = File(dir, "$stem.${sha.take(12)}.onnx")
            if (dst.exists() && dst.length() > 0L) return dst

            dir.listFiles()?.forEach { old ->
                if (old.name.startsWith("$stem.") && old.name != dst.name) old.delete()
            }
            val tmp = File(dir, "${dst.name}.part")
            ctx.assets.open("$DIR/$name").use { src ->
                tmp.outputStream().use { src.copyTo(it) }
            }
            //  نسخٌ إلى مؤقّتٍ ثمّ تسميةٌ: فنسخةٌ انقطعت في منتصفها لا
            //  تُقرأ بعد ذلك كأنّها نموذجٌ تامّ.
            if (!tmp.renameTo(dst)) {
                tmp.delete()
                error("could not place ${dst.name}")
            }
            return dst
        }

        private fun floats(t: OnnxTensor): FloatArray {
            val buf = t.floatBuffer
            val out = FloatArray(buf.remaining())
            buf.get(out)
            return out
        }
    }
}
