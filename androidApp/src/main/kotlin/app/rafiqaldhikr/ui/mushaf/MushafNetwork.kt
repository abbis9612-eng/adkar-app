package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/* ══════════════════════════════════════════════════════════════
   هل الاتّصالُ مجانيّ؟

   خطُّ الصفحة الواحدة نحو مليونَي بايت. وجلبُه على واي‑فاي لا يكلّف
   صاحبَه شيئاً، فلا معنى لأن يُستأذن. وجلبُه على بيانات الجوّال يكلّفه
   من رصيده، فلا يُفعل إلّا بإذنٍ صريح.

   وهذا هو الفرقُ الذي يُلغي السؤالَ في أغلب الحالات: أكثرُ الناس يفتحون
   المصحفَ في البيت.
══════════════════════════════════════════════════════════════ */

internal fun Context.isUnmetered(): Boolean = runCatching {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}.getOrDefault(false)
