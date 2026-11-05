package app.rafiqaldhikr.ui.screens.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.IcoShare
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File

/* ═══════════════════════════════════════════════════════════════════
   بطاقة المشاركة

   كانت الشاشةُ ترسم بطاقةً جميلةً ثمّ **ترميها**: زرُّ المشاركة يُرسل
   `type = "text/plain"` و`EXTRA_TEXT` فقط. فيصل واتساب ثلاثةُ أسطرٍ
   نصّية، والبطاقةُ التي اختارها المستخدمُ ونظر إليها لم تخرج من الجهاز.
   وهي شاشةٌ اسمُها «مشاركة بطاقة» — فكانت تفعل الشيءَ الوحيدَ الذي لا
   يبرّر وجودَها.

   والآن تُلتقط البطاقةُ المعروضةُ نفسُها عبر `rememberGraphicsLayer`
   وتُكتب PNG في `cacheDir/shares`، وتُشارَك `image/png` + `EXTRA_STREAM`
   من نفس `FileProvider` الذي أُضيف للتصدير — لا مزوّدَ جديد ولا إذن.

   والملتقَطُ هو ما رُئي بالضبط: لا رسمَ ثانياً على قماشٍ منفصلٍ يمكن أن
   يفترق عن المعاينة كما افترقت دائرةُ التنفّس عن نصِّها.
═══════════════════════════════════════════════════════════════════ */

@Composable
fun ShareCardScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val rc      = LocalRafiqColors.current
    val ar      = LocalArabicNumerals.current
    val scope   = rememberCoroutineScope()

    /*  `isLoading` كانت تُحسب في `ProfileViewModel` منذ أوّل يوم و**لا
     *  يقرؤها أحدٌ من الشاشات الأربع** التي تشاركه. فيفتح المستخدمُ
     *  الشاشةَ فيرى أصفاراً — سلسلةٌ صفر، أيّامٌ صفر — ثمّ تُصحَّح بعد
     *  إطارٍ أو إطارين. والصفرُ الكاذبُ في شاشةِ مواظبةٍ ليس بطيئاً
     *  فحسب: هو يقول لصاحب الأربعين يوماً إنّه بدأ اليوم. */
    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(rc.bg)) { LoadingState() }
        return
    }

    var selected by remember { mutableIntStateOf(0) }

    val streak = state.streak.current.toInt()
    val pages  = (state.todayProgress?.quranPages ?: 0L).toInt()
    val beads  = (state.todayProgress?.tasbeehCount ?: 0L).toInt()

    /*  الجموعُ من `plurals` لا من التسلسل: «٢ صفحات» ليست عربية،
     *  والصواب «صفحتان». والأرقامُ تتبع اختيارَ المستخدم. */
    val cards = listOf(
        ShareCardData(
            stringResource(R.string.share_card_streak),
            pluralStringResource(R.plurals.days, streak, streak).localizedDigits(ar),
            stringResource(R.string.share_sub_streak),
        ),
        ShareCardData(
            stringResource(R.string.share_card_quran),
            pluralStringResource(R.plurals.pages, pages, pages).localizedDigits(ar),
            stringResource(R.string.share_sub_quran),
        ),
        ShareCardData(
            stringResource(R.string.share_card_tasbeeh),
            pluralStringResource(R.plurals.tasbeehat, beads, beads).localizedDigits(ar),
            stringResource(R.string.share_sub_tasbeeh),
        ),
    )

    // طبقةُ الرسم التي تُلتقط منها الصورة — البطاقةُ المعروضة نفسُها.
    val layer = rememberGraphicsLayer()
    val shareFailed = stringResource(R.string.share_failed)
    val chooser     = stringResource(R.string.action_share)

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RafiqTopBar(
                title  = stringResource(R.string.share_title),
                onBack = { navController.popBackStack() },
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val card = cards[selected]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RafiqShape.card)
                        .drawWithContent {
                            // يُسجَّل المحتوى في الطبقة ثمّ تُرسم — فما يُرى
                            // هو ما يُلتقط، بايتاً ببايت.
                            layer.record { this@drawWithContent.drawContent() }
                            drawLayer(layer)
                        }
                        .background(Brush.linearGradient(listOf(rc.emerald, rc.gold))),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.onEmerald)
                        Spacer(Modifier.height(12.dp))
                        Text(card.value, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = rc.onEmerald)
                        Spacer(Modifier.height(8.dp))
                        Text(card.subtitle, color = rc.onEmerald.copy(alpha = 0.85f), style = RafiqType.bodyS)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.app_name),
                            color = rc.onEmerald.copy(alpha = 0.65f),
                            style = RafiqType.caption,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    cards.forEachIndexed { i, c ->
                        val on = selected == i
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RafiqShape.item)
                                .background(if (on) rc.emerald else rc.card)
                                .border(
                                    1.dp,
                                    if (on) rc.emerald else rc.gold.copy(alpha = BorderIdle),
                                    RafiqShape.item,
                                )
                                .clickable { selected = i }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                c.title,
                                fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                                color = if (on) rc.onEmerald else rc.ink,
                                style = RafiqType.bodyS,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RafiqShape.card)
                        .background(rc.emeraldFill)
                        .clickable {
                            scope.launch {
                                val ok = runCatching {
                                    val bmp = layer.toImageBitmap().asAndroidBitmap()
                                    shareBitmap(context, bmp, chooser)
                                }.getOrDefault(false)
                                if (!ok) {
                                    Toast.makeText(context, shareFailed, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IcoShare(20.dp, rc.onEmeraldFill)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.share_action),
                            fontWeight = FontWeight.Bold,
                            color = rc.onEmeraldFill,
                            style = RafiqType.body,
                        )
                    }
                }
            }
        }
    }
}

/**
 * يكتب الصورةَ في `cacheDir/shares` ويُرسلها عبر `FileProvider`.
 *
 * نفسُ المزوّد الذي يخدم التصدير — أُضيف مسارُ `shares/` إلى
 * `res/xml/file_paths.xml` بجانب `exports/`، ولا مزوّدَ ثانيَ ولا إذنَ
 * تخزينٍ: `FLAG_GRANT_READ_URI_PERMISSION` وحدَه يفتح الملفَّ للمستقبِل.
 *
 * @return false إن لم يوجد تطبيقٌ يقبل الصورة — فتُعرض رسالةٌ ولا يسقط
 *         التطبيق، كما في [app.rafiqaldhikr.util.sendMail].
 */
private fun shareBitmap(context: Context, bitmap: Bitmap, chooserTitle: String): Boolean {
    val dir = File(context.cacheDir, "shares").apply { mkdirs() }
    // بطاقةٌ واحدةٌ في الذاكرة المؤقّتة — لا تتراكم صورٌ مع كل مشاركة.
    dir.listFiles()?.forEach { it.delete() }
    val file = File(dir, "rafiq-card.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        context.startActivity(Intent.createChooser(intent, chooserTitle))
        true
    } catch (e: android.content.ActivityNotFoundException) {
        false
    }
}

private data class ShareCardData(val title: String, val value: String, val subtitle: String)
