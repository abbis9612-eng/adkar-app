package app.rafiqaldhikr.ui.components

import android.content.Context
import androidx.annotation.StringRes
import app.rafiqaldhikr.R

/*
 * المناسبةُ القادمة
 * ════════════════
 *
 * «بداية رمضان — بعد ١٢٣ يوماً».
 *
 * سطرٌ واحدٌ يُحوّل الوقتَ من شيءٍ يمرّ إلى شيءٍ **يُنتظَر**. ومن عرف
 * أنّ بينه وبين رمضان أربعةَ أشهرٍ استعدّ، ومن لم يعرف فوجئ به.
 *
 * ═══ حسابٌ لا نصّ ═══
 *
 * هذا الملفُّ **لا يحمل نصّاً دينيّاً**: لا حديثَ ولا فضلَ ولا حكماً.
 * أسماءُ المناسبات وتواريخُها فقط — وهي تقويمٌ لا روايةٌ تُسنَد.
 *
 * وقد أُريد أن يُوضع تحت كلّ مناسبةٍ فضلُها بتخريجه (صيامُ عرفة،
 * عاشوراء، الأيّامُ البيض)، فتعذّر التحقّقُ من ألفاظها من مصادرها
 * الأصليّة حين كُتب هذا. **ولا يُكتب نصٌّ دينيٌّ من الذاكرة** — ذاك نقضُ
 * معيار القبول المثبَت في `KHITTA.md`. فتُضاف الفضائلُ في المرحلة
 * الثانية مع عمل التخريج، ويبقى التقويمُ نافعاً وحدَه حتى حينه.
 *
 * ═══ أمُّ القرى لا التقدير ═══
 *
 * التواريخُ من `android.icu.util.IslamicCalendar` بنوع الحساب
 * `ISLAMIC_UMALQURA` — وهو تقويمُ أمّ القرى الذي تعمل به السعوديّة
 * ويوافق ما يراه أكثرُ الناس. والتقديرُ الحسابيُّ (٢٩٫٥٣ يوماً للشهر)
 * ينحرف يوماً أو يومين، وفرقُ يومٍ في عرفة ليس تفصيلاً.
 */

/** مناسبةٌ في التقويم الهجريّ — شهرُها ويومُها. */
data class HijriDate(val month: Int, val day: Int)

/** مناسبةٌ محسوبة: اسمُها، وكم بقي إليها. */
data class Munasaba(
    @StringRes val name: Int,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val gregorianMillis: Long,
    val daysAway: Int,
)

/*  أشهرُ `IslamicCalendar` تبدأ من صفر: محرّم ٠ … ذو الحجّة ١١. */
private const val MUHARRAM = 0
private const val RAMADAN = 8
private const val SHAWWAL = 9
private const val DHU_HIJJAH = 11

/**
 * المناسباتُ السنويّةُ المعتبَرة — **سنويّةٌ فقط**.
 *
 * والأيّامُ البيضُ (١٣–١٥ من كلّ شهر) ليست هنا عن قصد: هي شهريّةٌ، فلو
 * دخلت لما عرض العدّادُ غيرَها — «الأيّام البيض بعد ٣ أيّام» في كلّ
 * أسبوعين، ولا يرى صاحبُه رمضانَ قادماً أبداً. والعدّادُ يخدم البعيدَ
 * الذي يُنسى، لا القريبَ الذي يتكرّر.
 */
private val YEARLY: List<Pair<Int, HijriDate>> = listOf(
    R.string.munasaba_hijri_new_year to HijriDate(MUHARRAM, 1),
    R.string.munasaba_ashura to HijriDate(MUHARRAM, 10),
    R.string.munasaba_ramadan to HijriDate(RAMADAN, 1),
    R.string.munasaba_eid_fitr to HijriDate(SHAWWAL, 1),
    R.string.munasaba_ten_dhulhijjah to HijriDate(DHU_HIJJAH, 1),
    R.string.munasaba_arafah to HijriDate(DHU_HIJJAH, 9),
    R.string.munasaba_eid_adha to HijriDate(DHU_HIJJAH, 10),
)

/**
 * أقربُ مناسبةٍ لم تمضِ بعد — **دالّةٌ خالصةٌ تُختبر بلا جهاز**.
 *
 * @param candidates (اسمٌ، يومُ المناسبة منذ المبدأ)
 * @param todayEpochDay يومُ اليوم
 *
 * واليومُ نفسُه **يُحتسب مناسبةً حاضرة** لا ماضية: من فتح التطبيق صباحَ
 * عرفة يجب أن يُقال له «اليوم عرفة» لا أن يُقفز به إلى عرفة القادم.
 */
fun nearestOccasion(
    candidates: List<Pair<Int, Long>>,
    todayEpochDay: Long,
): Pair<Int, Long>? = candidates
    .filter { it.second >= todayEpochDay }
    .minByOrNull { it.second }

/**
 * يحسب المناسبةَ القادمة من تقويم أمّ القرى.
 *
 * @return null على أجهزةٍ لا `IslamicCalendar` فيها، أو إن تعذّر الحساب
 *         — ولا يُعرض سطرٌ خاطئٌ بدلَ لا شيء.
 */
fun nextMunasaba(ctx: Context, hijriOffset: Int = 0): Munasaba? {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) return null
    return runCatching {
        val now = System.currentTimeMillis() + hijriOffset * DAY_MS
        val todayEpochDay = localDay(now)

        val cal = android.icu.util.IslamicCalendar().apply {
            calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            timeInMillis = now
        }
        val year = cal.get(android.icu.util.IslamicCalendar.YEAR)

        /*  السنةُ الحاضرةُ والتي تليها.
         *
         *  ولولا الثانيةُ لما وُجدت مناسبةٌ في آخر ذي الحجّة: كلُّ مناسبات
         *  السنة مضت، فلا يُعرض شيءٌ أحوجَ ما يكون صاحبُه إلى رأس السنة. */
        val candidates = mutableListOf<Triple<Int, Long, Int>>()
        for (y in year..year + 1) {
            for ((name, d) in YEARLY) {
                val c = android.icu.util.IslamicCalendar().apply {
                    calculationType =
                        android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
                    clear()
                    set(y, d.month, d.day)
                }
                candidates += Triple(name, localDay(c.timeInMillis), y)
            }
        }

        val pick = nearestOccasion(candidates.map { it.first to it.second }, todayEpochDay)
            ?: return@runCatching null
        val chosen = candidates.first { it.first == pick.first && it.second == pick.second }
        val date = YEARLY.first { it.first == pick.first }.second

        Munasaba(
            name = pick.first,
            hijriDay = date.day,
            hijriMonth = date.month,
            hijriYear = chosen.third,
            //  لحظةُ المناسبة كما حسبها التقويمُ — لا تُبنى من رقم اليوم:
            //  رقمُ اليوم محليٌّ، وضربُه في طول اليوم يُرجع لحظةً بالتوقيت
            //  العالميّ فتُعرَض تاريخاً سابقاً شرقَ غرينتش.
            gregorianMillis = chosen.second * DAY_MS - localOffset(now),
            daysAway = (pick.second - todayEpochDay).toInt(),
        )
    }.getOrNull()
}

private const val DAY_MS = 86_400_000L

/**
 * رقمُ اليوم في **التقويم المحليّ** لا في التوقيت العالميّ.
 *
 * ═══ وهذا أخطرُ سطرٍ في هذا الملفّ ═══
 *
 * `IslamicCalendar` تُرجع **منتصفَ ليلٍ محليّاً** لتاريخٍ هجريّ. وقسمةُ
 * تلك اللحظةِ على طول اليوم تُرجع رقمَ اليوم **العالميّ**: فشرقَ غرينتش
 * منتصفُ الليل المحليُّ يقع في اليوم العالميّ **السابق** — بغدادُ مثلاً
 * تبدأ يومَها الساعةَ ٢١:٠٠ من أمسِ غرينتش.
 *
 * وكان «اليوم» يُحسب عالميّاً والمناسباتُ محليّاً، فتبدو كلُّ مناسبةٍ
 * **قد مضت بيومٍ**. فصباحَ عرفة تُحذَف عرفةُ من المرشَّحين **ويُقفَز إلى
 * عرفةِ العام القادم** — في اليوم الذي صاحبُه أحوجُ ما يكون إلى علمه.
 *
 * فالطرفان يُقاسان بالمسطرة نفسِها: التقويمُ المحليّ.
 */
internal fun localDay(millis: Long): Long =
    Math.floorDiv(millis + localOffset(millis), DAY_MS)

internal fun localOffset(millis: Long): Int =
    java.util.TimeZone.getDefault().getOffset(millis)
