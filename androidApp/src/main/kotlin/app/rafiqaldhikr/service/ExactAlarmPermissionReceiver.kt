package app.rafiqaldhikr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * يُرقّي التنبيهات إلى الدقيقة لحظةَ منحِ إذنِها.
 *
 * التطبيق يجدول تنبيهاً تقريبياً حين يُمنع الدقيق، فلا يبقى المستخدم بلا
 * أذان. فإن ذهب إلى إعدادات النظام وأذِن، وجب أن تُرقّى التنبيهات
 * المجدولةُ فوراً — وبدون هذا المستقبِل تبقى تقريبيةً حتى يُفتح التطبيق
 * مرّةً أخرى، وقد لا يُفتح قبل الفجر.
 */
class ExactAlarmPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (e: Exception) {
                Log.e("RafiqAlarmPerm", "فشلت إعادةُ الجدولة بعد منح الإذن الدقيق", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
