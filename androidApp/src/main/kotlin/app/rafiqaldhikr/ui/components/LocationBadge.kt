package app.rafiqaldhikr.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.material3.Text
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import com.google.android.gms.location.LocationServices

/**
 * شارة دائمة تظهر حين تكون مواقيت الصلاة محسوبة على موقع احتياطي (بلا إذن الموقع).
 * تُعلم المستخدم أن المواقيت تقريبية، وبالنقر تعيد طلب إذن الموقع مباشرة.
 * تختفي تلقائياً بمجرد توفر موقع حقيقي.
 */
@Composable
fun LocationBadge(
    hasLocation: Boolean,
    onLocationFetched: (lat: Double, lng: Double) -> Unit
) {
    val rc = LocalRafiqColors.current
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
            (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
        if (granted) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) onLocationFetched(loc.latitude, loc.longitude)
            }
        }
    }

    fun requestLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) onLocationFetched(loc.latitude, loc.longitude)
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    AnimatedVisibility(
        visible = !hasLocation,
        enter   = expandVertically() + fadeIn(),
        exit    = shrinkVertically() + fadeOut()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(rc.card)
                .border(1.dp, rc.gold, RoundedCornerShape(12.dp))
                .clickable { requestLocation() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("📍", fontSize = 15.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "المواقيت تقريبية — اضغط لتحديد موقعك",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = rc.ink,
                modifier = Modifier.weight(1f)
            )
            Text("تحديد", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
        }
    }
}
