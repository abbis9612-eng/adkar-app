package app.rafiqaldhikr.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.rafiqaldhikr.MainActivity
import app.rafiqaldhikr.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import app.rafiq.domain.repository.PrayerRepository
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext

private const val END_ACTION = "end_act:"

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val notifId    = intent.getIntExtra("notif_id", 0)

        /*  الذكرُ المثبَّت يُرسَل بوسم `dhikr_pin:<id>`، وهو لا يبدأ بـ
         *  `adhkar_` — فكان يسقط إلى فرع الصلاة فيُعرَض على القارئ
         *  «حان وقت dhikr_pin:5». وهو وسمٌ داخليٌّ لا يُعرض لأحد. */
        if (prayerName.startsWith(PrayerAlarmManager.END_PREFIX)) {
            showEndReminder(
                context, prayerName.removePrefix(PrayerAlarmManager.END_PREFIX), notifId,
                intent.getLongExtra("end_at", 0L),
            )
            return
        }
        if (prayerName.startsWith(END_ACTION)) {
            onEndAction(context, prayerName.removePrefix(END_ACTION), notifId, intent)
            return
        }

        if (prayerName.startsWith("dhikr_pin:")) {
            showPinnedDhikr(context, prayerName.removePrefix("dhikr_pin:"), notifId)
            return
        }

        if (prayerName.startsWith("adhkar_")) {
            showAdhkarReminder(context, prayerName, notifId)
            // تذكير النوم هو آخر تنبيه في اليوم — بعده نجدول مواقيت الغد
            if (prayerName == "adhkar_sleep") rescheduleTomorrow(context)
            return
        }

        val channelId = "prayer_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.prayer_times_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                )
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val localizedName = when (prayerName) {
            "fajr"    -> context.getString(R.string.fajr)
            "dhuhr"   -> context.getString(R.string.dhuhr)
            "asr"     -> context.getString(R.string.asr)
            "maghrib" -> context.getString(R.string.maghrib)
            "isha"    -> context.getString(R.string.isha)
            else      -> prayerName
        }

        val tapPending = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_prayer_title, localizedName))
            .setContentText(context.getString(R.string.notif_prayer_body, localizedName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        }

    }

    /* ═══════════════════════════════════════════════════════════════
       تذكيرُ خروج الوقت — «يبقى من وقت العصر عشرون دقيقة»

       لا يُعرض لمن أحاط صلاتَه في الورقة: يُقرأ `PrayerLog` أوّلاً. ومن
       لم يُسجّلها فله زرّان: «صلّيتُ» يُسجّلها كما تُسجَّل في الورقة،
       و«ذكّرني بعد ١٠د» ما لم يخرج الوقتُ قبلها.
    ═══════════════════════════════════════════════════════════════ */

    private fun todayKey(): String = kotlinx.datetime.Clock.System.now()
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()

    private fun prayerLabel(context: Context, prayer: String): String = when (prayer) {
        "fajr"    -> context.getString(R.string.fajr)
        "dhuhr"   -> context.getString(R.string.dhuhr)
        "asr"     -> context.getString(R.string.asr)
        "maghrib" -> context.getString(R.string.maghrib)
        else      -> prayer
    }

    private fun showEndReminder(context: Context, prayer: String, notifId: Int, endAt: Long) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val date = todayKey()
                val prayed = runCatching {
                    GlobalContext.get().get<PrayerRepository>().getRange(date, date).first()
                        .any { it.prayerName == prayer && it.prayed }
                }.getOrDefault(false)
                if (prayed) return@launch

                val channelId = "prayer_end_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                        NotificationChannel(
                            channelId,
                            context.getString(R.string.notif_channel_end),
                            NotificationManager.IMPORTANCE_HIGH,
                        ),
                    )
                }
                val name = prayerLabel(context, prayer)
                //  رمزُ الطلب سالب: الموجبُ كلُّه محجوز — والمثبَّتةُ ٣٠٠ + رقمُ
                //  الذكر بلا سقف، فرمزٌ موجبٌ هنا قد يطابق تنبيهَ ذكرٍ فيدهسه.
                fun action(kind: String, code: Int) = PendingIntent.getBroadcast(
                    context, -(notifId * 10 + code),
                    Intent(context, PrayerAlarmReceiver::class.java).apply {
                        putExtra("prayer_name", "$END_ACTION$kind:$prayer")
                        putExtra("notif_id", notifId)
                        putExtra("end_at", endAt)
                        putExtra("date", date)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val tapPending = PendingIntent.getActivity(
                    context, notifId,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val left = ((endAt - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(1)
                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.notif_end_title, name, left))
                    .setContentText(context.getString(R.string.notif_end_body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setContentIntent(tapPending)
                    .addAction(0, context.getString(R.string.notif_end_prayed, name), action("prayed", 1))
                if (endAt - System.currentTimeMillis() > PrayerAlarmManager.SNOOZE_MS) {
                    builder.addAction(0, context.getString(R.string.notif_end_snooze), action("snooze", 2))
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(context).notify(notifId, builder.build())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun onEndAction(context: Context, payload: String, notifId: Int, intent: Intent) {
        val (kind, prayer) = payload.split(":", limit = 2).let { it.getOrNull(0) to it.getOrNull(1) }
        if (prayer == null) return
        NotificationManagerCompat.from(context).cancel(notifId)
        val endAt = intent.getLongExtra("end_at", 0L)
        when (kind) {
            "snooze" -> runCatching {
                GlobalContext.get().get<PrayerAlarmManager>().snoozeEnd(prayer, endAt)
            }
            "prayed" -> {
                val date = intent.getStringExtra("date") ?: todayKey()
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        //  بنمطِ الورقة نفسِه: السجلُّ ثمّ عددُ اليوم
                        val prayers = GlobalContext.get().get<PrayerRepository>()
                        val progress = GlobalContext.get().get<ProgressRepository>()
                        prayers.logPrayer(date, prayer, true)
                        progress.ensureExists(date)
                        progress.updatePrayers(date, prayers.getPrayedCount(date, date))
                    } catch (_: Exception) {
                        // قاعدةٌ غيرُ مهيّأة — يُسجّلها من الورقة
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    // جدولة مواقيت الغد حتى تستمر الإشعارات بلا فتح التطبيق
    private fun rescheduleTomorrow(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (_: Exception) {
                // offline أو Koin غير مهيأ — سيُعاد عند فتح التطبيق أو الإقلاع
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * تذكيرُ ذكرٍ ثبّته القارئُ بنفسه.
     *
     * ونصُّ الذكر يُقرأ من القاعدة على خيطٍ خلفيٍّ بـ[goAsync]: مُستقبِلُ
     * البثّ يعمل على الخيط الرئيسيّ، وقراءةُ قاعدةٍ عليه تُعلّق النظام.
     *
     * وإن تعذّرت القراءةُ — قاعدةٌ مغلقةٌ أو ذكرٌ حُذف — عُرض عنوانٌ عامٌّ
     * ولم يُعرض **الوسمُ الداخليّ**: «حان وقت dhikr_pin:5» ليست رسالةً
     * تُقال لأحد، وهي ما كان يظهر قبل هذا.
     */
    private fun showPinnedDhikr(context: Context, dhikrId: String, notifId: Int) {
        val id = dhikrId.toLongOrNull() ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            var text: String? = null
            var category: String? = null
            try {
                val db = GlobalContext.get().get<app.rafiq.db.RafiqDatabase>()
                db.adhkarQueries.getById(id).executeAsOneOrNull()?.let {
                    text = it.text_ar
                    category = it.category
                }
            } catch (_: Exception) {
                // قاعدةٌ غيرُ مهيّأةٍ — يُعرَض العنوانُ العامُّ وحدَه
            }

            val channelId = "adhkar_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(
                        NotificationChannel(
                            channelId,
                            context.getString(R.string.notif_channel_adhkar),
                            NotificationManager.IMPORTANCE_DEFAULT,
                        ),
                    )
            }

            val target = category ?: "misc"
            val tapPending = PendingIntent.getActivity(
                context, notifId,
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://rafiqaldhikr.app/adhkar/$target"),
                    context, MainActivity::class.java,
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_pin_title))
                .setContentText(text ?: context.getString(R.string.notif_pin_body))
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    text ?: context.getString(R.string.notif_pin_body),
                ))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(tapPending)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notifId, notification)
            }
            pendingResult.finish()
        }
    }

    private fun showAdhkarReminder(context: Context, type: String, notifId: Int) {
        val channelId = "adhkar_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notif_channel_adhkar),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val (title, body, target) = when (type) {
            /*  نصُّ الإشعار من الموارد لا من الكود.
             *
             *  كانت العناوينُ والمتونُ الثلاثة مكتوبةً عربيةً هنا، فيصل
             *  الإشعارُ عربياً إلى من اختار الإنجليزية — وهو أوّلُ ما يراه
             *  من التطبيق في يومه، وقد لا يفتحَه أصلاً.  */
            "adhkar_morning" -> Triple(
                context.getString(R.string.notif_adhkar_morning_title),
                context.getString(R.string.notif_adhkar_morning_body),
                "morning",
            )
            "adhkar_evening" -> Triple(
                context.getString(R.string.notif_adhkar_evening_title),
                context.getString(R.string.notif_adhkar_evening_body),
                "evening",
            )
            else -> Triple(
                context.getString(R.string.notif_adhkar_sleep_title),
                context.getString(R.string.notif_adhkar_sleep_body),
                "sleep",
            )
        }

        val tapPending = PendingIntent.getActivity(
            context, notifId,
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://rafiqaldhikr.app/adhkar/$target"),
                context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        }
    }
}
