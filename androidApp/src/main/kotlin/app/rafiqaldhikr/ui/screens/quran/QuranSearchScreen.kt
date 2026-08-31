package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.usecase.SearchQuranUseCase
import app.rafiqaldhikr.ui.components.EmptyState
import app.rafiqaldhikr.ui.components.IcoSearch
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.compose.koinInject
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun QuranSearchScreen(navController: NavHostController) {
    val searchUseCase = koinInject<SearchQuranUseCase>()
    var query by remember { mutableStateOf("") }
    val rc = LocalRafiqColors.current

    val results by remember(query) {
        searchUseCase(query)
    }.collectAsStateWithLifecycle(emptyList())

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
            // u2550u2550u2550 HEADER u2550u2550u2550
            RafiqTopBar(
                title  = stringResource(R.string.quran_search_screen),
                onBack = {navController.popBackStack()},
            )

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text(stringResource(R.string.quran_search_field), color = rc.inkMed) },
                    singleLine    = true,
                    leadingIcon   = { IcoSearch(22.dp, rc.emerald) },
                    shape         = RafiqShape.card,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = rc.emerald,
                        unfocusedBorderColor = rc.gold.copy(alpha = 0.2f),
                        focusedTextColor = rc.ink,
                        unfocusedTextColor = rc.ink
                    )
                )
                Spacer(Modifier.height(12.dp))

                when {
                    query.length < 2 -> {
                        EmptyState(
                            message = stringResource(R.string.quran_search_min),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    results.isEmpty() -> {
                        EmptyState(
                            message = "لا توجد نتائج لـ \"$query\"",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Text("${results.size} نتيجة",
                            color = rc.inkMed, style = RafiqType.bodyS)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(results) { ayah ->
                                SearchResultCard(
                                    ayah    = ayah,
                                    onClick = {
                                        navController.navigate(RafiqRoute.Mushaf.atPage(ayah.page))
                                    },
                                    rc = rc
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    ayah: AyahInfo,
    onClick: () -> Unit,
    rc: RafiqPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rafiqCard()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("سورة ${ayah.surah} — آية ${ayah.ayahNumber}",
                fontWeight = FontWeight.Bold,
                color = rc.emerald, style = RafiqType.bodyS)
            Text("ص ${ayah.page}",
                color = rc.inkMed, style = RafiqType.caption)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            ayah.textUthmani,
            fontSize = 20.sp,
            fontFamily = app.rafiqaldhikr.ui.theme.QuranFamily,
            lineHeight = 40.sp,
            color = rc.ink,
            maxLines  = 3,
            overflow  = TextOverflow.Ellipsis
        )
    }
}
