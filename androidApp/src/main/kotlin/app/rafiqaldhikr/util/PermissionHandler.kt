package app.rafiqaldhikr.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionState(): PermissionState {
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled in ViewModel */ }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled in ViewModel */ }
    return remember { PermissionState(context, notifLauncher, locationLauncher) }
}

class PermissionState(
    private val context:          Context,
    private val notifLauncher:    ActivityResultLauncher<String>,
    private val locationLauncher: ActivityResultLauncher<Array<String>>
) {
    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= 33)
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else true

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33)
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestLocationPermission() {
        locationLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= 31)
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        else true

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
        }
    }
}
