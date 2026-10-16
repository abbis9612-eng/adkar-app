package app.rafiqaldhikr.ui.screens.waraqa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.FirstHint
import app.rafiqaldhikr.ui.components.NeedsLocation
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqIconButton
import app.rafiqaldhikr.ui.components.RisalaCard
import app.rafiqaldhikr.ui.components.pickRisala
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus
import app.rafiqaldhikr.ui.screens.home.HomeViewModel
import app.rafiqaldhikr.ui.theme.LocalMeeqat
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ═══════════════════════════════════════════════════════════════════
   ورقة اليوم — الصفحة الرئيسية

 *  التوقيع: ورقةُ تقويمٍ تُحاط صلواتُها بقلم

   ورقةُ تقويمٍ معلَّقة: ثقبا التعليق، والأوراقُ الباقيةُ تحتها، واليومُ
   الهجريُّ رقماً كبيراً — ثمّ الصلواتُ الخمس بأوقاتها. ومن صلّى ضغط
   الوقتَ فدار حوله قلمٌ كما يُدار على ورقةٍ حقيقيّة.

   والتوقيعُ هو الأداةُ نفسُها لا زينةٌ فوقها: الدائرةُ تعني «صلّيتُ»،
   وتُكتب في `PrayerLog` — الجدولِ الذي تقرأ منه الإحصاءاتُ والتقرير.
   وخمسُ دوائرَ تختم الورقة: «اكتملت ورقتك».

   وتحتها بطاقاتُ المحطّات تُقلَّب — «ماذا أفعل الآن؟» — وبعد العشاء
   تنقلب الورقةُ إلى ظهرها: محاسبةُ اليوم وأسبوعُك.
═══════════════════════════════════════════════════════════════════ */

@Composable
fun WaraqaScreen(
    navController: NavHostController,
    dayVm:  DayCompanionViewModel = koinViewModel(),
    homeVm: HomeViewModel         = koinViewModel(),
    wisdomVm: WisdomViewModel     = koinViewModel(),
    leafVm: LeafViewModel         = koinViewModel(),
) {
    val day  by dayVm.uiState.collectAsStateWithLifecycle()
    val home by homeVm.uiState.collectAsStateWithLifecycle()
    val leaf by leafVm.ui.collectAsStateWithLifecycle()
    val wisdom by wisdomVm.wisdom.collectAsStateWithLifecycle()
    val duas by wisdomVm.duas.collectAsStateWithLifecycle()
    val meeqat = LocalMeeqat.current
    val ctx = LocalContext.current
    /*  حالةُ الفتح تُحفظ في تفضيلاتٍ لا في القاعدة: هي حالةُ عرضٍ ليومٍ
     *  واحد، لا بيانُ مستخدمٍ يُصدَّر ويُستعاد. ومفتاحُها رقمُ اليوم،
     *  فتنغلق الرسالةُ من نفسِها مع أوّل فجر. */
    var opened by remember(wisdomVm.today) {
        mutableStateOf(
            ctx.getSharedPreferences("risala", android.content.Context.MODE_PRIVATE)
                .getLong("opened_day", -1L) == wisdomVm.today,
        )
    }
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    /*  بعد العشاء تنقلب الورقةُ وحدَها إلى المحاسبة — ويقلبها صاحبُها
     *  متى شاء. والمفتاحُ المحطّةُ الحاضرة: إذا دخل وقتُ النوم انقلبت
     *  مرّةً، ثمّ يبقى ما اختاره. */
    val night = day.nowStation?.id == "sleep"
    var flipped by rememberSaveable(night) { mutableStateOf(night) }

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            // الحشوة قبل التمرير: بعده كانت تتمرّر مع المحتوى،
            // فيزحف الرأس تحت ساعة الجهاز عند السحب.
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        Header(
            onSettings = { navController.navigate(RafiqRoute.Settings.route) },
            onBell     = { navController.navigate(RafiqRoute.NotificationSettings.route) },
        )

        val prayers = leafPrayers(home, leaf.prayed, day.timed)
        val stationsDone = day.stations.count { st ->
            st.status == StationStatus.DONE || StationTasks.isStationDone(
                st.id, StationTasks.of(st.id, st.route), leaf.prayed, leaf.completed, leaf.morning, leaf.evening,
            )
        }

        DayLeaf(
            hijriDay = home.hijriDay,
            hijriMonthYear = home.hijriMonthYear,
            dateLine = gregorianLine(home.city),
            prayers = prayers,
            onPrayer = leafVm::setPrayed,
            flipped = flipped,
            onFlip = { flipped = !flipped },
            front = {
                //  السؤالُ بعد دخول الفجر — قبله لم يبدأ اليومُ الذي يُعزَم فيه.
                //  وبلا مدينةٍ لا يُدرى متى الفجر، فيُسأل.
                val fajr = home.prayers.firstOrNull { it.en == "Fajr" }?.timeMillis ?: 0L
                IntentionBlock(
                    intentions = leaf.intentions,
                    ask = fajr == 0L || fajr <= System.currentTimeMillis(),
                    friday = day.isFriday,
                    completed = leaf.completed,
                    onCommit = leafVm::setIntentions,
                )
                LeafWord(day, wisdom)
                Text(
                    stringResource(R.string.leaf_flip_back),
                    style = RafiqType.label, fontWeight = FontWeight.Bold, color = rc.gold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp)
                        .heightIn(min = 48.dp)
                        .clickable { flipped = true }
                        .wrapContentHeight(Alignment.CenterVertically),
                )
            },
            accountRows = listOf(
                AccountRow(
                    stringResource(R.string.leaf_row_prayers),
                    stringResource(R.string.st_of, leaf.prayed.size.localized(ar), 5.localized(ar)),
                    leaf.prayed.size == 5,
                ),
                AccountRow(
                    stringResource(R.string.leaf_row_stations),
                    stringResource(R.string.st_of, stationsDone.localized(ar), day.stations.size.localized(ar)),
                    day.stations.isNotEmpty() && stationsDone == day.stations.size,
                ),
                AccountRow(
                    stringResource(R.string.task_morning),
                    stringResource(if (leaf.morning) R.string.leaf_row_done else R.string.leaf_row_open),
                    leaf.morning,
                ),
                AccountRow(
                    stringResource(R.string.task_evening),
                    stringResource(if (leaf.evening) R.string.leaf_row_done else R.string.leaf_row_open),
                    leaf.evening,
                ),
            ),
            week = leaf.week,
            modifier = Modifier.padding(top = 12.dp),
        )

        /*  المحطّاتُ تظهر دائماً — بمدينةٍ وبغيرها: الترتيبُ صادقٌ في كلّ
         *  مدينة، والساعةُ وحدَها تنتظر. */
        StationPager(
            stations = day.stations,
            nowId = day.nowStation?.id,
            timed = day.timed,
            leaf = leaf,
            onTask = leafVm::setTask,
            onDone = { st, tasks -> leafVm.completeStation(st.id, tasks) },
            onUndo = { st, tasks -> leafVm.undoStation(st.id, tasks) },
            onOpen = { navController.navigate(it) },
        )

        if (!day.timed) {
            NeedsLocation(message = stringResource(R.string.waraqa_needs_location))
        }

        FirstHint(
            "waraqa", R.string.hint_waraqa,
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        RisalaCard(
            dua = pickRisala(duas, wisdomVm.today, meeqat.phase),
            opened = opened,
            onOpen = {
                opened = true
                ctx.getSharedPreferences("risala", android.content.Context.MODE_PRIVATE)
                    .edit().putLong("opened_day", wisdomVm.today).apply()
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

/* ── الرأس: أقلّ ما يمكن — التاريخُ صار في الورقة ────────────────── */

@Composable
private fun Header(onSettings: () -> Unit, onBell: () -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RafiqIconButton(onClick = onBell, label = stringResource(R.string.nav_alerts)) {
            RafiqIcon(RIcon.Bell, 18.dp, rc.emerald)
        }
        Text(stringResource(R.string.app_name), style = RafiqType.titleM, color = rc.emerald)
        RafiqIconButton(onClick = onSettings, label = stringResource(R.string.settings)) {
            RafiqIcon(RIcon.Settings, 18.dp, rc.emerald)
        }
    }
}

/* ── كلمةُ الورقة: فضلُ الكهف يومَ الجمعة، وكلمةُ اليوم في غيره ───── */

@Composable
private fun LeafWord(day: DayCompanionViewModel.UiState, wisdom: app.rafiq.domain.model.Wisdom?) {
    val rc = LocalRafiqColors.current
    val kahf = if (day.isFriday) day.stations.firstOrNull { it.id == "friday_kahf" } else null
    val (text, source) = when {
        kahf != null && kahf.virtue.isNotBlank() -> kahf.virtue to kahf.source
        wisdom != null -> wisdom.text to listOf(wisdom.author, wisdom.source).filter { it.isNotBlank() }.joinToString(" · ")
        else -> return
    }
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
        Text(stringResource(R.string.leaf_word), style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = rc.gold)
        //  نصٌّ منقولٌ بمصدره — يُعرض كما هو، لا يُختصر.
        Text(text, style = RafiqType.dhikr.copy(fontSize = RafiqType.body.fontSize), color = rc.ink,
            modifier = Modifier.padding(top = 4.dp))
        if (source.isNotBlank()) Text(source, style = RafiqType.metaS, color = rc.inkMed, modifier = Modifier.padding(top = 2.dp))
    }
}

/* ── صلواتُ الورقة من مواقيت الرئيسية ──────────────────────────── */

private val PrayerNames = mapOf(
    "fajr" to R.string.fajr, "dhuhr" to R.string.dhuhr, "asr" to R.string.asr,
    "maghrib" to R.string.maghrib, "isha" to R.string.isha,
)

/**
 * الخمسُ بأوقاتها، والحاضرةُ والقادمةُ موسومتان.
 *
 * الحاضرةُ آخرُ صلاةٍ دخل وقتُها، والقادمةُ أوّلُ ما لم يدخل — والشروقُ
 * يُعدّ في الحساب ولا يُعرض: بعده لا «حاضرة» حتى الظهر.
 */
@Composable
private fun leafPrayers(home: HomeViewModel.UiState, prayed: Set<String>, timed: Boolean): List<LeafPrayer> {
    val ar = LocalArabicNumerals.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); now = System.currentTimeMillis() } }

    val byKey = home.prayers.associateBy { it.en.lowercase() }
    val started = home.prayers.lastOrNull { it.timeMillis in 1..now }?.en?.lowercase()
    val next = home.prayers.firstOrNull { it.timeMillis > now && it.en != "Sunrise" }?.en?.lowercase()
    return LeafViewModel.PRAYERS.map { key ->
        val p = byKey[key]
        val slot = when {
            !timed -> PrayerSlot.NONE
            key == started -> PrayerSlot.NOW
            key == next -> PrayerSlot.NEXT
            else -> PrayerSlot.NONE
        }
        LeafPrayer(
            key = key,
            name = PrayerNames.getValue(key),
            time = if (timed) p?.time else null,
            prayed = key in prayed,
            slot = slot,
            hint = if (slot == PrayerSlot.NEXT && p != null)
                stringResource(R.string.leaf_in, durationLabel(p.timeMillis - now, ar)) else null,
        )
    }
}

/** «الجمعة ١٦ أكتوبر · بغداد» — بلغة الجهاز، والمدينةُ إن عُرفت. */
@Composable
private fun gregorianLine(city: String): String {
    val locale = Locale.getDefault()
    val date = remember(locale) { SimpleDateFormat("EEEE d MMMM", locale).format(Date()) }
    return if (city.isBlank()) date else "$date · $city"
}
