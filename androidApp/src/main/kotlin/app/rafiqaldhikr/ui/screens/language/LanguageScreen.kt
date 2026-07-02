package app.rafiqaldhikr.ui.screens.language

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton

data class LanguageOption(
    val code: String,
    val nameNative: String,
    val nameArabic: String,
    val emoji: String
)

@Composable
fun LanguageScreen(navController: NavHostController) {
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf("ar") }

    val languages = listOf(
        LanguageOption("ar", "العربية", "العربية", "🇸🇦"),
        LanguageOption("en", "English", "الإنجليزية", "🇺🇸"),
        LanguageOption("fr", "Français", "الفرنسية", "🇫🇷"),
        LanguageOption("tr", "Türkçe", "التركية", "🇹🇷"),
        LanguageOption("ur", "اردو", "الأردو", "🇵🇰"),
        LanguageOption("id", "Bahasa Indonesia", "الإندونيسية", "🇮🇩"),
        LanguageOption("ms", "Bahasa Melayu", "الماليزية", "🇲🇾"),
        LanguageOption("bn", "বাংলা", "البنغالية", "🇧🇩"),
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "اللغة — Language",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    "اختر لغة التطبيق",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "سيتم تغيير لغة الواجهة فقط. النصوص القرآنية والأذكار تبقى بالعربية.",
                    fontSize = 13.sp,
                    color = rc.inkMed
                )
                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
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
                                Text(
                                    lang.nameNative,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rc.ink
                                )
                                Text(
                                    lang.nameArabic,
                                    fontSize = 13.sp,
                                    color = rc.inkMed
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "مُختار",
                                    tint = rc.emerald
                                )
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
