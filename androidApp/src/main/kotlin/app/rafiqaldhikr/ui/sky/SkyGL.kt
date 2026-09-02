package app.rafiqaldhikr.ui.sky

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   سماءٌ تُحسب على المعالج الرسوميّ

   مربّعٌ واحدٌ يملأ الشاشة، وكلُّ بكسلٍ فيه شعاعٌ يُحسب في `SkyShader`.
   لا نموذجَ ثلاثيّ ولا صورة ولا مكتبة — وحجمُ ما أُضيف إلى التطبيق
   بضعةُ كيلوبايتاتٍ من نصّ.

   **ثلاثةُ قراراتٍ في الأداء**، لأنّ هذه شاشةٌ تُفتح عشراتِ المرّات
   يومياً ولا يجوز أن تأكل البطارية:

     ١) يُصيَّر بنصف الدقّة (`setFixedSize`) ثمّ يُمدَّد. السماءُ ناعمةٌ
        بطبعها فلا يُرى الفرق، والعملُ يصير رُبعاً.
     ٢) `RENDERMODE_WHEN_DIRTY` مع نبضةٍ موقوتة — لا حلقةً حرّةً بستّين
        إطاراً. اثنا عشر إطاراً في الثانية تكفي لغيمٍ يزحف ونجمٍ يتلألأ.
     ٣) ويتوقّف تماماً في الخلفية: لا نبضة، ولا مستشعر، ولا سياقُ رسم.

   ومن خفض الحركة في إعدادات جهازه، سكن كلُّ شيء: النبضةُ تتوقّف بعد
   إطارٍ واحد، والتلألؤُ والزحفُ يُطفآن في المُظلِّل نفسِه.
══════════════════════════════════════════════════════════════ */

private const val FPS_MS = 1000L / 12L

internal class SkyRenderer : GLSurfaceView.Renderer {

    /** تُكتب من خيط الواجهة وتُقرأ من خيط الرسم — لذلك `@Volatile`. */
    @Volatile var sunAltDeg = -5.0
    @Volatile var sunAzDeg = 270.0
    @Volatile var moonAltDeg = 20.0
    @Volatile var moonAzDeg = 100.0
    @Volatile var moonLit = 0.4f
    @Volatile var moonWaxing = true
    @Volatile var tiltX = 0f
    @Volatile var tiltY = 0f
    @Volatile var reduced = false
    @Volatile var weather = SkyWeather()

    private var program = 0
    private var uRes = 0; private var uTime = 0; private var uSun = 0
    private var uMoon = 0; private var uMoonLit = 0; private var uMoonSign = 0
    private var uNight = 0; private var uTilt = 0; private var uReduced = 0
    private var uCloud = 0; private var uRain = 0; private var uSnow = 0
    private var uFog = 0; private var uFlash = 0
    private var aPos = 0

    private var w = 1f; private var h = 1f
    private val t0 = System.nanoTime()

    private val quad = ByteBuffer.allocateDirect(8 * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)); position(0)
        }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = link(VERTEX_SRC, FRAGMENT_SRC)
        if (program == 0) return
        aPos      = GLES20.glGetAttribLocation(program, "aPos")
        uRes      = GLES20.glGetUniformLocation(program, "uRes")
        uTime     = GLES20.glGetUniformLocation(program, "uTime")
        uSun      = GLES20.glGetUniformLocation(program, "uSun")
        uMoon     = GLES20.glGetUniformLocation(program, "uMoon")
        uMoonLit  = GLES20.glGetUniformLocation(program, "uMoonLit")
        uMoonSign = GLES20.glGetUniformLocation(program, "uMoonSign")
        uNight    = GLES20.glGetUniformLocation(program, "uNight")
        uTilt     = GLES20.glGetUniformLocation(program, "uTilt")
        uReduced  = GLES20.glGetUniformLocation(program, "uReduced")
        uCloud    = GLES20.glGetUniformLocation(program, "uCloud")
        uRain     = GLES20.glGetUniformLocation(program, "uRain")
        uSnow     = GLES20.glGetUniformLocation(program, "uSnow")
        uFog      = GLES20.glGetUniformLocation(program, "uFog")
        uFlash    = GLES20.glGetUniformLocation(program, "uFlash")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        w = width.toFloat(); h = height.toFloat()
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        /*  تعذّر المُظلِّل — تبقى السماءُ بلونها الصحيح لا بأزرقَ ثابت.
         *
         *  `skyColors` هي طبقاتُ الشفق التي كانت تُستعمل في الرسم على
         *  القماش، وقد بقيت لهذا بالذات: جهازٌ يعجز عن المُظلِّل يرى
         *  لونَ وقته على الأقلّ — فجراً أحمرَ وليلاً أزرقَ عميقاً. */
        if (program == 0) {
            val c = skyColors(sunAltDeg.toFloat()).second
            GLES20.glClearColor(c.red, c.green, c.blue, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            return
        }
        GLES20.glUseProgram(program)

        val sun = dirOf(sunAltDeg, sunAzDeg)
        val moon = dirOf(moonAltDeg, moonAzDeg)
        //  الليلُ يبدأ بعد الغروب بدرجتين ويتمّ عند ‎−١٢°: نفسُ الحدود
        //  التي تُحسب بها المواقيت، فلا تفترق السماءُ عن الأذان.
        val night = (((-sunAltDeg) - 2.0) / 10.0).coerceIn(0.0, 1.0).toFloat()

        GLES20.glUniform2f(uRes, w, h)
        val seconds = (System.nanoTime() - t0) / 1_000_000_000f
        GLES20.glUniform1f(uTime, seconds)
        GLES20.glUniform3f(uSun, sun[0], sun[1], sun[2])
        GLES20.glUniform3f(uMoon, moon[0], moon[1], moon[2])
        GLES20.glUniform1f(uMoonLit, moonLit)
        GLES20.glUniform1f(uMoonSign, if (moonWaxing) 1f else -1f)
        GLES20.glUniform1f(uNight, night)
        GLES20.glUniform2f(uTilt, tiltX, tiltY)
        GLES20.glUniform1f(uReduced, if (reduced) 1f else 0f)

        val w = weather
        GLES20.glUniform1f(uCloud, w.cloud)
        GLES20.glUniform1f(uRain, w.rain)
        GLES20.glUniform1f(uSnow, w.snow)
        GLES20.glUniform1f(uFog, w.fog)
        GLES20.glUniform1f(uFlash, if (w.thunder && !reduced) flashAt(seconds) else 0f)

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, quad)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(aPos)
    }

    /**
     * ومضةُ البرق — متقطّعةٌ لا دورية.
     *
     *  البرقُ الحقيقيّ لا ينبض بإيقاع. فالثانيةُ تُقسَّم إلى نوافذَ
     *  بأطوالٍ مختلفة، وتُختار منها القليلةُ عشوائياً — فتقع الومضةُ
     *  حين لا تتوقّعها، ثمّ تخبو بأسٍّ سريعٍ كما يخبو البرق.
     *
     *  وتُطفأ كلَّها عند خفض الحركة: ومضةٌ مفاجئةٌ في شاشةِ ذكرٍ ليست
     *  إزعاجاً فحسب — بعضُ الناس لا يحتملها.
     */
    private fun flashAt(t: Float): Float {
        val slot = kotlin.math.floor(t / 3.7f)
        val seed = ((slot * 12.9898f).mod(1f) * 43758.5453f).mod(1f)
        if (seed < 0.62f) return 0f
        val local = t - slot * 3.7f - seed * 2.4f
        if (local < 0f || local > 0.5f) return 0f
        //  ومضتان متتاليتان — البرقُ نادراً ما يومض مرّةً واحدة.
        val a = kotlin.math.exp(-local * 22f)
        val b = if (local > 0.12f) kotlin.math.exp(-(local - 0.12f) * 26f) * 0.7f else 0f
        return ((a + b) * 0.85f).coerceIn(0f, 1f)
    }

    /**
     * من (ارتفاع، سَمْت) إلى متّجهٍ في فضاء المشهد.
     *
     * والسَّمْتُ يُقاس من الشمال شرقاً، والمشهدُ ينظر جنوباً — فالجيبُ
     * والجيبُ تمامُ مقلوبان عمّا يُتوقّع. وهذا هو الموضعُ الذي كانت
     * تُخطئ فيه الشمسُ فتشرق من جهة الغروب.
     */
    private fun dirOf(altDeg: Double, azDeg: Double): FloatArray {
        val a = altDeg * PI / 180.0
        val z = (azDeg - 180.0) * PI / 180.0
        return floatArrayOf(
            (cos(a) * sin(z)).toFloat(),
            sin(a).toFloat(),
            (cos(a) * cos(z)).toFloat(),
        )
    }

    private fun link(vs: String, fs: String): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        if (v == 0 || f == 0) return 0
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v); GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)
        val ok = IntArray(1)
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            android.util.Log.e("SkyGL", "فشل ربط المُظلِّل: " + GLES20.glGetProgramInfoLog(p))
            GLES20.glDeleteProgram(p)
            return 0
        }
        return p
    }

    private fun compile(type: Int, src: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src); GLES20.glCompileShader(s)
        val ok = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) {
            android.util.Log.e("SkyGL", "فشل ترجمة المُظلِّل: " + GLES20.glGetShaderInfoLog(s))
            GLES20.glDeleteShader(s)
            return 0
        }
        return s
    }
}

/* ══════════════════════════════════════════════════════════════
   الميل — أرخصُ ما يُبهر في المشهد

   إزاحةُ الكاميرا بمقدارٍ يسير حين يميل الجهاز تجعل السماءَ تُقرأ
   عمقاً لا صورةً ملصقة. ومقياسُ التسارع في كلّ هاتف، والحسابُ سطران.

   ويُنعَّم بمرشّحٍ أُسّيّ: القراءةُ الخام ترتجف، والسماءُ التي ترتجف
   أسوأُ من سماءٍ ساكنة.
══════════════════════════════════════════════════════════════ */
private class TiltReader(context: Context, private val onTilt: (Float, Float) -> Unit) :
    SensorEventListener {

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var x = 0f
    private var y = 0f

    fun start() { sensor?.let { sm?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } }
    fun stop() { sm?.unregisterListener(this) }

    override fun onSensorChanged(e: SensorEvent) {
        //  لا نازلَ يخرج من ردّ نداء مستشعر — كما في بوصلة القبلة.
        runCatching {
            val tx = (-e.values[0] / 9.81f).coerceIn(-1f, 1f) * 0.085f
            val ty = ((e.values[1] / 9.81f) - 0.72f).coerceIn(-1f, 1f) * 0.055f
            x += (tx - x) * 0.10f
            y += (ty - y) * 0.10f
            if (abs(tx - x) < 0.30f) onTilt(x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

/**
 * السماءُ الحيّة.
 *
 * @param sunAlt ارتفاعُ الشمس · [sunAz] سَمْتُها — من `sunPosition`
 * @param moonAlt ارتفاعُ القمر · [moonAz] سَمْتُه — من `moonPosition`
 * @param moon طورُه، منه تُشتقّ زاويةُ الإضاءة في المُظلِّل
 * @param reducedMotion يُسكِن التلألؤَ والزحفَ ويوقف النبضة
 */
@Composable
fun SkyGL(
    sunAlt: Double,
    sunAz: Double,
    moonAlt: Double,
    moonAz: Double,
    moon: MoonPhase,
    reducedMotion: Boolean,
    weather: SkyWeather = SkyWeather(),
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val renderer = remember { SkyRenderer() }
    val owner = LocalLifecycleOwner.current

    renderer.sunAltDeg = sunAlt
    renderer.sunAzDeg = sunAz
    renderer.moonAltDeg = moonAlt
    renderer.moonAzDeg = moonAz
    renderer.moonLit = moon.illumination.toFloat()
    renderer.moonWaxing = moon.waxing
    renderer.reduced = reducedMotion
    renderer.weather = weather

    val view = remember {
        object : GLSurfaceView(ctx) {
            init {
                setEGLContextClientVersion(2)
                setRenderer(renderer)
                renderMode = RENDERMODE_WHEN_DIRTY
                //  السماءُ خلف المحتوى لا فوقه.
                setZOrderOnTop(false)
            }
            override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
                super.onSizeChanged(w, h, ow, oh)
                //  نصفُ الدقّة: العملُ رُبعٌ، والفرقُ لا يُرى على تدرّج.
                if (w > 0 && h > 0) holder.setFixedSize(w / 2, h / 2)
            }
        }
    }

    DisposableEffect(owner, reducedMotion) {
        val tilt = TiltReader(ctx) { x, y -> renderer.tiltX = x; renderer.tiltY = y }
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val beat = object : Runnable {
            override fun run() {
                view.requestRender()
                if (!reducedMotion) handler.postDelayed(this, FPS_MS)
            }
        }
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> {
                    view.onResume(); tilt.start()
                    handler.removeCallbacks(beat); handler.post(beat)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    handler.removeCallbacks(beat); tilt.stop(); view.onPause()
                }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose {
            owner.lifecycle.removeObserver(obs)
            handler.removeCallbacks(beat); tilt.stop(); view.onPause()
        }
    }

    AndroidView(factory = { view }, modifier = modifier, update = { it.requestRender() })
}
