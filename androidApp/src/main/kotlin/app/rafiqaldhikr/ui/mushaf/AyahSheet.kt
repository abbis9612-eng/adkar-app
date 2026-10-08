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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import app.rafiqaldhikr.ui.share.renderAyahCard
import app.rafiqaldhikr.ui.share.shareBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rafiqaldhikr.R
import androidx.compose.ui.res.stringResource
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
    /** يُستدعى بـ«سورة:آية» عند التنقّل داخل الورقة. */
    onVerse: (String) -> Unit = {},
    /** يُستدعى بـ«سورة:آية» لفتح التسميع. */
    onTasmee: (String) -> Unit = {},
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
    /*  آياتُ الصفحة — بها يُعرف ما قبل الآية وما بعدها.
     *
     *  والحدودُ **حدودُ الصفحة لا حدودُ السورة**: من بلغ آخرَ آيةٍ في
     *  الصفحة انتهى تنقّلُه، ولا يُقفز به إلى صفحةٍ أخرى والورقةُ فوقها
     *  — فيجد تحتها نصّاً غيرَ الذي كان يقرأ. */
    val pageVerses by vm.pageFlow(page).collectAsStateWithLifecycle(emptyList())
    val idx = pageVerses.indexOfFirst { it.surah == surah && it.ayahNumber == ayah }
    val prev = pageVerses.getOrNull(idx - 1)?.takeIf { idx > 0 }
    val next = pageVerses.getOrNull(idx + 1)
    /*  الملاحظة: `note` هو المحفوظ، و`draft` ما يُكتب الآن، و`editing`
     *  هل الحقلُ مفتوح. وفصلُ الثلاثة لازم: من فتح الحقلَ وغيّر رأيَه
     *  يُغلقه فيرجع المحفوظُ كما كان بلا حفظٍ ضمنيّ. */
    var note by remember(verse) { mutableStateOf<String?>(null) }
    var draft by remember(verse) { mutableStateOf("") }
    var editing by remember(verse) { mutableStateOf(false) }

    LaunchedEffect(verse) {
        if (verse == null) return@LaunchedEffect
        loading = true
        text = runCatching { vm.ayah(surah, ayah) }.getOrNull()?.textUthmani.orEmpty()
        tafsir = runCatching { vm.tafsir(surah, ayah) }.getOrNull()
        marked = runCatching { vm.isMarked(surah, ayah) }.getOrDefault(false)
        note = runCatching { vm.note(surah, ayah) }.getOrNull()
        draft = note.orEmpty()
        editing = false
        loading = false
    }

    // يُقرأ في التأليف: `stringResource` لا تُنادى داخل onClick.
    val shareTitle = stringResource(R.string.ayah_share_title)
    val ayahLabel = if (surah > 0) {
        stringResource(R.string.ayah_label, SurahNames.of(ctx, surah), ayah.toString())
    } else {
        ""
    }
    val appName = stringResource(R.string.app_name)
    val shareFailed = stringResource(R.string.share_failed)
    /*  «مشاركة» صارت بابين: نصّاً وصورةً. ويُسأل السؤالُ عند الضغط لا
     *  بزرّين في الصفّ — الصفُّ خمسةُ أفعالٍ أصلاً، والسادسُ يُضيّقها. */
    var sharing by remember(verse) { mutableStateOf(false) }
    val stopped = stringResource(R.string.ayah_stop_here)

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

                /*  سطرُ التنقّل: ⌃ «البقرة · الآية ١٣» ⌄
                 *
                 *  قراءةُ تفسيرِ صفحةٍ كاملةٍ كانت تكلّف إحدى وعشرين ضغطة
                 *  (إغلاقٌ وبحثٌ وضغطٌ مطوّلٌ لكلّ آية). وصارت سبعاً. */
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepDot(RIcon.ChevronRight, prev != null, ink) {
                        prev?.let { onVerse("${it.surah}:${it.ayahNumber}") }
                    }
                    Text(
                        stringResource(
                            R.string.ayah_label,
                            SurahNames.of(ctx, surah),
                            ayah.toString(),
                        ),
                        fontFamily = NaskhFamily,
                        fontSize = 13.sp,
                        color = ink.copy(alpha = 0.55f),
                    )
                    StepDot(RIcon.ChevronLeft, next != null, ink) {
                        next?.let { onVerse("${it.surah}:${it.ayahNumber}") }
                    }
                }
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

                /*  الملاحظةُ المحفوظة تُعرض فوق الأفعال لا تحتها:
                 *  هي **كلامُ المستخدم** لا فعلاً يفعله، وموضعُها مع
                 *  النصّ والتفسير لا مع الأزرار. */
                val n = note
                if (!editing && !n.isNullOrBlank()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(ink.copy(alpha = 0.05f))
                            .clickable { editing = true; draft = n }
                            .padding(horizontal = 11.dp, vertical = 9.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .heightIn(min = 16.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(rc.gold),
                        )
                        Text(
                            n,
                            fontFamily = NaskhFamily,
                            fontSize = 13.sp,
                            lineHeight = 24.sp,
                            color = ink.copy(alpha = 0.88f),
                        )
                    }
                    Spacer(Modifier.height(13.dp))
                }

                if (editing) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.ayah_note_hint),
                                fontFamily = NaskhFamily,
                                fontSize = 13.sp,
                                color = ink.copy(alpha = 0.45f),
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = NaskhFamily,
                            fontSize = 14.sp,
                            lineHeight = 26.sp,
                            color = ink,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = rc.gold,
                            unfocusedBorderColor = hair,
                            cursorColor = rc.gold,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(11.dp),
                        minLines = 2,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetAction(
                            stringResource(R.string.action_save), RIcon.Check, true,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            scope.launch {
                                vm.setNote(surah, ayah, page, draft)
                                note = draft.trim().takeIf { it.isNotEmpty() }
                                editing = false
                            }
                        }
                        SheetAction(
                            stringResource(R.string.action_close), RIcon.Close, false,
                            ink, hair, Modifier.weight(1f),
                        ) { draft = note.orEmpty(); editing = false }
                    }
                    Spacer(Modifier.height(13.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetAction(
                        label = if (marked) stringResource(R.string.ayah_bookmarked) else stringResource(R.string.ayah_bookmark),
                        icon = if (marked) RIcon.Check else RIcon.Bookmark,
                        filled = marked,
                        ink = ink,
                        hair = hair,
                        modifier = Modifier.weight(1.3f),
                    ) {
                        scope.launch { marked = vm.toggleMark(surah, ayah, page) }
                    }
                    SheetAction(stringResource(R.string.action_copy), RIcon.Copy, false, ink, hair, Modifier.weight(1f)) {
                        clip.setText(AnnotatedString(shareBody(text, tf, surah, ayah, ctx)))
                    }
                    SheetAction(
                        stringResource(R.string.ayah_note), RIcon.Edit, false,
                        ink, hair, Modifier.weight(1f),
                    ) { draft = note.orEmpty(); editing = true }
                    /*  «فاصل» ≠ «علامة».
                     *
                     *  العلامةُ تبقى، والفاصلُ موضعُ وقوفٍ **واحدٌ** يتبدّل
                     *  كلَّ يوم. وكانا زرّاً واحداً، فتمتلئ قائمةُ العلامات
                     *  بمواضعَ قديمةٍ لا معنى لها فتضيع المقصودةُ بينها. */
                    SheetAction(
                        stringResource(R.string.ayah_stop), RIcon.Pin, false,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        scope.launch {
                            vm.setStop(surah, ayah, page)
                            Toast.makeText(ctx, stopped, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                    SheetAction(stringResource(R.string.action_share), RIcon.Share, false, ink, hair, Modifier.weight(1f)) {
                        sharing = true
                    }
                }

                /*  التسميعُ في صفٍّ لنفسه لا سادساً في الصفّ.
                 *
                 *  فالخمسةُ أفعالٌ صغيرةٌ على الآية (علامةٌ · نسخٌ · حاشيةٌ
                 *  · وقوفٌ · مشاركة)، والتسميعُ **عملٌ يفتح شاشة**. وسادسٌ
                 *  في الصفّ يُضيّق الخمسةَ ويُخفي الفرق. */
                Spacer(Modifier.height(9.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp, 13.dp, 13.dp, 22.dp))
                        .background(ink.copy(alpha = 0.06f))
                        .clickable {
                            onDismiss()
                            onTasmee("$surah:$ayah")
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RafiqIcon(RIcon.Mic, 18.dp, ink.copy(alpha = 0.78f))
                        Text(
                            stringResource(R.string.action_tasmee),
                            style = RafiqType.label,
                            color = ink.copy(alpha = 0.9f),
                        )
                    }
                }

                if (sharing) {
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetAction(
                            stringResource(R.string.share_as_text), RIcon.Copy, false,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            sharing = false
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareBody(text, tf, surah, ayah, ctx))
                            }
                            ctx.startActivity(Intent.createChooser(i, shareTitle))
                        }
                        SheetAction(
                            stringResource(R.string.share_as_image), RIcon.Share, true,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            sharing = false
                            scope.launch {
                                /*  الرسمُ والكتابةُ على خيط الإدخال والإخراج:
                                 *  بطاقةٌ بعرض ١٠٨٠ وضغطُ PNG ليسا عملَ
                                 *  الخيط الرئيسيّ. */
                                val ok = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val bmp = renderAyahCard(
                                            ctx = ctx,
                                            ayahText = text,
                                            label = ayahLabel,
                                            appName = appName,
                                            bg = paper.toArgb(),
                                            ink = ink.toArgb(),
                                            gold = rc.gold.toArgb(),
                                        )
                                        shareBitmap(ctx, bmp, shareTitle, "rafiq-ayah.png")
                                    }.getOrDefault(false)
                                }
                                if (!ok) {
                                    Toast.makeText(ctx, shareFailed, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * زرُّ خطوةٍ في سطر التنقّل — يُعطَّل عند الطرف ولا يختفي.
 *
 * واختفاؤه كان يُزحزح العنوانَ يميناً وشمالاً مع كل آية، فيرقص السطر.
 * والمعطَّلُ يقول «لا مزيدَ هنا» وهو أصدقُ من الغياب.
 */
@Composable
private fun StepDot(icon: RIcon, enabled: Boolean, ink: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RafiqIcon(icon, 17.dp, ink.copy(alpha = if (enabled) 0.6f else 0.18f))
    }
}

/*  النصُّ الديني يُنقل كما هو حرفاً بحرف — ولا يُختصر ولا يُعاد صوغُه. */
private fun shareBody(ayah: String, tafsir: String?, s: Int, a: Int, ctx: android.content.Context): String {
    val head = ctx.getString(R.string.ayah_label, SurahNames.of(ctx, s), a.toString())
    return buildString {
        append(ayah).append("\n").append(head)
        if (!tafsir.isNullOrBlank()) {
            append("\n\n").append(ctx.getString(R.string.tafsir_label)).append("\n").append(tafsir)
        }
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
