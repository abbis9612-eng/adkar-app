package app.rafiqaldhikr.util

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
