package app.rafiqaldhikr.ui.sky

/* ══════════════════════════════════════════════════════════════
   مُظلِّلُ السماء — كلُّ بكسلٍ يُحسب، ولا صورةَ في المشروع

   هذا شعاعٌ يُطلَق من العين لكل بكسل، فيصطدم بالغلاف الجوّيّ وبقرص
   القمر وبالغيم. أي أنّ ما تراه ليس رسماً لسماءٍ بل **سماءً تُحسب**:

     • **التشتّتُ الجوّيّ حقيقيّ.** لونُ الغروب ليس متدرّجاً اخترتُه —
       هو ناتجُ امتصاصِ الطولِ الموجيّ القصير في مسارٍ أطول خلال الهواء.
       ولذلك يحمرّ الأفقُ من نفسه حين تنخفض الشمس، ويصير المطلعُ أزرقَ
       عميقاً. القِيَمُ βR وβM هي معاملاتُ رايلي ومي المعروفة.

     • **القمرُ كرةٌ لا قرص.** الشعاعُ يُقطع مع كرةٍ فيُحسب سطحُها
       ويُضاء بقانون لامبرت من اتّجاه الشمس — فالهلالُ **نتيجةُ هندسةٍ
       لا شكلٌ مرسوم**، ويميل بميل خطِّ عرضك من نفسه. وعلى جانبه المظلم
       نورُ الأرض، وفي وجهه بحارٌ من ضوضاء.

     • **النجومُ بأقدارها.** توزيعٌ أسّيٌّ: القليلُ ساطعٌ جداً والكثيرُ
       يكاد يُرى — وهذا المدى، لا العدد، هو ما يجعل السماء تُقرأ سماء.
       وألوانُها من حرارتها، ودربُ التبّانة شريطٌ من ضوضاء ليفية.

     • **الغيمُ حجميّ.** ضوضاءُ fbm تتحرّك، ويُضاء من جهة الشمس بتقريبِ
       تشتّتٍ أماميّ — فيحمرّ عند الغروب كما يحمرّ الغيمُ الحقيقيّ.

   ويعمل على `OpenGL ES 2.0` — أي على كلّ جهازٍ يدعمه التطبيق (أندرويد ٦
   فأعلى)، بلا مكتبةٍ واحدةٍ ولا ميغابايت.
══════════════════════════════════════════════════════════════ */

internal const val VERTEX_SRC = """
attribute vec2 aPos;
varying vec2 vUv;
void main() {
    vUv = aPos * 0.5 + 0.5;
    gl_Position = vec4(aPos, 0.0, 1.0);
}
"""

internal const val FRAGMENT_SRC = """
precision highp float;

varying vec2 vUv;

uniform vec2  uRes;
uniform float uTime;
uniform vec3  uSun;        // اتّجاهُ الشمس (وحدويّ، ص لأعلى)
uniform vec3  uMoon;       // اتّجاهُ القمر
uniform float uMoonLit;    // نسبةُ القرص المضاء ٠..١
uniform float uMoonSign;   // ‎+1 متزايدٌ · ‎-1 متناقص
uniform float uNight;      // ٠ نهارٌ تامّ · ١ ليلٌ تامّ
uniform vec2  uTilt;       // ميلُ الجهاز — يُزيح المشهد فيُقرأ عمقاً
uniform float uReduced;    // ١ إذا طُلب خفضُ الحركة

/* ── طقسُ مكانك — كلُّها شدّاتٌ من صفرٍ إلى واحد ───────────── */
uniform float uCloud;      // غطاءُ الغيم المقيس
uniform float uRain;
uniform float uSnow;
uniform float uFog;
uniform float uFlash;      // ومضةُ برقٍ آنيّة

const float PI = 3.14159265;

/* ── ضوضاء ─────────────────────────────────────────────── */
float hash1(vec3 p){
    p = fract(p * 0.3183099 + vec3(0.71, 0.113, 0.419));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}
float vnoise(vec3 x){
    vec3 i = floor(x), f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash1(i+vec3(0,0,0)), hash1(i+vec3(1,0,0)), f.x),
                   mix(hash1(i+vec3(0,1,0)), hash1(i+vec3(1,1,0)), f.x), f.y),
               mix(mix(hash1(i+vec3(0,0,1)), hash1(i+vec3(1,0,1)), f.x),
                   mix(hash1(i+vec3(0,1,1)), hash1(i+vec3(1,1,1)), f.x), f.y), f.z);
}
float fbm(vec3 p){
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) { v += a * vnoise(p); p *= 2.02; a *= 0.5; }
    return v;
}

/* ── التشتّتُ الجوّيّ ─────────────────────────────────────
   معاملاتُ رايلي تتناسب عكساً مع الطول الموجيّ للأسّ الرابع — ولهذا
   السماءُ زرقاء نهاراً والشمسُ حمراء عند الأفق: الأزرقُ يتشتّت أوّلاً
   فيبقى الأحمرُ حين يطول المسار.                                      */
const vec3 BETA_R = vec3(5.5e-6, 13.0e-6, 22.4e-6) * 1.0e5;
const vec3 BETA_M = vec3(21e-6) * 1.0e5;

float phaseR(float c){ return 3.0 / (16.0 * PI) * (1.0 + c * c); }
float phaseM(float c, float g){
    float g2 = g * g;
    return 3.0 / (8.0 * PI) * ((1.0 - g2) * (1.0 + c * c))
         / ((2.0 + g2) * pow(max(1.0 + g2 - 2.0 * g * c, 1e-4), 1.5));
}
/* كتلةُ الهواء: تطول كلّما اقترب الاتّجاه من الأفق. */
float airMass(float y){ return 1.0 / max(y + 0.05, 0.02); }

vec3 atmosphere(vec3 rd, vec3 sun){
    float c   = dot(rd, sun);
    float mV  = airMass(max(rd.y, -0.02));
    float mS  = airMass(max(sun.y, -0.06));

    vec3 extV = exp(-(BETA_R + BETA_M) * mV * 0.09);
    vec3 extS = exp(-(BETA_R * 1.35 + BETA_M) * mS * 0.09);

    vec3 inR = BETA_R * phaseR(c);
    vec3 inM = BETA_M * phaseM(c, 0.78) * 1.7;

    vec3 col = (inR + inM) * extS * (1.0 - extV) / (BETA_R + BETA_M);
    col *= 22.0 * smoothstep(-0.32, 0.12, sun.y);

    /* زرقةُ الليل: السماءُ لا تصير سوداءَ تماماً — يبقى ضوءٌ خافت. */
    vec3 nightSky = mix(vec3(0.016, 0.028, 0.062), vec3(0.006, 0.010, 0.028),
                        smoothstep(0.0, 0.55, rd.y));
    col = max(col, nightSky * (0.35 + 0.65 * uNight));

    /* قرصُ الشمس وهالتُها — تُرى فوق الأفق وحدَها. */
    if (sun.y > -0.02) {
        col += vec3(1.0, 0.86, 0.62) * pow(max(c, 0.0), 900.0) * 9.0 * smoothstep(-0.02, 0.05, sun.y);
    }
    return col;
}

/* ── النجوم ─────────────────────────────────────────────
   كلُّ خليةٍ في الشبكة تحمل نجماً واحداً على الأكثر، وقدرُه أسّيٌّ
   فيصير القليلُ ساطعاً والكثيرُ خافتاً — وهو مدى السماء الحقيقيّ.   */
vec3 starTint(float u){
    if (u < 0.10) return vec3(0.67, 0.78, 1.00);
    if (u < 0.32) return vec3(0.85, 0.92, 1.00);
    if (u < 0.66) return vec3(1.00, 0.99, 0.96);
    if (u < 0.88) return vec3(1.00, 0.94, 0.80);
    if (u < 0.97) return vec3(1.00, 0.82, 0.62);
    return vec3(1.00, 0.70, 0.55);
}
vec3 stars(vec3 rd){
    if (uNight < 0.02) return vec3(0.0);
    vec3 acc = vec3(0.0);

    /* طبقتان بكثافتَين — الأولى للسواطع والثانية لغبار الخافتة. */
    for (int L = 0; L < 2; L++) {
        float scale = (L == 0) ? 78.0 : 168.0;
        vec3 p = rd * scale;
        vec3 i = floor(p), f = fract(p);
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            vec3 g = vec3(float(x), float(y), float(z));
            vec3 id = i + g;
            float h = hash1(id);
            if (h < (L == 0 ? 0.955 : 0.90)) continue;

            vec3 off = vec3(hash1(id + 11.3), hash1(id + 27.7), hash1(id + 43.1));
            float d = length(f - g - off);

            /* القدر: أسٌّ يجعل الساطعَ نادراً — والسطوعُ ١٠^(‎-0.4·m) */
            float m = 1.0 + pow(hash1(id + 5.5), 0.40) * 5.0;
            float b = pow(10.0, -0.4 * (m - 1.0));

            float size = (L == 0 ? 0.055 : 0.030) * (0.35 + sqrt(b));
            float core = smoothstep(size, 0.0, d);
            core = pow(core, 2.2);

            /* التلألؤ للسواطع وحدَها، ويسكن عند خفض الحركة. */
            float tw = 1.0;
            if (m < 2.6) {
                tw = 0.80 + 0.20 * sin(uTime * 2.3 + h * 90.0);
                tw = mix(1.0, tw, 1.0 - uReduced);
            }
            acc += starTint(hash1(id + 71.9)) * core * (0.35 + b) * tw;
        }
    }

    /* دربُ التبّانة: شريطٌ ليفيٌّ حول دائرةٍ عظمى مائلة. */
    vec3 axis = normalize(vec3(0.34, 0.62, -0.71));
    float band = 1.0 - abs(dot(rd, axis));
    band = smoothstep(0.86, 1.0, band);
    float milk = fbm(rd * 9.0) * fbm(rd * 21.0 + 4.0);
    acc += vec3(0.62, 0.66, 0.86) * band * milk * 0.55;

    return acc * uNight;
}

/* ── القمر: كرةٌ يقطعها الشعاع ───────────────────────────
   الهلالُ هنا ليس شكلاً يُرسم — هو الجزءُ من الكرة الذي يبلغه ضوءُ
   الشمس. ولذلك يميل ميلَه الصحيح في كلّ خطّ عرضٍ بلا حسابٍ إضافيّ.  */
vec4 moonLayer(vec3 rd, vec3 mdir, vec3 sun){
    float align = dot(rd, mdir);
    if (align < 0.9975) {
        /* الهالة: تُرى أبعدَ من القرص بكثير. */
        float halo = pow(max(align, 0.0), 2600.0) * 0.42
                   + pow(max(align, 0.0), 260.0) * 0.05;
        return vec4(vec3(0.72, 0.79, 1.0) * halo * uNight, 0.0);
    }

    /* الشعاعُ يقطع كرةً مركزُها mdir*D ونصفُ قطرها R. */
    float D = 60.0, R = 1.55;
    vec3 oc = -mdir * D;
    float b = dot(rd, oc);
    float c = dot(oc, oc) - R * R;
    float disc = b * b - c;
    if (disc < 0.0) return vec4(0.0);

    float t = -b - sqrt(disc);
    vec3 hit = rd * t;
    vec3 n = normalize(hit - mdir * D);

    /* اتّجاهُ الإضاءة يُشتقّ من الطور: صفرٌ محاقاً وواحدٌ بدراً.
       والإشارةُ تقلب القرنَ بين متزايدٍ ومتناقص.                     */
    float ang = acos(clamp(1.0 - 2.0 * uMoonLit, -1.0, 1.0));
    vec3 side = normalize(cross(mdir, vec3(0.0, 1.0, 0.0)));
    vec3 lightDir = normalize(-mdir * cos(ang) + side * sin(ang) * uMoonSign);

    float lam = max(dot(n, lightDir), 0.0);
    lam = pow(lam, 0.62);                       /* سطحٌ خشنٌ لا كرةٌ ملساء */

    /* بحارٌ ومرتفعات */
    float mare = fbm(n * 3.4 + 9.0);
    vec3 surf = mix(vec3(0.74, 0.76, 0.82), vec3(0.97, 0.97, 0.94),
                    smoothstep(0.42, 0.68, mare));

    /* نورُ الأرض على الجانب المظلم — يُرى في الأهلّة الرقيقة. */
    float earth = (1.0 - lam) * (0.055 + 0.10 * (1.0 - uMoonLit));

    vec3 col = surf * (lam * 1.25 + earth);
    return vec4(col * uNight, 1.0);
}

/* ── الغيم ──────────────────────────────────────────────── */
vec3 clouds(vec3 rd, vec3 sun, vec3 sky){
    if (rd.y < 0.015) return sky;

    /* إسقاطُ الشعاع على مستوًى أفقيٍّ فوق الرأس — سحابةٌ مسطّحة. */
    vec2 uv = rd.xz / rd.y;
    float drift = uReduced > 0.5 ? 0.0 : uTime * 0.006;
    vec3 p = vec3(uv * 0.42 + vec2(drift, drift * 0.4), uTime * 0.010);

    float d = fbm(p);
    /*  عتبةُ الغيم تنزل كلّما ارتفع الغطاءُ المقيس: صفرٌ يعني سماءً
        صافيةً لا سحابةَ فيها، وواحدٌ يعني غيماً مطبِقاً لا فُرجةَ فيه. */
    float lo = mix(0.62, 0.20, uCloud);
    float hi = mix(0.94, 0.44, uCloud);
    float cover = smoothstep(lo, hi, d) * smoothstep(0.02, 0.22, rd.y);
    if (cover < 0.004) return sky;

    /* إضاءةٌ بتقريبِ تشتّتٍ أماميّ: الحافّةُ المواجهةُ للشمس أنصع. */
    float dens = fbm(p + vec3(0.14, 0.0, 0.0));
    float lit  = clamp((d - dens) * 6.0 + 0.5, 0.0, 1.0);
    float fwd  = pow(max(dot(rd, sun), 0.0), 5.0);

    vec3 lightC = mix(vec3(0.40, 0.44, 0.58), vec3(1.0, 0.80, 0.55),
                      smoothstep(-0.22, 0.14, sun.y));
    vec3 darkC  = mix(vec3(0.10, 0.12, 0.20), vec3(0.44, 0.42, 0.48),
                      smoothstep(-0.22, 0.24, sun.y));
    vec3 cc = mix(darkC, lightC, lit) + lightC * fwd * 0.6;

    /* ليلاً الغيمُ يحجب النجوم ولا يضيء. */
    cc = mix(cc, cc * 0.30, uNight * 0.75);
    return mix(sky, cc, cover * (0.72 - 0.24 * uNight));
}

/* ── المطر ──────────────────────────────────────────────
   خيوطٌ لا نقاط: القطرةُ الساقطة تُرى خطّاً لأنّ العينَ تدمج حركتَها.
   وثلاثُ طبقاتٍ بسرعاتٍ مختلفة تُقرأ عمقاً — القريبةُ أسرعُ وأعرض.

   **ولونُها لونُ السماء لا الأبيض.** هذا هو الفرقُ بين مطرٍ في مشهدٍ
   ومطرٍ ملصوقٍ عليه: القطرةُ عدسةٌ تنقل ما خلفها، فتحمرّ في الغروب
   وتزرقّ في الظهيرة.                                                */
float rainSheet(vec2 uv, float t, float scale, float slant, float speed){
    uv.x += uv.y * slant;
    vec2 g = uv * scale;
    g.y += t * speed;
    vec2 i = floor(g), f = fract(g);
    float h = hash1(vec3(i, 3.0));
    if (h < 0.90) return 0.0;
    float x = abs(f.x - 0.5) * 2.0;
    return smoothstep(0.55, 0.0, x) * smoothstep(1.0, 0.15, f.y) * (0.5 + h);
}

/* ── الثلج ──────────────────────────────────────────────
   حبّاتٌ مستديرةٌ تتهادى: السقوطُ بطيءٌ والانحرافُ جانبيٌّ بجيبٍ
   يختلف طورُه لكلّ حبّة — فلا تنزل الحبّاتُ في خطوطٍ متوازية.        */
float snowLayer(vec2 uv, float t, float scale, float speed){
    vec2 g = uv * scale;
    g.y += t * speed;
    vec2 i = floor(g);
    float h = hash1(vec3(i, 7.0));
    if (h < 0.93) return 0.0;
    g.x += sin(t * (0.7 + h) + h * 30.0) * 0.34;
    vec2 f = fract(g) - 0.5;
    return smoothstep(0.26, 0.02, length(f)) * (0.45 + h * 0.55);
}

void main(){
    vec2 uv = (gl_FragCoord.xy - 0.5 * uRes) / uRes.y;

    /* اتّجاهُ الشعاع: كاميرا تنظر إلى الأفق، والميلُ يُزيحها قليلاً.
       والإزاحةُ هي التي تُقرأ عمقاً حين يميل الجهاز.                 */
    vec3 rd = normalize(vec3(uv.x + uTilt.x, uv.y * 0.92 + 0.30 + uTilt.y, 1.0));

    vec3 col = atmosphere(rd, uSun);
    col += stars(rd);

    vec4 m = moonLayer(rd, uMoon, uSun);
    col = mix(col + m.rgb, m.rgb, m.a);

    col = clouds(rd, uSun, col);

    /* ── البرق: يضيء الغيمَ كلَّه لا نقطةً منه ───────────── */
    if (uFlash > 0.001) {
        col += vec3(0.72, 0.76, 0.92) * uFlash * (0.35 + 0.65 * smoothstep(0.0, 0.4, rd.y));
    }

    /* ── الضباب: حجابٌ يثخن نحو الأفق ─────────────────────
       ويأخذ لونَ الضوء الحاضر: رماديٌّ ليلاً، ذهبيٌّ عند الغروب —
       فضبابُ الفجر ليس ضبابَ الظهيرة.                              */
    if (uFog > 0.003) {
        vec3 veil = mix(vec3(0.16, 0.18, 0.24), vec3(0.86, 0.80, 0.72),
                        smoothstep(-0.16, 0.22, uSun.y));
        float thick = uFog * smoothstep(0.55, -0.05, rd.y);
        col = mix(col, veil, clamp(thick, 0.0, 0.92));
    }

    /* ── الهطول ─────────────────────────────────────────── */
    float fall = uReduced > 0.5 ? 0.0 : uTime;
    if (uRain > 0.004) {
        /* الريحُ تُميل الخيوط، والشدّةُ تزيد عددَها وعتامتها. */
        float sl = 0.16;
        float r = rainSheet(uv, fall, 26.0, sl, 5.2) * 0.9
                + rainSheet(uv + 3.1, fall, 42.0, sl, 7.4) * 0.6
                + rainSheet(uv + 7.7, fall, 66.0, sl, 9.8) * 0.4;
        /* لونُ القطرة من السماء نفسِها، مرفوعاً قليلاً لا مبيَّضاً. */
        vec3 drop = col * 1.45 + vec3(0.05, 0.06, 0.08);
        col = mix(col, drop, clamp(r * uRain * 0.85, 0.0, 0.85));
    }
    if (uSnow > 0.004) {
        float s = snowLayer(uv, fall, 17.0, 0.85) * 0.9
                + snowLayer(uv + 5.3, fall, 27.0, 1.25) * 0.65
                + snowLayer(uv + 9.1, fall, 40.0, 1.70) * 0.45;
        vec3 flake = mix(vec3(0.93, 0.95, 1.0), col * 1.6, 0.25);
        col = mix(col, flake, clamp(s * uSnow, 0.0, 0.92));
    }

    /* الأفق: الأرضُ تحت الخطّ، وضبابٌ فوقه. */
    float horizon = smoothstep(0.020, -0.028, rd.y);
    vec3 haze = mix(vec3(0.05, 0.06, 0.10), vec3(0.42, 0.28, 0.20),
                    smoothstep(-0.20, 0.10, uSun.y));
    col = mix(col, haze * 0.55, smoothstep(0.10, -0.01, rd.y) * 0.55);
    col = mix(col, vec3(0.020, 0.026, 0.045), horizon);

    /* مُنحنى نغميّ ثمّ تشويشٌ يكسر التشريط — تدرّجٌ بثمانيةِ بتّاتٍ
       على مساحةٍ كهذه يُظهر حلقاتٍ على أغلب الشاشات.                */
    col = col / (col + 0.72);
    col = pow(col, vec3(1.0 / 2.2));
    col += (hash1(vec3(gl_FragCoord.xy, 1.0)) - 0.5) / 255.0;

    gl_FragColor = vec4(col, 1.0);
}
"""
