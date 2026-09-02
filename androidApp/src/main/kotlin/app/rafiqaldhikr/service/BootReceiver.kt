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
 * يُعيد جدولة الأذان بعد كل حدثٍ يُبطل التنبيهات المجدولة.
 *
 * وكان يقبل `BOOT_COMPLETED` وحدَه ويرفض ما عداه — فالتنبيهات تموت عند كل
 * تحديثٍ للتطبيق (أندرويد يُلغي تنبيهات الحزمة المستبدَلة)، وتبقى على
 * توقيت البلد المغادَر عند السفر (الجدولة بتوقيتٍ مطلق).
 */
class BootReceiver : BroadcastReceiver() {

    private companion object {
        val HANDLED = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                GlobalContext.get().get<PrayerRescheduler>().reschedule()
            } catch (e: Exception) {
                // الصمتُ التامّ كان يخفي عطباً لا يُكتشف إلّا بغياب الأذان:
                // لا تقارير أعطال في التطبيق، فالسجلّ هو الأثر الوحيد.
                Log.e("RafiqBoot", "فشلت إعادةُ جدولة الأذان بعد ${intent.action}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
