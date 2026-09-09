package app.rafiqaldhikr.ui.hero

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/* ══════════════════════════════════════════════════════════════
   بطاقةُ الشاشة الأولى — يبدّلها صاحبُ التطبيق بلا تحديثٍ من المتجر

   **القاعدةُ التي لا تُكسر: التطبيق يعمل دون اتصال.** فالبطاقةُ طبقةٌ
   فوق المشهد المضمَّن لا بديلٌ عنه. بلا إنترنتٍ تُقرأ آخرُ بطاقةٍ
   وصلت من المحفوظ فوراً، وإن لم تصل واحدةٌ قطُّ بقي المشهدُ كما هو.

   ولا مكتبةَ جديدة: `HttpURLConnection` و`org.json` — نفسُ ما يجلب به
   `SkyWeather` الطقسَ اليوم.

   ═══ لماذا يُقرأ ولا يُكتب ═══

   المستخدمُ لا يملك تغييرَ شيء، لأنّ الملفَّ يُجلب بـGET فقط. ومفتاحُ
   التحكّم ليس في التطبيق أصلاً — بل صلاحيّةُ النشر على مُضيف صاحبِه.

   ═══ وما يأتي من الشبكة لا يُصدَّق ═══

   لو اختُرق حسابُ النشر لوصل نصٌّ أو صورةٌ إلى شاشة كلّ مستخدم. فكلُّ
   حقلٍ هنا **يُتحقَّق منه ويُقصّ**، ولا يُقبل ما خرج عن الحدود:

     • `src` **اسمُ ملفٍّ فقط** — لا رابطَ حرّاً ولا `../`. فما يُنزَّل
       لا يخرج أبداً عن مجلَّد صاحب التطبيق.
     • النوعُ والحركةُ من قائمةٍ مغلقة، وما سواهما يُهمَل.
     • أطوالٌ قصوى للنصوص، فلا يُغرَق المشهدُ بجدارِ كلام.
     • عددٌ أقصى للبطاقات، فلا يُستنزف الجهاز.

   ═══ ولا نصَّ دينيّاً هنا ═══

   ما يأتي من الشبكة **لا يمرّ على `tools/check_religious_sources.py`**.
   فهذا الحقلُ لكلام صاحب التطبيق وحدَه؛ وكلُّ نصٍّ مرويٍّ يُستدعى
   برقمه من أصول التطبيق المخرَّجة — وإلّا صار البابُ ثغرةً في وعد
   التطبيق كلِّه.
══════════════════════════════════════════════════════════════ */

/** ما تحمله البطاقة. وما خرج عن هذه القائمة يُقرأ `TEXT`. */
enum class HeroKind { TEXT, IMAGE, VIDEO, MISBAHA }

/**
 * بطاقةٌ واحدة. `from`/`to` تاريخان بصيغة `YYYY-MM-DD` شاملان للطرفين.
 */
data class HeroCard(
    val id: String,
    val from: String,
    val to: String,
    val kind: HeroKind = HeroKind.TEXT,
    val anim: HeroAnim = HeroAnim.PEN,
    val title: String = "",
    val note: String = "",
    /** اسمُ ملفٍّ في مجلَّد المُضيف — لا رابط. فارغٌ إن لم تحمل وسائط. */
    val src: String = "",
    val actionLabel: String = "",
    val actionRoute: String = "",
)

object HeroStore {

    private const val PREFS = "rafiq_hero"
    private const val FRESH_MS = 3 * 60 * 60_000L        // ثلاثُ ساعات
    private const val MAX_CARDS = 40
    private const val MAX_TITLE = 48
    private const val MAX_NOTE = 120
    private const val MAX_LABEL = 32
    private const val MAX_BODY = 96 * 1024               // ملفُّ الإعداد
    private const val MAX_MEDIA = 6 * 1024 * 1024        // صورةٌ أو مقطع

    private val ID_RE = Regex("^[A-Za-z0-9_-]{1,24}$")
    private val DATE_RE = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    //  اسمُ ملفٍّ لا مسار: لا شرطةَ مائلة، ولا نقطتان متتاليتان.
    private val SRC_RE = Regex("^[A-Za-z0-9_-]{1,48}\\.[A-Za-z0-9]{2,5}$")
    private val ROUTE_RE = Regex("^[A-Za-z0-9/_-]{0,48}$")

    /** آخرُ ما وصل، يُقرأ فوراً بلا شبكة. فارغةٌ إن لم يصل شيءٌ قطّ. */
    fun cached(context: Context, today: String = todayIso()): HeroCard? {
        val body = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("body", null) ?: return null
        return runCatching { pick(parse(body), today) }.getOrNull()
    }

    /**
     * يجلب الإعدادَ إن مضى عليه أكثرُ من ثلاث ساعات. **لا يرمي أبداً**،
     * ويعيد المحفوظَ عند أيّ فشل.
     */
    suspend fun refresh(context: Context, baseUrl: String): HeroCard? =
        withContext(Dispatchers.IO) {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val at = p.getLong("at", 0L)
            if (System.currentTimeMillis() - at < FRESH_MS) return@withContext cached(context)

            val base = baseUrl.trimEnd('/')
            val etag = p.getString("etag", null)
            val got = runCatching { fetch("$base/manifest.json", etag) }.getOrNull()

            //  ٣٠٤ يعني «لم يتغيّر»: نجدّد الختمَ الزمنيّ فقط فلا نسأل
            //  ثانيةً قبل ثلاث ساعات — وهو طلبٌ بحجم سطرٍ لا صفحة.
            if (got != null) {
                if (got.body != null && runCatching { parse(got.body) }.isSuccess) {
                    p.edit().putString("body", got.body).putString("etag", got.etag)
                        .putLong("at", System.currentTimeMillis()).apply()
                } else {
                    p.edit().putLong("at", System.currentTimeMillis()).apply()
                }
            }

            val card = cached(context)
            if (card != null && card.src.isNotEmpty()) {
                runCatching { media(context, base, card.src) }
            }
            card
        }

    /** الملفُّ المنزَّل لهذه البطاقة، أو `null` إن لم يصل بعد. */
    fun mediaFile(context: Context, src: String): File? {
        if (!SRC_RE.matches(src)) return null
        val f = File(File(context.filesDir, "hero"), src)
        return if (f.isFile && f.length() > 0) f else null
    }

    /**
     * ينزّل الوسائط مرّةً واحدة. **الاسمُ هو النسخة**: لا يُعاد التنزيل
     * لملفٍّ موجود، فغيِّر الاسمَ إذا غيّرتَ الصورة.
     */
    private fun media(context: Context, base: String, src: String): File? {
        if (!SRC_RE.matches(src)) return null
        val dir = File(context.filesDir, "hero").apply { mkdirs() }
        val f = File(dir, src)
        if (f.isFile && f.length() > 0) return f

        val conn = (URL("$base/$src").openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000; readTimeout = 20_000
        }
        val tmp = File(dir, "$src.part")
        try {
            if (conn.responseCode !in 200..299) return null
            if (conn.contentLength > MAX_MEDIA) return null
            var n = 0L
            conn.inputStream.use { input ->
                tmp.outputStream().use { out ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                        val r = input.read(buf)
                        if (r < 0) break
                        n += r
                        if (n > MAX_MEDIA) return null            // كذبَ الترويسةَ
                        out.write(buf, 0, r)
                    }
                }
            }
        } catch (_: Exception) {
            return null
        } finally {
            conn.disconnect()
        }
        //  لا يُسمّى باسمه النهائيّ إلّا بعد اكتمالِه، فلا يُقرأ نصفُ ملفّ.
        if (!tmp.renameTo(f)) { tmp.delete(); return null }
        prune(dir, keep = src)
        return f
    }

    /** لا يُترك في الجهاز إلّا ملفُّ البطاقة الحاليّة وأحدثُ أربعةٍ قبله. */
    private fun prune(dir: File, keep: String) {
        val files = dir.listFiles()?.filter { it.isFile && it.name != keep } ?: return
        files.sortedByDescending { it.lastModified() }.drop(4).forEach { it.delete() }
    }

    private class Got(val body: String?, val etag: String?)

    private fun fetch(url: String, etag: String?): Got? {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            etag?.let { setRequestProperty("If-None-Match", it) }
        }
        try {
            if (conn.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) return Got(null, etag)
            if (conn.responseCode !in 200..299) return null
            if (conn.contentLength > MAX_BODY) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            if (body.length > MAX_BODY) return null
            return Got(body, conn.getHeaderField("ETag"))
        } catch (_: Exception) {
            return null
        } finally {
            conn.disconnect()
        }
    }

    /** أوّلُ بطاقةٍ يقع اليومُ في مداها. */
    internal fun pick(cards: List<HeroCard>, today: String): HeroCard? =
        cards.firstOrNull { today >= it.from && today <= it.to }

    /**
     * يقرأ الإعدادَ ويطرح كلَّ بطاقةٍ لا تستوفي الشروط.
     *
     * بطاقةٌ واحدةٌ فاسدةٌ لا تُسقط البقيّة: تُهمَل وحدَها. فخطأٌ مطبعيٌّ
     * في أسبوعٍ قادمٍ لا يُطفئ أسبوعَ اليوم.
     */
    internal fun parse(body: String): List<HeroCard> {
        val root = JSONObject(body)
        val arr = root.optJSONArray("cards") ?: return emptyList()
        val out = ArrayList<HeroCard>(minOf(arr.length(), MAX_CARDS))
        for (i in 0 until minOf(arr.length(), MAX_CARDS)) {
            val o = arr.optJSONObject(i) ?: continue
            val act = o.optJSONObject("action")
            out += card(
                id = o.optString("id"),
                from = o.optString("from"),
                to = o.optString("to"),
                type = o.optString("type"),
                anim = o.optString("anim"),
                title = o.optString("title"),
                note = o.optString("note"),
                src = o.optString("src"),
                label = act?.optString("label").orEmpty(),
                route = act?.optString("route").orEmpty(),
            ) ?: continue
        }
        return out
    }

    /**
     * قواعدُ القبول كلُّها في مكانٍ واحدٍ خالٍ من JSON — فتُختبَر وحدَها،
     * وهي الجزءُ الذي يهمّ: ما يأتي من الشبكة لا يُصدَّق.
     *
     * @return البطاقةَ إن استوفت كلَّ شرط، و`null` إن أخلّت بواحد.
     */
    internal fun card(
        id: String, from: String, to: String,
        type: String = "", anim: String = "",
        title: String = "", note: String = "", src: String = "",
        label: String = "", route: String = "",
    ): HeroCard? {
        if (!ID_RE.matches(id) || !DATE_RE.matches(from) || !DATE_RE.matches(to)) return null
        if (from > to) return null

        val kind = when (type) {
            "image" -> HeroKind.IMAGE
            "video" -> HeroKind.VIDEO
            "misbaha" -> HeroKind.MISBAHA
            else -> HeroKind.TEXT
        }
        /*  نوعٌ لا يعرف التطبيقُ رسمَه **يُهمَل ولا يُعرَض بديلاً صامتاً**:
         *  أن ترى بطاقةً غيرَ التي كتبتَ أسوأُ من أن ترى المشهدَ المضمَّن.
         *  والصيغةُ تحتمله فيُقرأ يومَ يُبنى. */
        if (kind == HeroKind.VIDEO || kind == HeroKind.MISBAHA) return null
        //  بطاقةٌ تَعِد بصورةٍ ولا تسمّي ملفّاً سليماً تُهمَل — وإلّا ظهرت
        //  رقعةٌ لا يفهم صاحبُها لماذا خالفت ما كتب.
        if (kind == HeroKind.IMAGE && !SRC_RE.matches(src)) return null

        return HeroCard(
            id = id, from = from, to = to, kind = kind,
            anim = HeroAnim.of(anim),
            title = title.trim().take(MAX_TITLE),
            note = note.trim().take(MAX_NOTE),
            src = if (kind == HeroKind.IMAGE) src else "",
            actionLabel = label.trim().take(MAX_LABEL),
            actionRoute = if (ROUTE_RE.matches(route)) route else "",
        )
    }

    private fun todayIso(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH),
        )
    }
}
