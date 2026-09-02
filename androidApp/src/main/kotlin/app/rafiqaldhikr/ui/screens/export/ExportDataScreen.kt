package app.rafiqaldhikr.ui.screens.export

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.IcoDownload
import app.rafiqaldhikr.ui.components.IcoTrash
import app.rafiqaldhikr.ui.components.IcoUpload
import app.rafiqaldhikr.ui.components.IcoWarning
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun ExportDataScreen(
    navController: NavHostController,
    viewModel: ExportDataViewModel = koinViewModel()
) {
    val context = LocalContext.current
    // تُحلّ هنا لا داخل onClick: stringResource دالّة @Composable
    // ولا تُستدعى من لامدا نقرٍ عادية.
    val shareSubject = stringResource(R.string.export_share_title)
    val exportFailed = stringResource(R.string.export_failed)
    val shareChooser = stringResource(R.string.export_action)
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                title  = stringResource(R.string.export_title),
                onBack = {navController.popBackStack()},
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.export_headline),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.export_body),
                    color = rc.inkMed, style = RafiqType.bodyS)

                Spacer(Modifier.height(24.dp))

                // Export
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IcoUpload(22.dp, rc.emerald)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.export_action), fontWeight = FontWeight.SemiBold, color = rc.ink, style = RafiqType.body)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.export_desc),
                        fontSize = 13.sp,
                        color = rc.inkMed
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RafiqShape.item)
                            .background(rc.emerald.copy(alpha = 0.1f))
                            .clickable {
                                /*  ملفٌّ عبر FileProvider لا نصٌّ في Intent.
                                 *
                                 *  كان التصديرُ كلُّه يُمرَّر في
                                 *  `EXTRA_TEXT` — أي داخل معاملة Binder
                                 *  سقفُها نحو ميغابايت. وبيانات سنةٍ من
                                 *  الاستعمال تتجاوزه، فينهار التطبيق عند
                                 *  من طال استعمالُه: أوفى المستخدمين
                                 *  بالضبط.  */
                                viewModel.exportJson(
                                    onReady = { json ->
                                        val uri = runCatching {
                                            writeExportFile(context, json)
                                        }.getOrNull()
                                        if (uri == null) {
                                            Toast.makeText(context, exportFailed, Toast.LENGTH_LONG).show()
                                            return@exportJson
                                        }
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, shareChooser))
                                    },
                                    // الفشلُ كان صامتاً تماماً: `onSuccess` وحدَه
                                    // بلا `onFailure`، فيضغط المستخدم ولا يقع شيء.
                                    onError = {
                                        Toast.makeText(context, exportFailed, Toast.LENGTH_LONG).show()
                                    },
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IcoDownload(22.dp, rc.emerald)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.export_action), color = rc.emerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Delete
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RafiqShape.card)
                        .background(rc.card)
                        .border(1.dp, rc.error.copy(alpha = 0.3f), RafiqShape.card)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IcoTrash(22.dp, rc.error)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.delete_all), fontWeight = FontWeight.SemiBold, color = rc.ink, style = RafiqType.body)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.delete_warning),
                        fontSize = 13.sp,
                        color = rc.error.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RafiqShape.item)
                            .background(rc.error)
                            .clickable { showDeleteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IcoWarning(22.dp, Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_all_short), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title), color = rc.ink, fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.delete_confirm_body), color = rc.inkMed) },
            containerColor = rc.card,
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAllData {
                            // البيانات صُفّرت — نعود لشاشة البداية من جديد
                            navController.navigate(RafiqRoute.Onboarding.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = rc.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel), color = rc.emerald) }
            }
        )
    }
}

/**
 * يكتب التصدير إلى ملفٍّ في `cacheDir/exports` ويُرجع رابطَه المشترَك.
 *
 * والمجلَّدُ يُنظَّف قبل كل كتابة: نسخُ التصدير القديمة لا يحتاجها أحد،
 * وهي تحمل كلَّ سجلّ المستخدم فلا تُترك في ذاكرةٍ مؤقّتة بلا داعٍ.
 */
private fun writeExportFile(context: android.content.Context, json: String): android.net.Uri {
    val dir = File(context.cacheDir, "exports").apply {
        deleteRecursively()
        mkdirs()
    }
    val stamp = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        .format(java.util.Date())
    val file = File(dir, "rafiq-$stamp.json")
    file.writeText(json)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
