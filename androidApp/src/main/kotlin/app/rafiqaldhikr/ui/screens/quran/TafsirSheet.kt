package app.rafiqaldhikr.ui.screens.quran

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

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
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                    "تفسير الآية",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
                Text(
                    "سورة $surahNumber : $ayahNumber",
                    fontSize = 14.sp,
                    color = rc.inkMed
                )
            }

            Spacer(Modifier.height(16.dp))

            // Ayah text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(rc.card)
                    .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    ayahText,
                    modifier  = Modifier.fillMaxWidth(),
                    fontFamily = app.rafiqaldhikr.ui.theme.QuranFamily,
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center,
                    color = rc.ink
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "التفسير الميسر",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = rc.ink
            )
            Spacer(Modifier.height(12.dp))

            Text(
                tafsirText,
                fontSize = 16.sp,
                fontFamily = app.rafiqaldhikr.ui.theme.NaskhFamily,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start,
                color = rc.inkMed
            )

            Spacer(Modifier.height(32.dp))

            // Actions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionBtn(
                    text = "نسخ",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString("$ayahText\n\nالتفسير الميسر:\n$tafsirText"))
                    }
                )
                ActionBtn(
                    text = "مشاركة",
                    icon = Icons.Default.Share,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "$ayahText\n\nالتفسير الميسر:\n$tafsirText\n\nعبر تطبيق رفيق الذكر 🌙")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "مشاركة التفسير"))
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val rc = LocalRafiqColors.current
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = rc.emerald),
        border = androidx.compose.foundation.BorderStroke(1.dp, rc.emerald.copy(alpha = 0.5f))
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

