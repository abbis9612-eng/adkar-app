package app.rafiqaldhikr.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.City
import app.rafiq.domain.repository.PrefsRepository
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

/* ═══════════════════════════════════════════════════════════════════
   «حدّد موقعك» — الحالة التي كان الاحتياطيّ الصامت يخفيها

   قبل هذا كان التطبيق يعرض مواقيت السليمانية لمن لم يمنح الإذن، بثقة
   وبلا أي إشارة. والأغرب: LocationPermissionEffect كان معرَّفاً في
   المشروع ولا يُستدعى من أي شاشة — أي أن التطبيق لم يكن يطلب الموقع
   أصلاً، والاحتياطيّ هو ما جعل ذلك غير مرئي طوال الوقت.

   هذه الشاشة تحلّ محلّ الرقم الكاذب: تشرح، وتطلب، وتحفظ.
═══════════════════════════════════════════════════════════════════ */

class LocationRequestViewModel(
    private val context:   Context,
    private val prefsRepo: PrefsRepository,
) : ViewModel() {

    /**
     * اسم المدينة تحسينٌ للعرض فقط: إن تعذّر ترميزه العكسي (بلا شبكة) بقي
     * فارغاً، والمواقيت تُحسب من الإحداثيات وحدها — فالتطبيق يظلّ يعمل
     * دون اتصال، وهو شرط لا يُكسر.
     */
    fun save(lat: Double, lng: Double) {
        viewModelScope.launch {
            val city = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(context).getFromLocation(lat, lng, 1)
                        ?.firstOrNull()?.let { it.locality ?: it.adminArea }
                }.getOrNull().orEmpty()
            }
            prefsRepo.updateLocation(city, lat, lng)
        }
    }

    /** مدينة مختارة يدوياً: اسمها معروف، فلا ترميز عكسي ولا حاجة إلى شبكة. */
    fun saveCity(city: City) {
        viewModelScope.launch { prefsRepo.updateLocation(city.ar, city.lat, city.lng) }
    }
}

@Composable
fun NeedsLocation(
    message: String = "المواقيت تُحسب من موقعك، ولا يمكن حسابها بدونه.",
    modifier: Modifier = Modifier,
    vm: LocationRequestViewModel = koinViewModel(),
) {
    val rc      = LocalRafiqColors.current
    val context = LocalContext.current
    val client  = remember { LocationServices.getFusedLocationProviderClient(context) }
    var asking  by remember { mutableStateOf(false) }
    var denied  by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }

    @SuppressLint("MissingPermission")
    fun readLastLocation() {
        client.lastLocation
            .addOnSuccessListener { loc ->
                asking = false
                if (loc != null) vm.save(loc.latitude, loc.longitude) else denied = true
            }
            .addOnFailureListener { asking = false; denied = true }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) readLastLocation() else { asking = false; denied = true }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IcoPin(72.dp, rc.gold, off = true)
        Spacer(Modifier.height(20.dp))
        Text("حدّد موقعك", style = RafiqType.titleL, color = rc.ink)
        Spacer(Modifier.height(10.dp))
        Text(message, textAlign = TextAlign.Center, style = RafiqType.body, color = rc.inkMed)

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (asking) "جارٍ تحديد الموقع…" else "تفعيل الموقع",
            style = RafiqType.label,
            color = rc.onEmeraldFill,
            modifier = Modifier
                .clip(RafiqShape.chip)
                .background(rc.emeraldFill)
                .clickable(enabled = !asking) {
                    denied = false
                    asking = true
                    val fine = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fine || coarse) readLastLocation() else launcher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                }
                .padding(horizontal = 28.dp, vertical = 12.dp),
        )

        Spacer(Modifier.height(6.dp))

        // الباب الثاني: من يرفض الإذن، أو يكون على جهاز بلا GPS، يختار
        // مدينته بنفسه. بدون هذا كان حذف الاحتياطيّ يترك التطبيق بلا
        // مواقيت ولا قبلة ولا ورقة لمن لا يمنح الموقع.
        Text(
            "أو اختر مدينتك يدوياً",
            style = RafiqType.label,
            color = rc.emerald,
            modifier = Modifier
                .clip(RafiqShape.chip)
                .clickable { picking = true }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        )

        if (picking) {
            CityPickerSheet(
                onDismiss = { picking = false },
                onPick    = { city -> vm.saveCity(city); picking = false },
            )
        }

        if (denied) {
            Spacer(Modifier.height(14.dp))
            Text(
                "تعذّر الحصول على الموقع. تأكّد من تفعيل خدمة الموقع في جهازك، " +
                    "أو امنح الإذن من إعدادات النظام، أو اختر مدينتك يدوياً.",
                textAlign = TextAlign.Center,
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
        }
    }
}
