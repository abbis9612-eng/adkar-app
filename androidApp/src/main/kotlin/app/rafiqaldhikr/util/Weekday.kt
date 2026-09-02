package app.rafiqaldhikr.util

import androidx.compose.runtime.Composable
import app.rafiqaldhikr.R
import kotlinx.datetime.LocalDate

/* ═══════════════════════════════════════════════════════════════════
   الجمعة — بحسابٍ صرف بلا java.time

   كان الكود يقول `dayOfWeek == DayOfWeek.FRIDAY`. و kotlinx.datetime
   .DayOfWeek هو typealias لـ java.time.DayOfWeek على أندرويد: فئةٌ لا
   وجود لها قبل API 26، و minSdk هنا 23.

   أي أن كل جهاز على أندرويد ٦ أو ٧ كان ينهار عنده التطبيق عند فتح
   الشاشة الرئيسية — لأن ورقة اليوم تمرّ من هذا السطر. أمسك lint هذا في
   أول دقيقة من تشغيله، بعد أن كان يمرّ في كل بناء أخضر قبله.

   الحساب: ١٩٧٠-٠١-٠١ كان خميساً، فأول جمعة بعد الحقبة هي اليوم ١.
   إذن جمعة ⇔ باقي قسمة أيام الحقبة على ٧ يساوي ١.

   الباقي محسوبٌ يدوياً لا بـ Math.floorMod — تلك واجهة API 24، فتُبدِل
   خطأً بخطأ.
═══════════════════════════════════════════════════════════════════ */

fun LocalDate.isFriday(): Boolean = isFridayFromEpochDays(toEpochDays())

/** مفصولة عن LocalDate ليتمكّن الاختبار من فحص الحساب نفسه. */
internal fun isFridayFromEpochDays(epochDays: Int): Boolean =
    ((epochDays % 7) + 7) % 7 == 1

/* ═══════════════════════════════════════════════════════════════════
   يومُ الأسبوع بالحساب نفسِه

   نُقل إلى هنا من `AwraqViewModel` لأن شاشتين تحتاجانه: «أوراقي»
   وصفُّ الأسبوع في الملف الشخصي. وكان الثاني يُسمّي الأيّامَ بموضعها في
   القائمة لا بتاريخها — فأسبوعٌ ناقصُ الصفوف تُنسب أيّامُه إلى غير
   أسمائها.

   أوّلُ الحقبة (1970-01-01) خميس، فإزاحةُ أربعةٍ تجعل الأحدَ صفراً.
   والباقي محسوبٌ يدوياً لا بـ Math.floorMod — تلك API 24.
═══════════════════════════════════════════════════════════════════ */

fun weekdayIndex(d: LocalDate): Int = weekdayIndexFromEpochDays(d.toEpochDays())

/** مفصولة عن LocalDate ليتمكّن الاختبار من فحص الحساب نفسه. */
internal fun weekdayIndexFromEpochDays(epochDays: Int): Int =
    (((epochDays + 4) % 7) + 7) % 7

/*  الأحرفُ والأسماءُ في `arrays.xml`: العربيةُ حرفٌ واحد («ح» للأحد)
 *  والإنجليزيةُ حرفان («Su») — ولا يُشتقّ أحدُهما من الآخر.
 *  الأحد أوّلاً في الترتيبين، مطابقاً لـ[weekdayIndex]. */
@Composable
fun weekdayLetters(): List<String> =
    androidx.compose.ui.res.stringArrayResource(R.array.weekday_letters).toList()

@Composable
fun weekdayNames(): List<String> =
    androidx.compose.ui.res.stringArrayResource(R.array.weekday_names).toList()
