package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.components.IcoCopy
import app.rafiqaldhikr.ui.components.IcoShare
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.theme.RafiqType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirSheet(
    surahNumber: Int,
    ayahNumber:  Int,
    ayahText:    String,
    tafsirText:  String,
    onDismiss:   () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rc = LocalRafiqColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    // تُحلّ قبل الـlambda — stringResource دالّة @Composable
    val shareChooser = stringResource(R.string.tafsir_share)
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RafiqShape.sheetTop,
        containerColor   = rc.bg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = rc.inkLight) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.tafsir_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
                Text("سورة $surahNumber : $ayahNumber",
                    color = rc.inkMed, style = RafiqType.bodyS)
            }

            Spacer(Modifier.height(16.dp))

            // Ayah text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rafiqCard()
                    .padding(16.dp)
            ) {
                Text(
                    ayahText,
                    modifier  = Modifier.fillMaxWidth(),
                    // 1.64 كان ضيّقاً على التشكيل العثماني — النسبة القرآنية 1.92
                    fontFamily = app.rafiqaldhikr.ui.theme.QuranFamily,
                    fontSize = 22.sp,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center,
                    color = rc.ink
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.tafsir_muyassar),
                fontWeight = FontWeight.Bold,
                color = rc.ink, style = RafiqType.body)
            Spacer(Modifier.height(12.dp))

            Text(tafsirText,
                fontFamily = app.rafiqaldhikr.ui.theme.NaskhFamily,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start,
                color = rc.inkMed, style = RafiqType.body)

            Spacer(Modifier.height(32.dp))

            // Actions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionBtn(
                    text = stringResource(R.string.action_copy),
                    icon = { s, c -> IcoCopy(s, c) },
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString("$ayahText\n\nالتفسير الميسر:\n$tafsirText"))
                    }
                )
                ActionBtn(
                    text = stringResource(R.string.action_share),
                    icon = { s, c -> IcoShare(s, c) },
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "$ayahText\n\nالتفسير الميسر:\n$tafsirText\n\nعبر تطبيق رفيق الذكر 🌙")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, shareChooser))
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionBtn(text: String, icon: @Composable (androidx.compose.ui.unit.Dp, androidx.compose.ui.graphics.Color) -> Unit, onClick: () -> Unit) {
    val rc = LocalRafiqColors.current
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = rc.emerald),
        border = androidx.compose.foundation.BorderStroke(1.dp, rc.emerald.copy(alpha = 0.5f))
    ) {
        icon(18.dp, rc.emerald)
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

