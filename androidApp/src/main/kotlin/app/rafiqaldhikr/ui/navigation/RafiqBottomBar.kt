package app.rafiqaldhikr.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

data class BottomNavItem(
    val labelRes: Int,
    val icon:     ImageVector,
    val route:    RafiqRoute
)

@Composable
fun RafiqBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(R.string.nav_home,    Icons.Default.Home,                 RafiqRoute.Home),
        BottomNavItem(R.string.nav_quran,   Icons.Default.MenuBook,             RafiqRoute.QuranList),
        BottomNavItem(R.string.nav_tasbeeh, Icons.Default.RadioButtonUnchecked, RafiqRoute.Tasbeeh),
        BottomNavItem(R.string.nav_dua,     Icons.Default.FavoriteBorder,       RafiqRoute.DuaCategories),
        BottomNavItem(R.string.nav_profile, Icons.Default.Person,               RafiqRoute.Profile),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val rc = LocalRafiqColors.current

    // Frosted glass surface
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = rc.navGlass.copy(alpha = 0.97f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Column {
            // Top gold accent hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                rc.gold.copy(alpha = 0.35f),
                                rc.gold.copy(alpha = 0.55f),
                                rc.gold.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp)
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

    // Active pill: solid emerald behind the icon, white glyph
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) rc.navSelected else Color.Transparent,
        animationSpec = tween(280),
        label = "navPill"
    )
    // rc.bg reads as white on the deep-emerald pill in light mode and as
    // near-black on the bright-emerald pill in dark mode — high contrast in both.
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) rc.bg else rc.inkMed.copy(alpha = 0.75f),
        animationSpec = tween(280),
        label = "navIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) rc.navSelected else rc.inkMed.copy(alpha = 0.75f),
        animationSpec = tween(280),
        label = "navLabel"
    )

    // Micro-animation: bounce on select
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navScale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(pillColor)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = stringResource(item.labelRes),
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.height(3.dp))

        Text(
            stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = labelColor
        )
    }
}
