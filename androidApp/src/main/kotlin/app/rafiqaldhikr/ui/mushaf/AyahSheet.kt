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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import app.rafiqaldhikr.ui.theme.tapSpec

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
    val ar = LocalArabicNumerals.current
    val ink = if (night) Color(0xFFE8E1CF) else rc.ink
    val hair = ink.copy(alpha = 0.16f)

    /*  حاجبٌ يُعتم الصفحةَ ويبتلع النقرَ خلف الورقة.
     *
     *  كان شفّافاً تماماً: يمنع قلبَ الصفحة ولا يُرى. فتبدأ الورقةُ
     *  بحدٍّ صلبٍ على نصٍّ كاملِ الوضوح، ويُقطع السطرُ الذي تحتها نصفين
     *  — نصفٌ مقروءٌ ونصفٌ مبتور، يُقرأ عطباً في الرسم لا طبقةً فوقه.
     *
     *  وبإعتامٍ خفيفٍ يرجع الترتيبُ إلى عينه: صفحةٌ ثمّ شيءٌ فوقها.
     *  وهو يتلاشى مع الورقة لا يقفز معها. */
    AnimatedVisibility(
        visible = verse != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(rc.scrim.copy(alpha = 0.38f))
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
                    //  سقفٌ على ارتفاعها وتمريرٌ **واحد**: كان التفسيرُ
                    //  يتمرّر داخلَ ورقةٍ لا تتمرّر، فيقع تمريرٌ في تمرير
                    //  ويخرج ما تحته عن الشاشة بلا سبيلٍ إليه.
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState())
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
                Spacer(Modifier.height(15.dp))

                /*  ═══ رأسُ الورقة: وردةُ الآية واسمُ السورة ═══
                 *
                 *  رقمُ الآية في **وردةٍ** لا في سطرِ نصّ. وهي علامةُ آخرِ
                 *  الآية في المصحف المطبوع نفسِها — فالقارئُ يعرفها قبل
                 *  أن يُشرح له، ولا تحتاج عنواناً يقول «الآية». */
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    AyahRosette(ayah, rc.gold, ink)
                    //  اسمُ السورة وحدَه: رقمُ الصفحة مكتوبٌ على الصفحة
                    //  التي تحت الورقة، وإعادتُه هنا سطرٌ يُقرأ ولا يفيد.
                    Text(
                        SurahNames.of(ctx, surah),
                        style = RafiqType.titleM,
                        color = ink,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text.ifBlank { if (loading) "…" else "" },
                    fontFamily = QuranFamily,
                    fontSize = 25.sp,
                    lineHeight = 48.sp,
                    color = ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                /*  ═══ القسمُ الأوّل: ما تصنع بها ═══
                 *
                 *  ستُّ خاناتٍ في صفّين لا ستٌّ في صفّ: الخانةُ تتّسع
                 *  فيُكتب الاسمُ كاملاً بخطّ الواجهة لا بأصغرِ ما عندنا.
                 *
                 *  ولونُ الحدّ يحمل معنًى لا زينة:
                 *    · حدٌّ رماديّ  → فعلٌ يقع هنا وتبقى في الورقة
                 *    · ممتلئٌ زمرديّ → حالةٌ قائمة (الآيةُ معلَّمة)
                 *    · حدٌّ ذهبيّ   → يخرج بك من الورقة إلى شاشة (التسميع)
                 *
                 *  فالتسميعُ كان زرّاً بعرض الورقة لأنّه «يفتح شاشة»،
                 *  والفرقُ صحيحٌ لكنّه لا يحتاج شكلاً مختلفاً — يحتاج
                 *  لوناً. والشبكةُ تبقى شبكةً بلا يتيمٍ في آخرها. */
                SectionRule(stringResource(R.string.ayah_sec_do), hair, ink)

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    SheetAction(
                        label = stringResource(
                            if (marked) R.string.ayah_bookmarked else R.string.ayah_mark_short,
                        ),
                        icon = if (marked) RIcon.Check else RIcon.Bookmark,
                        tone = if (marked) ActionTone.On else ActionTone.Here,
                        ink = ink,
                        hair = hair,
                        modifier = Modifier.weight(1f),
                    ) {
                        scope.launch { marked = vm.toggleMark(surah, ayah, page) }
                    }
                    SheetAction(
                        stringResource(R.string.ayah_stop), RIcon.Pin, ActionTone.Here,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        scope.launch {
                            vm.setStop(surah, ayah, page)
                            Toast.makeText(ctx, stopped, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                    SheetAction(
                        stringResource(R.string.action_copy), RIcon.Copy, ActionTone.Here,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        clip.setText(AnnotatedString(shareBody(text, tafsir, surah, ayah, ctx)))
                    }
                }
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    SheetAction(
                        stringResource(R.string.action_share), RIcon.Share, ActionTone.Here,
                        ink, hair, Modifier.weight(1f),
                    ) { sharing = true }
                    SheetAction(
                        stringResource(R.string.ayah_note), RIcon.Edit, ActionTone.Here,
                        ink, hair, Modifier.weight(1f),
                    ) { draft = note.orEmpty(); editing = true }
                    SheetAction(
                        stringResource(R.string.action_tasmee_short), RIcon.Mic, ActionTone.Away,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        onDismiss()
                        onTasmee("$surah:$ayah")
                    }
                }

                /*  ═══ القسمُ الثاني: تدبُّرها ═══
                 *
                 *  التفسيرُ كان جداراً مفتوحاً دائماً بارتفاعٍ أقصاه ١٩٠
                 *  نقطةً **وتمريرٌ داخلَه**: فتُدفَع الأفعالُ خارجَ الشاشة،
                 *  ويقع تمريرٌ داخل تمرير. وأكثرُ من يفتح الورقةَ يريد
                 *  فعلاً سريعاً لا قراءةَ تفسير.
                 *
                 *  فصار بطاقةً تُفتح: سطران يُقرآن بلا لمسة، والبقيّةُ
                 *  بلمسة. ولا يُقتطع النصُّ بثلاث نقاطٍ عند الفتح — يُعرض
                 *  كاملاً، والورقةُ كلُّها تتمرّر مرّةً واحدة. */
                val tf2 = tafsir
                val n = note
                if (!tf2.isNullOrBlank() || !n.isNullOrBlank() || editing) {
                    SectionRule(stringResource(R.string.ayah_sec_ponder), hair, ink)
                }

                if (!tf2.isNullOrBlank()) {
                    TafsirCard(tf2, ink, hair, rc.gold)
                    Spacer(Modifier.height(9.dp))
                }

                /*  الملاحظةُ كلامُ صاحبِ المصحف لا كلامَ العلماء — فلها
                 *  خيطٌ ذهبيٌّ على حافّتها يفصل صوتَه عن صوتهم. */
                if (!editing && !n.isNullOrBlank()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp, 13.dp, 13.dp, 20.dp))
                            .background(ink.copy(alpha = 0.05f))
                            .clickable { editing = true; draft = n }
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            fontSize = 14.sp,
                            lineHeight = 26.sp,
                            color = ink.copy(alpha = 0.88f),
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                }

                if (editing) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.ayah_note_hint),
                                style = RafiqType.bodyS,
                                color = ink.copy(alpha = 0.40f),
                            )
                        },
                        textStyle = RafiqType.bodyS.copy(color = ink),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        SheetAction(
                            stringResource(R.string.action_save), RIcon.Check, ActionTone.On,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            scope.launch {
                                vm.setNote(surah, ayah, page, draft)
                                note = draft.trim().takeIf { it.isNotEmpty() }
                                editing = false
                            }
                        }
                        SheetAction(
                            stringResource(R.string.action_close), RIcon.Close, ActionTone.Here,
                            ink, hair, Modifier.weight(1f),
                        ) { draft = note.orEmpty(); editing = false }
                    }
                    Spacer(Modifier.height(9.dp))
                }

                /*  ═══ التنقّلُ أسفلَ الورقة ═══
                 *
                 *  كان في رأسها، وهو أبعدُ ما يكون عن الإبهام على هاتفٍ
                 *  يُمسك بيدٍ واحدة. وقراءةُ تفسير صفحةٍ كاملةٍ تمرُّ على
                 *  هذا الزرّ خمسَ عشرةَ مرّة — فموضعُه ليس تفصيلاً.
                 *
                 *  والمعطَّلُ يبقى ظاهراً عند طرف الصفحة: اختفاؤه كان
                 *  يُزحزح العنوانَ مع كلّ آية فيرقص السطر. */
                Spacer(Modifier.height(5.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 24.dp))
                        .background(ink.copy(alpha = 0.045f))
                        .padding(horizontal = 6.dp, vertical = 5.dp),
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
                            ayah.localized(ar),
                        ),
                        style = RafiqType.label,
                        color = ink.copy(alpha = 0.72f),
                    )
                    StepDot(RIcon.ChevronLeft, next != null, ink) {
                        next?.let { onVerse("${it.surah}:${it.ayahNumber}") }
                    }
                }

                if (sharing) {
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetAction(
                            stringResource(R.string.share_as_text), RIcon.Copy, ActionTone.Here,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            sharing = false
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareBody(text, tafsir, surah, ayah, ctx))
                            }
                            ctx.startActivity(Intent.createChooser(i, shareTitle))
                        }
                        SheetAction(
                            stringResource(R.string.share_as_image), RIcon.Share, ActionTone.On,
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

/**
 * نبرةُ الخانة — لونُ حدِّها يقول ماذا تفعل، لا يزيّنها.
 *
 * [Here] فعلٌ يقع في الورقة وتبقى فيها · [On] حالةٌ قائمةٌ الآن ·
 * [Away] يخرج بك من الورقة إلى شاشةٍ أخرى.
 */
private enum class ActionTone { Here, On, Away }

/** خانةُ فعلٍ في الشبكة — رمزٌ فوق اسمٍ كامل. */
@Composable
private fun SheetAction(
    label: String,
    icon: RIcon,
    tone: ActionTone,
    ink: Color,
    hair: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val shape = RoundedCornerShape(14.dp, 14.dp, 14.dp, 22.dp)
    val border = when (tone) {
        ActionTone.Here -> hair
        ActionTone.On -> rc.emeraldFill
        ActionTone.Away -> rc.gold.copy(alpha = 0.45f)
    }
    val content = when (tone) {
        ActionTone.Here -> ink
        ActionTone.On -> rc.onEmeraldFill
        ActionTone.Away -> rc.gold
    }
    Column(
        modifier
            .heightIn(min = 74.dp)
            //  توقيعُ الزاوية الواحدة الأوسع — كسائر أسطح التطبيق.
            .clip(shape)
            .background(if (tone == ActionTone.On) rc.emeraldFill else Color.Transparent)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        RafiqIcon(icon, 19.dp, content)
        Spacer(Modifier.height(7.dp))
        Text(
            label,
            style = RafiqType.label,
            color = if (tone == ActionTone.Here) ink.copy(alpha = 0.88f) else content,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * وردةُ الآية — علامةُ آخرِ الآية في المصحف المطبوع، تحمل رقمَها.
 *
 * ═══ ولماذا هي وليست رقماً في سطر ═══
 *
 * «الآية ٢١» سطرُ بياناتٍ يصلح لأيّ تطبيق. والوردةُ **من المصحف نفسِه**:
 * القارئُ يراها آخرَ كلّ آيةٍ على الورقة التي تحته الآن، فيعرف ما تعنيه
 * قبل أن يُشرح له، ويعرف أنّ هذه الورقةَ تتكلّم عن آيةٍ واحدةٍ بعينها.
 *
 * وهي الجرأةُ الوحيدةُ في هذه الشاشة — وما عداها حدودٌ رفيعةٌ ومسافاتٌ
 * منضبطة.
 */
@Composable
private fun AyahRosette(n: Int, gold: Color, ink: Color) {
    val ar = LocalArabicNumerals.current
    Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(46.dp)) {
            val w = size.width
            val c = Offset(w / 2f, w / 2f)
            //  اثنتا عشرة ورقةً حول القرص — عددُ ما يُرسم في المصاحف
            //  المطبوعة، ونصفُ القطر يتناوب فتُقرأ وردةً لا تِرساً.
            for (i in 0 until 12) {
                val a = (i * 30f) * PI.toFloat() / 180f
                val r = if (i % 2 == 0) w * 0.47f else w * 0.41f
                drawCircle(
                    gold.copy(alpha = if (i % 2 == 0) 0.55f else 0.30f),
                    radius = w * 0.045f,
                    center = Offset(c.x + r * cos(a), c.y + r * sin(a)),
                )
            }
            drawCircle(gold.copy(alpha = 0.55f), w * 0.34f, c, style = Stroke(1.1f))
            drawCircle(gold.copy(alpha = 0.07f), w * 0.34f, c)
        }
        Text(
            n.localized(ar),
            style = RafiqType.label,
            color = ink.copy(alpha = 0.80f),
        )
    }
}

/**
 * فاصلٌ يحمل اسمَ قسمه — خطٌّ ينقطع عند الاسم ويُستأنف بعده.
 *
 * والاسمُ بالخطّ الكوفيّ: قاعدةُ التطبيق أنّ الكوفيَّ لما **ليس كلاماً**
 * — وسومُ الأقسام وأسماءُ الحقول — فلا يلتبس بصوتٍ يُقرأ.
 */
@Composable
private fun SectionRule(label: String, hair: Color, ink: Color) {
    Spacer(Modifier.height(18.dp))
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(label, style = RafiqType.meta, color = ink.copy(alpha = 0.55f))
        Box(Modifier.weight(1f).height(1.dp).background(hair))
    }
    Spacer(Modifier.height(11.dp))
}

/**
 * بطاقةُ التفسير — سطران بلا لمسة، وبقيّتُه بلمسة.
 *
 * ولا يُقتطع النصُّ عند الفتح: يُعرض كاملاً وتتمرّر الورقةُ كلُّها.
 * والاقتطاعُ في الحال المطويّة وحدَها، ومعه ما يقول إنّ وراءه بقيّة.
 */
@Composable
private fun TafsirCard(body: String, ink: Color, hair: Color, gold: Color) {
    var open by remember(body) { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp, 14.dp, 14.dp, 22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, hair, shape)
            .clickable { open = !open }
            .padding(horizontal = 13.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RafiqIcon(RIcon.Book, 16.dp, gold)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.ayah_tafsir_name),
                style = RafiqType.label,
                color = ink.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f),
            )
            /*  سهمٌ يدور لا سهمان يتبادلان.
             *
             *  `ChevronLeft/Right` تدلّ على **جهة** لا على فتحٍ وطيّ —
             *  وفي واجهةٍ من اليمين إلى اليسار تصير أكذبَ: اليسارُ فيها
             *  «إلى الأمام». فالسهمُ واحدٌ يُدار ربعَ دورة، وهي الحركةُ
             *  التي يعرفها كلُّ قارئٍ لمعنى «افتح».
             *
             *  و`tapSpec` ترجع `snap()` عند تقليل الحركة. */
            val turn by animateFloatAsState(
                if (open) 90f else -90f,
                tapSpec(),
                label = "tafsirTurn",
            )
            Box(Modifier.rotate(turn)) {
                RafiqIcon(RIcon.ChevronLeft, 15.dp, ink.copy(alpha = 0.40f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            fontFamily = NaskhFamily,
            fontSize = 14.sp,
            lineHeight = 27.sp,
            color = ink.copy(alpha = 0.82f),
            maxLines = if (open) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!open) {
            Spacer(Modifier.height(7.dp))
            Text(
                stringResource(R.string.ayah_tafsir_more),
                style = RafiqType.caption,
                color = gold,
            )
        }
    }
}
