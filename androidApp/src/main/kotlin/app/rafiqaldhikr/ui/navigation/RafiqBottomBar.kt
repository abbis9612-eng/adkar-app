package app.rafiqaldhikr.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.IcoMisbaha
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

data class BottomNavItem(
    val labelRes: Int,
    // أيقونة مخصصة من مكتبة RafiqIcons الموحّدة (حجم، لون)
    val icon:     @Composable (Dp, Color) -> Unit,
    val route:    RafiqRoute
)

@Composable
fun RafiqBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(R.string.nav_home,    { s, c -> RafiqIcon(RIcon.Home, s, c) },     RafiqRoute.Home),
        BottomNavItem(R.string.nav_quran,   { s, c -> RafiqIcon(RIcon.Book, s, c) },     RafiqRoute.QuranList),
        BottomNavItem(R.string.nav_tasbeeh, { s, c -> IcoMisbaha(s, c) },             RafiqRoute.Tasbeeh),
        BottomNavItem(R.string.nav_dua,     { s, c -> RafiqIcon(RIcon.Heart, s, c) }, RafiqRoute.DuaCategories),
        BottomNavItem(R.string.nav_profile, { s, c -> RafiqIcon(RIcon.User, s, c) },  RafiqRoute.Profile),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val rc = LocalRafiqColors.current

    // Frosted glass surface
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = rc.bg.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        // Top gold accent line
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                rc.gold.copy(alpha = 0.3f),
                                rc.gold.copy(alpha = 0.5f),
                                rc.gold.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route.route
                    BottomBarItemEnhanced(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItemEnhanced(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rc = LocalRafiqColors.current

    // Animated background
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            rc.emerald.copy(alpha = 0.08f)
        else
            Color.Transparent,
        animationSpec = tween(300),
        label = "navBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected)
            rc.emerald
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
        animationSpec = tween(300),
        label = "navIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected)
            rc.emerald
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
        animationSpec = tween(300),
        label = "navLabel"
    )

    // Micro-animation: bounce on select
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navScale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with subtle border
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .then(
                    if (isSelected) Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                rc.emerald.copy(alpha = 0.05f),
                                rc.emerald.copy(alpha = 0.12f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            item.icon(22.dp, iconColor)
        }

        Spacer(Modifier.height(2.dp))

        Text(
            stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = labelColor
        )

        // Gold underline dot for active
        if (isSelected) {
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                rc.gold,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
