package app.rafiqaldhikr.ui.sky

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/* ══════════════════════════════════════════════════════════════
   طقسُ مكانك — طبقةٌ تُضاف، لا شرطٌ للعمل

   **القاعدةُ التي لا تُكسر: التطبيق يعمل دون اتصال.** فالطقسُ زينةٌ
   فوق السماء المحسوبة، لا أساسٌ لها. بلا إنترنتٍ تبقى السماءُ تامّةً
   بشمسها وقمرها ونجومها — ويسقط المطرُ والضبابُ وحدَهما.

   والمصدر [Open-Meteo]: **بلا مفتاحٍ ولا تسجيلٍ ولا بطاقة**، وبياناته
   برخصة CC BY 4.0. اختيرَ لهذا بالذات — كلُّ بديلٍ يفرض على صاحب
   التطبيق أن يسجّل ويحمل مفتاحاً ويجدّده.

   > **قيدٌ أُصرّح به:** الاستعمالُ المجانيّ لواجهتهم **لغير التجاريّ**.
   > فإن رُفع التطبيق إلى المتجر ببيعٍ أو إعلان، وجب اشتراكٌ عندهم أو
   > مصدرٌ آخر. والبياناتُ نفسُها حرّةٌ بالعزو، والقيدُ على الواجهة.

   ولا مكتبةَ شبكةٍ جديدة: `HttpURLConnection` و`org.json` — وكلاهما
   مستعملٌ في التطبيق أصلاً (تنزيلُ خطوط المصحف، وأسماءُ السور).
══════════════════════════════════════════════════════════════ */

/**
 * حالُ الطقس مُهيَّأةً للرسم — كلُّ حقلٍ شدّةٌ من صفرٍ إلى واحد.
 *
 * وليست نسخةً من جواب الخادم: الخادمُ يعطي رمزاً ونِسَباً، وهذه ما
 * يحتاجه المُظلِّل بالضبط. فالتحويلُ يقع مرّةً هنا لا في كلّ إطار.
 */
data class SkyWeather(
    val cloud: Float = 0f,
    val rain: Float = 0f,
    val snow: Float = 0f,
    val fog: Float = 0f,
    val thunder: Boolean = false,
    val tempC: Float = Float.NaN,
    /** رمزُ WMO كما جاء — يُعرض اسمُه للمستخدم. */
    val code: Int = -1,
    val fetchedAt: Long = 0L,
) {
    val known: Boolean get() = code >= 0
}

object WeatherStore {

    private const val PREFS = "rafiq_weather"
    private const val FRESH_MS = 30 * 60_000L          // نصفُ ساعة
    private const val STALE_MS = 6 * 60 * 60_000L      // بعدها لا يُعرض

    /**
     * آخرُ طقسٍ محفوظ — يُقرأ فوراً بلا شبكة.
     *
     * فتُرسم السماءُ بحال الأمس القريب ريثما يصل الجديد، بدل أن تقفز
     * من سماءٍ صافيةٍ إلى ممطرةٍ أمام عينَي القارئ.
     */
    fun cached(context: Context): SkyWeather {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val at = p.getLong("at", 0L)
        if (at == 0L || System.currentTimeMillis() - at > STALE_MS) return SkyWeather()
        return SkyWeather(
            cloud = p.getFloat("cloud", 0f),
            rain = p.getFloat("rain", 0f),
            snow = p.getFloat("snow", 0f),
            fog = p.getFloat("fog", 0f),
            thunder = p.getBoolean("thunder", false),
            tempC = p.getFloat("temp", Float.NaN),
            code = p.getInt("code", -1),
            fetchedAt = at,
        )
    }

    /**
     * يجلب الطقسَ إن لزم. **لا يرمي أبداً**، ويعيد المحفوظَ عند الفشل.
     *
     * @return الحالُ المعروضة — جديدةً أو محفوظةً أو فارغة.
     */
    suspend fun refresh(context: Context, lat: Double, lng: Double): SkyWeather =
        withContext(Dispatchers.IO) {
            val old = cached(context)
            if (old.known && System.currentTimeMillis() - old.fetchedAt < FRESH_MS) return@withContext old

            val fresh = runCatching { fetch(lat, lng) }.getOrNull() ?: return@withContext old

            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putFloat("cloud", fresh.cloud).putFloat("rain", fresh.rain)
                .putFloat("snow", fresh.snow).putFloat("fog", fresh.fog)
                .putBoolean("thunder", fresh.thunder).putFloat("temp", fresh.tempC)
                .putInt("code", fresh.code).putLong("at", fresh.fetchedAt)
                .apply()
            fresh
        }

    private fun fetch(lat: Double, lng: Double): SkyWeather {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lng" +
                "&current=temperature_2m,weather_code,cloud_cover,precipitation,visibility" +
                "&timezone=auto",
        )
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        val body = try {
            if (conn.responseCode !in 200..299) return SkyWeather()
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }

        val cur = JSONObject(body).getJSONObject("current")
        return interpret(
            code = cur.optInt("weather_code", -1),
            cloudPct = cur.optDouble("cloud_cover", 0.0),
            precipMm = cur.optDouble("precipitation", 0.0),
            visibilityM = cur.optDouble("visibility", 50_000.0),
            tempC = cur.optDouble("temperature_2m", Double.NaN),
        )
    }

    /**
     * من رمز WMO إلى شدّاتِ الرسم.
     *
     * الرمزُ وحدَه لا يكفي: «مطر» رمزٌ واحدٌ لرذاذٍ ولوابل. فتُؤخذ
     * الشدّةُ من المطر المقيس بالمليمتر، والرمزُ يقرّر **نوعَ** ما
     * يسقط — ماءً أم ثلجاً — والغيمُ من نسبته المئوية مباشرةً.
     */
    internal fun interpret(
        code: Int,
        cloudPct: Double,
        precipMm: Double,
        visibilityM: Double,
        tempC: Double,
    ): SkyWeather {
        val wet = (precipMm / 2.2).coerceIn(0.0, 1.0).toFloat()

        val isSnow = code in 71..77 || code in 85..86
        val isRain = code in 51..67 || code in 80..82 || code in 95..99
        val isFog = code == 45 || code == 48

        //  الضبابُ يُقاس بالمدى المرئيّ لا بالرمز وحدَه: مدًى دون
        //  كيلومترين ضبابٌ يُرى وإن لم يُعلنه الرمز.
        val fogFromVis = ((4000.0 - visibilityM) / 3500.0).coerceIn(0.0, 1.0)

        return SkyWeather(
            cloud = (cloudPct / 100.0).coerceIn(0.0, 1.0).toFloat(),
            //  حدٌّ أدنى للشدّة: رمزُ مطرٍ بلا مليمتراتٍ مقيسةٍ يبقى مطراً
            //  خفيفاً يُرى — لا سماءً صافيةً تكذّب ما يراه من نافذته.
            rain = if (isRain) maxOf(wet, 0.22f) else 0f,
            snow = if (isSnow) maxOf(wet, 0.30f) else 0f,
            fog = maxOf(if (isFog) 0.75f else 0f, fogFromVis.toFloat()),
            thunder = code in 95..99,
            tempC = tempC.toFloat(),
            code = code,
            fetchedAt = System.currentTimeMillis(),
        )
    }
}
