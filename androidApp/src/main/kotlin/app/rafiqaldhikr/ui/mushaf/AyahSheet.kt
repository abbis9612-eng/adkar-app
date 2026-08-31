package app.rafiqaldhikr.ui.mushaf

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.NaskhFamily
import app.rafiqaldhikr.ui.theme.QuranFamily
import app.rafiqaldhikr.ui.theme.RafiqType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/* ══════════════════════════════════════════════════════════════
   ورقةُ الآية

   كان نقرُ الآية في المصحف لا يفعل شيئاً سوى التلوين، وكان في الحزمة
   ٢٫٦ ميغابايت من التفسير الميسّر لا يصل إليها المستخدم بأيّ طريق،
   وكانت شاشةُ العلامات تعرض ولا شيءَ يكتب فيها فتبقى فارغةً أبداً.
   وهذه الورقةُ تصل الثلاثةَ في موضعٍ واحد.

   والآيةُ هنا تُرسم بخطّ نصٍّ عاديّ لا بخطّ QCF: ذاك خطُّ رموزٍ لا
   حروف، لا يُصيّر النصَّ العثمانيَّ أصلاً.
══════════════════════════════════════════════════════════════ */

@Composable
fun AyahSheet(
    verse: String?,
    /** صفحةُ المصحف التي فُتحت منها — بها تُلتقط الآيةُ من القاعدة. */
    page: Int,
    night: Boolean,
    onDismiss: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val vm: MushafPageViewModel = koinViewModel()

    val surah = verse?.substringBefore(':')?.toIntOrNull() ?: 0
    val ayah = verse?.substringAfter(':')?.toIntOrNull() ?: 0

    var text by remember(verse) { mutableStateOf("") }
    var tafsir by remember(verse) { mutableStateOf<String?>(null) }
    var marked by remember(verse) { mutableStateOf(false) }
    var loading by remember(verse) { mutableStateOf(true) }

    LaunchedEffect(verse) {
        if (verse == null) return@LaunchedEffect
        loading = true
        text = runCatching { vm.ayah(surah, ayah, page) }.getOrNull()?.textUthmani.orEmpty()
        tafsir = runCatching { vm.tafsir(surah, ayah) }.getOrNull()
        marked = runCatching { vm.isMarked(surah, ayah) }.getOrDefault(false)
        loading = false
    }

    val paper = if (night) Color(0xFF1A1712) else rc.bg
    val ink = if (night) Color(0xFFE8E1CF) else rc.ink
    val hair = ink.copy(alpha = 0.16f)

    if (verse != null) {
        // حاجبٌ يبتلع النقرَ خلف الورقة فلا تُقلَب الصفحةُ تحتها.
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = verse != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(paper)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { /* تبتلع النقرَ فلا يصل إلى الحاجب */ }
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp)
                    .padding(top = 12.dp, bottom = 18.dp),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(38.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(hair),
                )
                Spacer(Modifier.height(13.dp))

                Text(
                    "${SurahNames.of(ctx, surah)} · الآية $ayah",
                    fontFamily = NaskhFamily,
                    fontSize = 13.sp,
                    color = ink.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(9.dp))

                Text(
                    text.ifBlank { if (loading) "…" else "" },
                    fontFamily = QuranFamily,
                    fontSize = 25.sp,
                    lineHeight = 48.sp,
                    color = ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(13.dp))

                val tf = tafsir
                if (!tf.isNullOrBlank()) {
                    Text(
                        tf,
                        fontFamily = NaskhFamily,
                        fontSize = 14.sp,
                        lineHeight = 27.sp,
                        color = ink.copy(alpha = 0.82f),
                        modifier = Modifier
                            .heightIn(max = 190.dp)
                            .verticalScroll(rememberScrollState()),
                    )
                    Spacer(Modifier.height(15.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetAction(
                        label = if (marked) "مُعَلَّمة" else "ضَعْ علامة",
                        icon = if (marked) RIcon.Check else RIcon.Bookmark,
                        filled = marked,
                        ink = ink,
                        hair = hair,
                        modifier = Modifier.weight(1.3f),
                    ) {
                        scope.launch { marked = vm.toggleMark(surah, ayah, page) }
                    }
                    SheetAction("نسخ", RIcon.Copy, false, ink, hair, Modifier.weight(1f)) {
                        clip.setText(AnnotatedString(shareBody(text, tf, surah, ayah, ctx)))
                    }
                    SheetAction("مشاركة", RIcon.Share, false, ink, hair, Modifier.weight(1f)) {
                        val i = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareBody(text, tf, surah, ayah, ctx))
                        }
                        ctx.startActivity(Intent.createChooser(i, "مشاركةُ الآية"))
                    }
                }
            }
        }
    }
}

/*  النصُّ الديني يُنقل كما هو حرفاً بحرف — ولا يُختصر ولا يُعاد صوغُه. */
private fun shareBody(ayah: String, tafsir: String?, s: Int, a: Int, ctx: android.content.Context): String {
    val head = "${SurahNames.of(ctx, s)} · الآية $a"
    return buildString {
        append(ayah).append("\n").append(head)
        if (!tafsir.isNullOrBlank()) append("\n\nالتفسير الميسّر:\n").append(tafsir)
    }
}

@Composable
private fun SheetAction(
    label: String,
    icon: RIcon,
    filled: Boolean,
    ink: Color,
    hair: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (filled) rc.emeraldFill else Color.Transparent)
            .border(1.dp, if (filled) rc.emeraldFill else hair, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RafiqIcon(icon, 16.dp, if (filled) rc.onEmeraldFill else ink)
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = RafiqType.label,
            color = if (filled) rc.onEmeraldFill else ink,
            maxLines = 1,
        )
    }
}
