package app.rafiqaldhikr.ui.screens.language

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.IcoCheck
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

data class LanguageOption(
    val code: String,
    val nameNative: String,
    val nameArabic: String,
    val emoji: String
)

@Composable
fun LanguageScreen(navController: NavHostController) {
    val context = LocalContext.current
    /*  اللغةُ الحاضرة تُقرأ لا تُفترض.
     *
     *  كانت `mutableStateOf("ar")` ثابتةً — فمن اختار الإنجليزية ثم عاد
     *  إلى الشاشة وجد العلامةَ على «العربية»، فيظنّ اختيارَه لم يُحفظ.  */
    var selectedLang by remember {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales()
                .takeIf { !it.isEmpty }?.get(0)?.language ?: "ar"
        )
    }

    // لغتان فقط: ما تُرجم فعلاً. الستّ الأخرى (fr · tr · ur · id · ms · bn)
    // كانت معروضة بلا ملف موارد واحد — يختارها المستخدم فلا يتغيّر شيء.
    val languages = listOf(
        LanguageOption("ar", "العربية", stringResource(R.string.lang_arabic),  "🇸🇦"),
        LanguageOption("en", "English",  stringResource(R.string.lang_english), "🇺🇸"),
    )

    val rc = LocalRafiqColors.current

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
            // ═══ HEADER ═══
            RafiqTopBar(
                title  = stringResource(R.string.lang_screen_title),
                onBack = {navController.popBackStack()},
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(stringResource(R.string.lang_choose),
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink, style = RafiqType.titleM)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.lang_note),
                    fontSize = 13.sp,
                    color = rc.inkMed
                )
                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                ) {
                    languages.forEachIndexed { index, lang ->
                        val isSelected = selectedLang == lang.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) rc.emerald.copy(alpha = 0.05f) else rc.card)
                                .clickable {
                                    selectedLang = lang.code
                                    applyLocale(lang.code)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lang.nameNative,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rc.ink, style = RafiqType.body)
                                Text(
                                    lang.nameArabic,
                                    fontSize = 13.sp,
                                    color = rc.inkMed
                                )
                            }
                            if (isSelected) {
                                IcoCheck(20.dp, rc.emerald)
                            }
                        }
                        if (index < languages.lastIndex) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun applyLocale(code: String) {
    val localeList = LocaleListCompat.forLanguageTags(code)
    AppCompatDelegate.setApplicationLocales(localeList)
}
