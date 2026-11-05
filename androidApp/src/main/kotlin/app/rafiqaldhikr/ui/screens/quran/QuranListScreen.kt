package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.rafiqaldhikr.ui.components.EmptyState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.components.RafiqIconButton

/* Colors are now provided by LocalRafiqColors from RafiqPalette.kt */

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION (reused from HomeScreen pattern)
══════════════════════════════════════════════════════════════ */


/* ══════════════════════════════════════════════════════════════
   PILL BUTTON
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   DAILY RECITATION CARD (Hero)
══════════════════════════════════════════════════════════════ */


/* ══════════════════════════════════════════════════════════════
   SEARCH BAR
══════════════════════════════════════════════════════════════ */

@Composable
private fun QuranSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val rc = LocalRafiqColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        placeholder = {
            Text(stringResource(R.string.quran_search_hint), color = LocalRafiqColors.current.inkMed, style = RafiqType.bodyS)
        },
        leadingIcon = {
            RafiqIcon(RIcon.Search, 17.dp, rc.inkMed)
        },
        singleLine = true,
        shape = RafiqShape.card,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LocalRafiqColors.current.gold.copy(alpha = 0.5f),
            unfocusedBorderColor = LocalRafiqColors.current.gold.copy(alpha = 0.12f),
            focusedContainerColor = LocalRafiqColors.current.card,
            unfocusedContainerColor = LocalRafiqColors.current.card,
            focusedTextColor = LocalRafiqColors.current.ink,
            unfocusedTextColor = LocalRafiqColors.current.ink,
            cursorColor = LocalRafiqColors.current.emerald,
        )
    )
}

/* ══════════════════════════════════════════════════════════════
   SURAH ROW CARD
══════════════════════════════════════════════════════════════ */

/**
 * صفُّ سورة — سطرٌ بخطّ لا بطاقة.
 *
 * البطاقة تقول «هذه مجموعة». وحول ١١٤ سورة لا معنى للجملة: المصحف
 * كلُّه مجموعةٌ واحدة، فتصير البطاقة إطاراً حول كل شيء — أي حول لا
 * شيء. وفوق ذلك كانت ١١٤ حافّةً وظلّاً تُرسم في قائمةٍ تُمرَّر.
 *
 * والقاعدة في التطبيق بعد اليوم: قائمةٌ طويلة متجانسة ← خطوط،
 * ومجموعةٌ قصيرة مختلفة ← بطاقة. (الإعدادات مثال الثانية.)
 */
@Composable
private fun SurahRow(
    number: Int,
    nameAr: String,
    nameEn: String,
    ayahCount: Int,
    pageStart: Int,
    revelation: String,
    last: Boolean,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.padding(vertical = 12.dp).fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // رقمٌ في حلقةٍ رفيعة: كان مربّعاً مملوءاً داخل بطاقة — طبقتان
            // فوق بعضهما. وبلا بطاقةٍ يكفي الحدّ.
            Box(
                Modifier.size(44.dp).clip(RafiqShape.item)
                    .border(1.dp, rc.cardBorder, RafiqShape.item),
                contentAlignment = Alignment.Center,
            ) {
                Text("$number".localizedDigits(LocalArabicNumerals.current), fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald, style = RafiqType.bodyS)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(nameAr, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.ink)
                Spacer(Modifier.height(2.dp))
                /*  رقمُ الصفحة من `page_start` لا من صيغةٍ مخترعة.
                    كانت `number * 5 + 1` فتُعرض الكهفُ «صفحة ٩١» وموضعُها
                    ٢٩٣. والبيانةُ الصحيحة كانت حاضرةً في السجلّ نفسِه،
                    مطابقةً لتخطيط المصحف في ١١٤ سورةً من ١١٤. */
                Text(
                    "$ayahCount آية · صفحة $pageStart".localizedDigits(LocalArabicNumerals.current),
                    color = LocalRafiqColors.current.inkMed,
                    style = RafiqType.caption,
                )
            }
            Box(
                Modifier.clip(RafiqShape.item)
                    .background(if (revelation == "meccan") rc.meccanBg else rc.madaniBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(if (revelation == "meccan") stringResource(R.string.revelation_makki) else stringResource(R.string.revelation_madani),
                    fontWeight = FontWeight.SemiBold,
                    color = if (revelation == "meccan") rc.meccanText else rc.madaniText, style = RafiqType.micro)
            }
            Spacer(Modifier.width(8.dp))
            RafiqIcon(RIcon.ChevronLeft, 14.dp, rc.inkLight)
        }
        if (!last) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN QURAN LIST SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun QuranListScreen(
    navController: NavHostController,
    viewModel: QuranListViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ TOP BAR ═══
            RafiqTopBar(title = stringResource(R.string.quran_title)) {
                // بابُ المصحف صفحةً صفحة — والقائمةُ تبقى للبحث والانتقال بالسورة
                RafiqIconButton(onClick = { navController.navigate(RafiqRoute.Mushaf.tab) }, label = "المصحف") {
                    RafiqIcon(RIcon.Book, 17.dp, rc.emerald)
                }
                RafiqIconButton(onClick = { navController.navigate(RafiqRoute.QuranBookmarks.route) }, label = stringResource(R.string.quran_bookmarks)) {
                    RafiqIcon(RIcon.Bookmark, 17.dp, rc.emerald)
                }
                RafiqIconButton(onClick = { navController.navigate(RafiqRoute.QuranSearch.route) }, label = stringResource(R.string.quran_search_title)) {
                    RafiqIcon(RIcon.Search, 17.dp, rc.emerald)
                }
            }

            when {
                state.isLoading -> LoadingState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // ═══ DAILY RECITATION CARD ═══
                    item {
                        // حُذفت بطاقة «تلاوة اليوم»: كانت مشغّلاً وهمياً
                        // بالكامل — «سورة الكهف» ثابتة دائماً، وشريط تقدّم
                        // مثبَّت على ٣٥٪، وأزرار تشغيل بـ clickable { } فارغة
                        // لا تفعل شيئاً. المستخدم يضغط فلا يحدث شيء.
                        Spacer(Modifier.height(18.dp))
                    }

                    // ═══ SEARCH BAR ═══
                    item {
                        QuranSearchBar(
                            query = state.query,
                            onQueryChange = { viewModel.search(it) }
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // ═══ SECTION HEADER ═══
                    item {
                        Row(
                            Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                Modifier.width(4.dp).height(18.dp)
                                    .clip(RafiqShape.chip)
                                    .background(rc.gold)
                            )
                            Text(stringResource(R.string.quran_surahs), fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark, style = RafiqType.titleM)
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    /*  بحثٌ لا يجد شيئاً كان يترك فراغاً أبيض تحت مربّع
                     *  البحث — بلا سطرٍ يقول إنّ الاسم ليس في القائمة.  */
                    if (state.filtered.isEmpty() && !state.isLoading) {
                        item {
                            EmptyState(
                                message = stringResource(R.string.quran_list_no_match),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            )
                        }
                    }

                    // ═══ SURAH LIST ═══
                    itemsIndexed(state.filtered) { i, surah ->
                        Box(Modifier.padding(horizontal = 18.dp)) {
                            SurahRow(
                                last = i == state.filtered.lastIndex,
                                number = surah.number,
                                nameAr = surah.nameAr,
                                nameEn = surah.nameEn,
                                ayahCount = surah.ayahCount,
                                pageStart = surah.pageStart,
                                revelation = surah.revelation,
                                onClick = {
                                    navController.navigate(RafiqRoute.Mushaf.atPage(surah.pageStart))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
