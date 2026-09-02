package app.rafiqaldhikr.ui.screens.settings

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.util.rememberPermissionState
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun NotificationSettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = koinViewModel()
) {
    val rc    = LocalRafiqColors.current
    val perms = rememberPermissionState()

    /*  حالُ الإذن تُقرأ عند كل عودةٍ إلى الشاشة، لا مرّةً عند التركيب:
     *  المستخدم يخرج إلى إعدادات النظام ليأذن ثمّ يعود — ولو بقيت القيمةُ
     *  الأولى لبقيت البطاقةُ تطلب إذناً قد مُنح.  */
    var exact by remember { mutableStateOf(perms.canScheduleExactAlarms()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) exact = perms.canScheduleExactAlarms()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // u2550u2550u2550 HEADER u2550u2550u2550
            RafiqTopBar(
                title  = stringResource(R.string.notif_title),
                onBack = {navController.popBackStack()},
            )

            // Content
            val enabled by vm.notificationsEnabled.collectAsStateWithLifecycle()

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    // Main Toggle
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.notif_enable), color = rc.ink, style = RafiqType.body)
                            Text(stringResource(R.string.notif_enable_desc), fontSize = 13.sp, color = rc.inkMed)
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                /*  إذنُ الإشعارات كان يُطلب في التعريف الأوّل
                                 *  وحدَه. فمن تخطّاه ثمّ أضاء هذا المفتاح ظنّ
                                 *  أنّ الأذان سيصله، والنظامُ يُسقط كلَّ إشعارٍ
                                 *  بلا صوتٍ ولا أثر.  */
                                if (it) perms.requestNotificationPermission()
                                vm.setNotifications(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = rc.card,
                                checkedTrackColor = rc.emerald,
                                uncheckedThumbColor = rc.inkLight,
                                uncheckedTrackColor = rc.divider
                            )
                        )
                    }
                    
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))

                    /*  قائمةُ ما يصل، لا مفاتيحُ تحكّم.
                     *
                     *  كانت هذه الصفوفُ الثلاثة تبدو مفاتيحَ معطّلة: عنوانٌ
                     *  ووصفٌ ولا شيء يُنقر. وهي في الحقيقة وصفٌ لما يُرسَل
                     *  حين يُضاء المفتاحُ الأعلى. فصارت تحت عنوانٍ يقول ذلك.
                     *  والتحكّمُ المنفصل لكل نوعٍ يحتاج عمودَين في الجدول،
                     *  ولا يُغيَّر المخطّطُ بلا ترحيلٍ مرقَّم.  */
                    Text(
                        stringResource(R.string.notif_what_arrives),
                        color = rc.inkMed,
                        style = RafiqType.caption,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
                    )

                    listOf(
                        stringResource(R.string.cat_morning) to stringResource(R.string.notif_morning_desc),
                        stringResource(R.string.cat_evening) to stringResource(R.string.notif_evening_desc),
                        stringResource(R.string.nav_prayer) to stringResource(R.string.notif_prayer_desc)
                    ).forEach { (title, subtitle) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, color = if (enabled) rc.ink else rc.inkLight, style = RafiqType.body)
                                Text(subtitle, fontSize = 13.sp, color = rc.inkMed)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                /*  دقّةُ التنبيه — حالٌ يجب أن يراها صاحبُه.
                 *
                 *  التطبيق يستهدف SDK 36، وأندرويد يسحب إذنَ التنبيه الدقيق
                 *  عن كل مستهدِفٍ لـ33 فأعلى. فيبقى الأذانُ مجدولاً تقريبياً
                 *  ما لم يأذن المستخدم — وهو لا يعلم أنّ ثمّة إذناً أصلاً.  */
                if (enabled && !exact) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .rafiqCard()
                            .clickable {
                                perms.requestExactAlarmPermission()
                                exact = perms.canScheduleExactAlarms()
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.notif_exact_title),
                            color = rc.ink,
                            fontWeight = FontWeight.SemiBold,
                            style = RafiqType.body,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.notif_exact_body),
                            fontSize = 13.sp,
                            color = rc.inkMed,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.notif_exact_cta),
                            color = rc.emerald,
                            fontWeight = FontWeight.SemiBold,
                            style = RafiqType.bodyS,
                        )
                    }
                }
            }
        }
    }
}
