package app.rafiqaldhikr.ui.screens.deeplink

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

@Composable
fun DeepLinkLandingScreen(
    target: String,
    navController: NavHostController
) {
    val rc = LocalRafiqColors.current

    LaunchedEffect(target) {
        val route = when (target) {
            "quran"     -> RafiqRoute.QuranList.route
            "adhkar"    -> RafiqRoute.AdhkarCategories.route
            "tasbeeh"   -> RafiqRoute.Tasbeeh.route
            "prayer"    -> RafiqRoute.PrayerTimes.route
            "qibla"     -> RafiqRoute.Qibla.route
            "dua"       -> RafiqRoute.DuaCategories.route
            "profile"   -> RafiqRoute.Profile.route
            "settings"  -> RafiqRoute.Settings.route
            "breathing" -> RafiqRoute.Breathing.route
            "garden"    -> RafiqRoute.Garden.route
            "ramadan"   -> RafiqRoute.RamadanHome.route
            else        -> null
        }
        if (route != null) {
            navController.navigate(route) {
                popUpTo(RafiqRoute.Home.route)
                launchSingleTop = true
            }
        }
    }

    // Fallback UI if target is unknown
    Box(
        modifier         = Modifier.fillMaxSize().background(rc.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(rc.card)
                .border(1.dp, rc.gold.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌙", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "رفيق الذكر",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = rc.emerald
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "جاري التوجيه...",
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                color     = rc.inkMed
            )
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = rc.emerald)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { navController.navigate(RafiqRoute.Home.route) }) {
                Text("الرئيسية", color = rc.emerald)
            }
        }
    }
}
