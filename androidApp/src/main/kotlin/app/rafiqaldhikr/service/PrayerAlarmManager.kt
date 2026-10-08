package app.rafiqaldhikr.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class PrayerAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val FAJR_ID    = 100
        const val DHUHR_ID   = 101
        const val ASR_ID     = 102
        const val MAGHRIB_ID = 103
        const val ISHA_ID    = 104

        // تذكيرات الأذكار — مربوطة بالمواقيت الفعلية (رفيق اليوم)
        const val ADHKAR_MORNING_ID = 200
        const val ADHKAR_EVENING_ID = 201
        const val ADHKAR_SLEEP_ID   = 202

        const val MORNING_DELAY_MS = 25 * 60_000L  // بعد الفجر
        const val EVENING_DELAY_MS = 30 * 60_000L  // بعد العصر
        const val SLEEP_DELAY_MS   = 90 * 60_000L  // بعد العشاء

        /*  تذكيراتُ الأذكار المثبَّتة — معرّفاتُها من ٣٠٠ فصاعداً.
         *
         *  ومعرّفُ التنبيه `PIN_ID_BASE + dhikrId`، فلكلّ ذكرٍ معرّفُه
         *  الثابت: يُلغى ويُعاد جدولتُه بلا أن يدهس تذكيرَ غيره. ولو
         *  استُعمل عدّادٌ متسلسلٌ لاختلطت التذكيراتُ عند حذف واحدٍ منها.  */
        const val PIN_ID_BASE = 300
    }

    /**
     * @param prayers هل يُجدوَل أذانُ الصلوات الخمس؟
     * @param morning و[evening] و[sleep] تذكيراتُ الأذكار.
     *
     * كانت الجدولةُ تشمل الثمانيةَ دائماً، وصفوفُ شاشة الإشعارات الثلاثة
     * تبدو مفاتيحَ ولا تُنقر. فمن أراد تذكيرَ الأذكار بلا أذانٍ لم يكن
     * له إلّا أن يُطفئ الإشعاراتِ كلَّها.
     */
    fun scheduleAllForToday(
        prayerTimes: Map<String, Long>,
        prayers: Boolean = true,
        morning: Boolean = true,
        evening: Boolean = true,
    ) {
        if (!prayers) {
            listOf(FAJR_ID, DHUHR_ID, ASR_ID, MAGHRIB_ID, ISHA_ID).forEach(::cancelOne)
        }
        if (!morning) cancelOne(ADHKAR_MORNING_ID)
        if (!evening) cancelOne(ADHKAR_EVENING_ID)

        if (prayers) prayerTimes.forEach { (name, timeMillis) ->
            val notifId = when (name) {
                "fajr"    -> FAJR_ID
                "dhuhr"   -> DHUHR_ID
                "asr"     -> ASR_ID
                "maghrib" -> MAGHRIB_ID
                "isha"    -> ISHA_ID
                else      -> return@forEach
            }
            schedulePrayer(name, timeMillis, notifId)
        }

        // أذكار الصباح بعد الفجر، والمساء بعد العصر، والنوم بعد العشاء
        if (morning) prayerTimes["fajr"]?.let { schedulePrayer("adhkar_morning", it + MORNING_DELAY_MS, ADHKAR_MORNING_ID) }
        if (evening) prayerTimes["asr"]?.let  { schedulePrayer("adhkar_evening", it + EVENING_DELAY_MS, ADHKAR_EVENING_ID) }
        /*  تذكيرُ النوم يُجدوَل دائماً ما دامت الإشعاراتُ مفعَّلة: هو
         *  الذي يحمل سلسلةَ إعادة الجدولة لليوم التالي (انظر
         *  `PrayerAlarmReceiver.rescheduleTomorrow`). فإطفاؤه يُوقف
         *  التنبيهاتِ كلَّها إلى الأبد.  */
        prayerTimes["isha"]?.let { schedulePrayer("adhkar_sleep",   it + SLEEP_DELAY_MS,   ADHKAR_SLEEP_ID) }
    }

    /**
     * يجدول تذكيراتِ الأذكار المثبَّتة **بمواقيتها لا بساعاتها**.
     *
     * «ذكّرني الساعةَ ٦:٣٠» تنفع في بلدٍ وفصلٍ واحد؛ ومن سافر أو جاء
     * الشتاء صار تذكيرُه قبل المغرب بساعتين أو بعده بساعة فيُعطّله.
     * و«بعد المغرب» صحيحةٌ في كلّ بلدٍ وفصل، لأنّ المغربَ يُحسب من
     * إحداثيّات صاحبه.
     *
     * @param pins (معرّفُ الذكر، ميقاتُه، إزاحتُه بالدقائق)
     */
    fun schedulePinned(prayerTimes: Map<String, Long>, pins: List<Triple<Long, String, Int>>) {
        pins.forEach { (dhikrId, meeqat, offsetM) ->
            val base = prayerTimes[meeqat] ?: return@forEach
            schedulePrayer(
                prayerName = "dhikr_pin:$dhikrId",
                triggerAtMillis = base + offsetM * 60_000L,
                notifId = (PIN_ID_BASE + dhikrId).toInt(),
            )
        }
    }

    /** يُلغي تذكيرَ ذكرٍ بعينه — ولا يمسّ غيرَه. */
    fun cancelPinned(dhikrId: Long) = cancelOne((PIN_ID_BASE + dhikrId).toInt())

    /**
     * هل يملك التطبيقُ إذنَ التنبيه الدقيق؟
     *
     * على أندرويد ١٢ فما فوق يُمنح `SCHEDULE_EXACT_ALARM` تلقائياً، لكنّه
     * **يُسحب** عند استهداف SDK 33 فأعلى — وهذا التطبيق يستهدف ٣٦. فصار
     * الإذن مرفوضاً افتراضياً على كل تثبيتٍ جديد على أندرويد ١٣ فأعلى.
     */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()

    private fun schedulePrayer(prayerName: String, triggerAtMillis: Long, notifId: Int) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("notif_id",    notifId)
        }
        val pending = PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /*  صمتٌ تامّ كان هو السلوك: `if (!canScheduleExactAlarms()) return`.
         *
         *  فعلى كل جهاز أندرويد ١٣ فأعلى — وهي أغلبُ الأجهزة — لم يكن
         *  يُجدوَل أذانٌ واحد، ولا رسالةَ خطأ ولا سطرَ في السجلّ. يفتح
         *  المستخدم الإعدادات فيجد مفتاحَ الإشعارات مضاءً، وينتظر أذاناً
         *  لا يأتي أبداً.
         *
         *  والآن: الدقيقُ إن أُذن، وإلّا `setAndAllowWhileIdle` — يوقظ
         *  الجهازَ من السبات كذلك، وهامشُه دقائق معدودة. وتنبيهٌ متأخّرٌ
         *  دقائقَ خيرٌ من لا تنبيه، والشاشةُ تقول للمستخدم أيّهما يعمل
         *  عنده وتعرض عليه الترقية.
         */
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } catch (e: SecurityException) {
            // سُحب الإذنُ بين الفحص والجدولة — نازلٌ نادرٌ لكنّه يُسقط التطبيق.
            runCatching {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        }
    }

    /** يُلغي تنبيهاً واحداً بمعرّفه. */
    private fun cancelOne(id: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { alarmManager.cancel(it) }
    }

    /**
     * يُلغي كلَّ ما جُدول — **والمثبَّتةُ منه كذلك**.
     *
     * وكانت الثمانيةُ الثابتةُ وحدَها تُلغى، فتبقى تذكيراتُ الأذكار
     * المثبَّتة تعمل بعد أن يُطفئ صاحبُها التنبيهاتَ أو يمحو موقعه. ومن
     * أطفأ التنبيهاتِ فجاءه تنبيهٌ لم يُطفئ شيئاً.
     *
     * ومعرّفاتُ المثبَّتة تُمرَّر من القاعدة ولا تُخمَّن بنطاق: المعرّفُ
     * `PIN_ID_BASE + dhikrId` ورقمُ الذكر غيرُ محدود، فنطاقٌ ثابتٌ يترك
     * ثغرةً فوقه. ومن يُنادي هذه الدالّةَ يقرأ القاعدةَ أصلاً.
     *
     * @param pinIds أرقامُ الأذكار المثبَّتة كلِّها — لا المذكَّرةُ منها
     *               وحدَها: من أزال التذكيرَ ثمّ أطفأ التنبيهات لا يبقى له
     *               تنبيهٌ معلَّق
     */
    fun cancelAll(pinIds: List<Long> = emptyList()) {
        (
            listOf(
                FAJR_ID, DHUHR_ID, ASR_ID, MAGHRIB_ID, ISHA_ID,
                ADHKAR_MORNING_ID, ADHKAR_EVENING_ID, ADHKAR_SLEEP_ID
            ) + pinIds.map { (PIN_ID_BASE + it).toInt() }
        ).forEach { id ->
            val intent  = Intent(context, PrayerAlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }
}
