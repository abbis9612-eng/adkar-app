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
import org.koin.core.context.GlobalContext

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val notifId    = intent.getIntExtra("notif_id", 0)

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
