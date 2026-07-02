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
        val channelId  = "prayer_channel"

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
            .setContentTitle("حان وقت $localizedName")
            .setContentText("حان الآن وقت صلاة $localizedName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        }

        // بعد إشعار العشاء: جدولة مواقيت الغد حتى تستمر الإشعارات بلا فتح التطبيق
        if (prayerName == "isha") {
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
    }
}
