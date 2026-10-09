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
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized

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
                    /*  رقمُ الآية في وردةٍ لا في سطرِ نصّ.
                     *
                     *  «الآية ١٢» سطرُ بياناتٍ يصلح لأيّ تطبيق، والوردةُ
                     *  علامةُ آخرِ الآية في المصحف المطبوع — يراها القارئُ
                     *  على الورقة التي تحته الآن فيعرفها بلا شرح.
                     *
                     *  وبارتفاع السطر نفسِه: اثنتان وعشرون نقطةً داخل صفٍّ
                     *  علوُّه ثمانٍ وثلاثون — فلا يطول السطرُ نقطةً واحدة. */
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AyahRosette(22.dp, rc.gold)
                            Text(
                                ayah.localized(ar),
                                style = RafiqType.micro,
                                color = ink.copy(alpha = 0.72f),
                            )
                        }
                        Text(
                            SurahNames.of(ctx, surah),
                            fontFamily = NaskhFamily,
                            fontSize = 13.sp,
                            color = ink.copy(alpha = 0.55f),
                        )
                    }
                    StepDot(RIcon.ChevronLeft, next != null, ink) {
                        next?.let { onVerse("${it.surah}:${it.ayahNumber}") }
                    }
                }
                Spacer(Modifier.height(9.dp))

                /*  خطّان ذهبيّان رفيعان فوق الآية وتحتها.
                 *
                 *  نبرةٌ من جدول المصحف بسمك شعرة — تفصل كلامَ الله عمّا
                 *  تحته من كلام المفسِّر، بلا أن تأخذ من الارتفاع شيئاً
                 *  يُذكر. وبثمانيةٍ وعشرين بالمئة من الذهب: حدٌّ يُرى ولا
                 *  يُزاحم النصّ. */
                Text(
                    text.ifBlank { if (loading) "…" else "" },
                    fontFamily = QuranFamily,
                    fontSize = 25.sp,
                    lineHeight = 48.sp,
                    color = ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val c = rc.gold.copy(alpha = 0.28f)
                            val t = 1.dp.toPx()
                            drawRect(c, Offset(0f, 0f), Size(size.width, t))
                            drawRect(c, Offset(0f, size.height - t), Size(size.width, t))
                        }
                        .padding(vertical = 9.dp),
                )
                Spacer(Modifier.height(13.dp))

                val tf = tafsir
                if (!tf.isNullOrBlank()) {
                    /*  التفسيرُ منسوبٌ لصاحبه.
                     *
                     *  كان نصّاً رمادياً بلا اسمٍ تحت كلام الله مباشرة،
                     *  فلا يعرف القارئُ كلامَ من يقرأ — وهو نقصٌ قبل أن
                     *  يكون بصريّاً. والاسمُ بالكوفيّ: قاعدةُ التطبيق أنّ
                     *  الكوفيَّ لما ليس كلاماً يُقال. */
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Text(
                            stringResource(R.string.ayah_tafsir_name),
                            style = RafiqType.meta,
                            color = rc.gold,
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(rc.gold.copy(alpha = 0.20f)),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
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
                            stringResource(R.string.action_save), { c, _ -> IcCheck(17.dp, c) }, true,
                            ink, hair, Modifier.weight(1f),
                        ) {
                            scope.launch {
                                vm.setNote(surah, ayah, page, draft)
                                note = draft.trim().takeIf { it.isNotEmpty() }
                                editing = false
                            }
                        }
                        SheetAction(
                            stringResource(R.string.action_close), { c, _ -> IcClose(17.dp, c) }, false,
                            ink, hair, Modifier.weight(1f),
                        ) { draft = note.orEmpty(); editing = false }
                    }
                    Spacer(Modifier.height(13.dp))
                }

                /*  خمسةٌ بالاسم كاملاً — لا «نس» و«ملا» و«مش».
                 *
                 *  كانت الخمسةُ صفّاً أفقيّاً: رمزٌ ثمّ اسمٌ بجانبه. وخمسةُ
                 *  أعمدةٍ على عرض هاتفٍ تترك لكلّ اسمٍ نحوَ خمسٍ وعشرين
                 *  نقطة — فيُقَصّ «نسخ» إلى «نس» و«ملاحظة» إلى «ملا»
                 *  و«مشاركة» إلى «مش». و`maxLines = 1` بلا `overflow`
                 *  تقطع في منتصف الكلمة بلا نقاطٍ تدلّ على القطع، فيقرأ
                 *  حروفاً لا معنى لها ولا يعرف ما الزرّ.
                 *
                 *  فالرمزُ **فوق** الاسم: العرضُ كلُّه للاسم، والخمسةُ
                 *  تتساوى، ولا يُقَصّ حرف. */
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    SheetAction(
                        label = stringResource(
                            if (marked) R.string.ayah_bookmarked else R.string.ayah_mark_short,
                        ),
                        glyph = { c, g -> if (marked) IcCheck(17.dp, c) else IcMark(17.dp, c, g) },
                        filled = marked,
                        ink = ink,
                        hair = hair,
                        modifier = Modifier.weight(1f),
                    ) {
                        scope.launch { marked = vm.toggleMark(surah, ayah, page) }
                    }
                    SheetAction(
                        stringResource(R.string.action_copy), { c, g -> IcCopy(17.dp, c, g) }, false,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        clip.setText(AnnotatedString(shareBody(text, tf, surah, ayah, ctx)))
                    }
                    SheetAction(
                        stringResource(R.string.ayah_note), { c, g -> IcNote(17.dp, c, g) }, false,
                        ink, hair, Modifier.weight(1f),
                    ) { draft = note.orEmpty(); editing = true }
                    /*  «فاصل» ≠ «علامة».
                     *
                     *  العلامةُ تبقى، والفاصلُ موضعُ وقوفٍ **واحدٌ** يتبدّل
                     *  كلَّ يوم. وكانا زرّاً واحداً، فتمتلئ قائمةُ العلامات
                     *  بمواضعَ قديمةٍ لا معنى لها فتضيع المقصودةُ بينها. */
                    SheetAction(
                        stringResource(R.string.ayah_stop), { c, g -> IcStop(17.dp, c, g) }, false,
                        ink, hair, Modifier.weight(1f),
                    ) {
                        scope.launch {
                            vm.setStop(surah, ayah, page)
                            Toast.makeText(ctx, stopped, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                    SheetAction(
                        stringResource(R.string.action_share), { c, g -> IcSend(17.dp, c, g) }, false,
                        ink, hair, Modifier.weight(1f),
                    ) { sharing = true }
                }

                /*  التسميعُ في صفٍّ لنفسه لا سادساً في الصفّ.
                 *
                 *  فالخمسةُ أفعالٌ صغيرةٌ على الآية (علامةٌ · نسخٌ · حاشيةٌ
                 *  · وقوفٌ · مشاركة)، والتسميعُ **عملٌ يفتح شاشة**. وسادسٌ
                 *  في الصفّ يُضيّق الخمسةَ ويُخفي الفرق. */
                Spacer(Modifier.height(9.dp))
                /*  ذهبيٌّ لا رماديّ — وهو الفعلُ الوحيدُ في الورقة الذي
                 *  يُغلقها ويفتح شاشةً أخرى. فيُقرأ مختلفاً بلا أن يكبر. */
                val tasShape = RoundedCornerShape(13.dp, 13.dp, 13.dp, 22.dp)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(tasShape)
                        .background(rc.gold.copy(alpha = 0.10f))
                        .border(1.dp, rc.gold.copy(alpha = 0.32f), tasShape)
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
                        IcMic(18.dp, rc.gold, rc.gold)
                        Text(
                            stringResource(R.string.action_tasmee),
                            style = RafiqType.label,
                            color = rc.gold,
                        )
                    }
                }

                if (sharing) {
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetAction(
                            stringResource(R.string.share_as_text), { c, g -> IcCopy(17.dp, c, g) }, false,
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
                            stringResource(R.string.share_as_image), { c, g -> IcSend(17.dp, c, g) }, true,
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
    /** يرسم الرمز: (لونُ الحبر، لونُ الذهب) — انظر `AyahIcons.kt`. */
    glyph: @Composable (Color, Color) -> Unit,
    filled: Boolean,
    ink: Color,
    hair: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 18.dp)
    //  حدٌّ ذهبيٌّ خفيفٌ لا رماديّ: الصفُّ كلُّه أدواتُ مصحفٍ لا أزرارَ
    //  نظام، والذهبُ هو ما يقوله التطبيقُ في كلّ شاشةٍ أخرى.
    val edge = if (filled) rc.emeraldFill else rc.gold.copy(alpha = 0.26f)
    Column(
        modifier
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(if (filled) rc.emeraldFill else Color.Transparent)
            .border(1.dp, edge, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        glyph(
            if (filled) rc.onEmeraldFill else ink.copy(alpha = 0.80f),
            if (filled) rc.onEmeraldFill else rc.gold,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            label,
            style = RafiqType.micro,
            color = if (filled) rc.onEmeraldFill else ink.copy(alpha = 0.85f),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
