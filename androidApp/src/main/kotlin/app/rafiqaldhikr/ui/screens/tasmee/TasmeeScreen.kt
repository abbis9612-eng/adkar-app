package app.rafiqaldhikr.ui.screens.tasmee

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.WordMark
import app.rafiq.domain.model.tasmeeNormalize
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.FirstHint
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.theme.QuranFamily
import androidx.compose.ui.unit.sp

/*
 * شاشةُ التسميع
 * ════════════
 *
 * ═══ ولماذا لا تقول «صحّ» و«خطأ» ═══
 *
 * **تغليطُ قارئٍ في كتاب الله بخطأِ نموذجٍ أسوأُ من غياب الميزة كلِّها.**
 *
 * فللشاشة ثلاثةُ أحوال: تمّت · فيها مواضعُ تُراجَع · **لم أتبيّن**. والثالثُ
 * ليس خطأً مخفَّفاً — هو إقرارُ الآلةِ أنّها لم تسمع، ويُطلب فيه أن يعيد.
 * ولا يُشار فيه إلى كلمةٍ بسوءٍ أبداً.
 *
 * ═══ والكلمةُ تُشار ولا تُلوَّن حمراء ═══
 *
 * الأحمرُ حكمٌ، والمقصودُ **موضعٌ يُراجَع**. فالمشكوكةُ تُظلَّل بذهبٍ خفيفٍ
 * وتُسطَّر، والمفقودةُ تُخفَّف حبراً — فتُقرأ الآيةُ كلُّها متّصلةً، ويعرف
 * القارئُ أين يعود. ولو لُوّنت حمراءَ لصارت الآيةُ لوحةَ أخطاء.
 */

@Composable
fun TasmeeScreen(nav: NavHostController, surah: Int, ayahNumber: Int) {
    val rc = LocalRafiqColors.current
    val vm: TasmeeViewModel = koinViewModel()
    val stage by vm.stage.collectAsStateWithLifecycle()
    val ayah by vm.ayah.collectAsStateWithLifecycle()

    remember(surah, ayahNumber) { vm.load(surah, ayahNumber); 0 }

    //  الإذنُ يُطلب عند فتح الشاشة لا عند أوّل ضغطة: من ضغط «سمّع» ثمّ
    //  قُوطع بحوار إذنٍ ضاع نفَسُه الأوّل.
    AskForMic()

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        RafiqTopBar(
            title = stringResource(R.string.tasmee_title),
            subtitle = stringResource(R.string.tasmee_subtitle),
            onBack = { nav.popBackStack() },
        )

        Column(Modifier.padding(horizontal = 16.dp)) {
            FirstHint("tasmee", R.string.tasmee_hint)
            Spacer(Modifier.height(14.dp))

            if (!vm.modelPresent()) {
                Notice(stringResource(R.string.tasmee_no_model), rc.inkMed, rc.chipBg)
                return@Column
            }

            val marks = (stage as? TasmeeViewModel.Stage.Judged)?.result?.marks
            AyahBody(ayah, marks)

            Spacer(Modifier.height(22.dp))
            Verdict(stage)
            Spacer(Modifier.height(18.dp))
            Mic(stage) { vm.toggle() }
            Spacer(Modifier.height(10.dp))

            if (stage is TasmeeViewModel.Stage.Judged || stage is TasmeeViewModel.Stage.Unheard) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 24.dp))
                        .background(rc.chipBg)
                        .clickable { vm.again() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.tasmee_again),
                        style = RafiqType.label,
                        color = rc.emerald,
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

/**
 * يطلب إذنَ الميكروفون مرّةً عند فتح الشاشة.
 *
 * ونمطُه نمطُ `util/PermissionHandler.kt` — ولم يُنادَ ذاك لأنّه مخصوصٌ
 * بالإشعارات والموقع، وتوسيعُه ليحمل إذناً ثالثاً يجعله يُطلب في شاشاتٍ
 * لا تحتاجه. والميكروفونُ لا يُطلب إلّا هنا.
 */
@Composable
private fun AskForMic() {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

/**
 * نصُّ الآية، وكلماتُه معلَّمةٌ إن كان حكم.
 *
 * والمحاذاةُ بين كلمات النصّ وعلامات الحكم: الحكمُ يُعلّم **الكلماتَ
 * المطبَّعة**، وفي النصّ علاماتُ وقفٍ (ۖ ۛ) ليست كلماتٍ وتُسقَط في
 * التطبيع. فتُمرَّر الكلماتُ بالترتيب، وما طُبّع إلى فراغٍ لا يستهلك
 * علامةً — ولولا ذلك انزاحت العلاماتُ كلُّها بعد أوّل علامة وقف.
 */
@Composable
private fun AyahBody(ayah: String, marks: List<WordMark>?) {
    val rc = LocalRafiqColors.current
    val text: AnnotatedString = remember(ayah, marks) {
        buildAnnotatedString {
            var at = 0
            val words = ayah.split(' ')
            for ((i, w) in words.withIndex()) {
                if (i > 0) append(' ')
                val normalized = tasmeeNormalize(w)
                val mark = if (normalized.isEmpty()) null else marks?.getOrNull(at)
                if (normalized.isNotEmpty()) at++
                when (mark) {
                    WordMark.SUSPECT -> withStyle(
                        SpanStyle(background = rc.tintGold, color = rc.ink),
                    ) { append(w) }
                    WordMark.SILENT -> withStyle(
                        SpanStyle(color = rc.inkLight),
                    ) { append(w) }
                    else -> append(w)
                }
            }
        }
    }
    /*  بخطّ المصحف لا بخطّ الحديث.
     *
     *  كان `RafiqType.ayah` — وهو أميريٌّ بنسبة ١٫٦٢. وفي التطبيق خطّان:
     *  `QuranFamily` (شهرزاد) للقرآن، و`AmiriFamily` للحديث والأثر.
     *  والمصحفُ وورقةُ الآية يستعملان الأوّل، وهذه الشاشةُ وحدَها كانت
     *  تستعمل الثاني — وهي تعرض **نصّاً عثمانيّاً** مثلَهما.
     *
     *  وأميري Regular ليس خطّاً قرآنيّاً (لأميري نسخةٌ قرآنيّةٌ منفصلة):
     *  جدولُ محارفه يغطّي الـ٦٩ محرفاً المستعملةَ في المصحف كاملةً —
     *  فحصتُه — لكنّ **تركيبَ العلامات** فيه ليس للعثمانيّ، فتسقط
     *  علاماتٌ عند تراكبها.
     *
     *  والنسبةُ ١٫٦٢ تقصّ ما يعلو ويسفل من العلامات. فصارت ٢٥ على ٤٨
     *  كورقة الآية بالضبط — نصٌّ واحدٌ يُرسم رسماً واحداً في الشاشتين. */
    Text(
        text,
        fontFamily = QuranFamily,
        fontSize = 25.sp,
        lineHeight = 48.sp,
        color = rc.ink,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** سطرُ الحكم — ثلاثةُ أحوالٍ لا اثنان. */
@Composable
private fun Verdict(stage: TasmeeViewModel.Stage) {
    val rc = LocalRafiqColors.current
    val digits = LocalArabicNumerals.current
    when (stage) {
        is TasmeeViewModel.Stage.Judged -> {
            val r = stage.result
            if (r.marks.none { it != WordMark.HEARD }) {
                Notice(stringResource(R.string.tasmee_clear), rc.emerald, rc.emeraldPastel)
            } else {
                val n = r.total - r.heard
                Notice(
                    stringResource(R.string.tasmee_review, n.localized(digits)),
                    rc.gold, rc.tintGold,
                )
            }
        }
        is TasmeeViewModel.Stage.Unheard ->
            Notice(stringResource(R.string.tasmee_unheard), rc.inkMed, rc.chipBg)
        TasmeeViewModel.Stage.NoMic ->
            Notice(stringResource(R.string.tasmee_no_mic), rc.inkMed, rc.chipBg)
        TasmeeViewModel.Stage.NoModel ->
            Notice(stringResource(R.string.tasmee_no_model), rc.inkMed, rc.chipBg)
        TasmeeViewModel.Stage.Thinking ->
            Notice(stringResource(R.string.tasmee_thinking), rc.inkMed, rc.chipBg)
        is TasmeeViewModel.Stage.Recording -> {
            val left = TasmeeViewModel.LIMIT_SECONDS - stage.seconds.toInt()
            Notice(
                stringResource(R.string.tasmee_recording, left.localized(digits)),
                rc.emerald, rc.emeraldPastel,
            )
        }
        TasmeeViewModel.Stage.Idle ->
            Notice(stringResource(R.string.tasmee_ready), rc.inkMed, rc.chipBg)
    }
}

@Composable
private fun Notice(text: String, ink: Color, bg: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 20.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = RafiqType.bodyS, color = ink, textAlign = TextAlign.Center)
    }
}

/**
 * زرُّ التسميع.
 *
 * ويخفق ما دام يسجّل — لا زينةً بل **ليعرف أنّ الميكروفونَ مفتوحٌ فعلاً**.
 * وشاشةٌ تسجّل بلا علامةٍ حيّةٍ تجعل صاحبَها يقرأ ولا يُسمَع.
 */
@Composable
private fun Mic(stage: TasmeeViewModel.Stage, onClick: () -> Unit) {
    val rc = LocalRafiqColors.current
    val live = stage is TasmeeViewModel.Stage.Recording
    val busy = stage is TasmeeViewModel.Stage.Thinking

    val pulse by rememberInfiniteTransition(label = "mic").animateFloat(
        initialValue = 1f,
        targetValue = if (live) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .scale(pulse)
                .size(86.dp)
                .clip(CircleShape)
                .background(if (live) rc.emeraldFill else rc.chipBg)
                .clickable(enabled = !busy, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            RafiqIcon(
                icon = if (live) RIcon.Check else RIcon.Mic,
                size = 34.dp,
                tint = if (live) rc.onEmeraldFill else rc.emerald,
                contentDescription = stringResource(
                    if (live) R.string.tasmee_stop else R.string.tasmee_start,
                ),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(if (live) R.string.tasmee_stop else R.string.tasmee_start),
        style = RafiqType.caption,
        color = rc.inkMed,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
