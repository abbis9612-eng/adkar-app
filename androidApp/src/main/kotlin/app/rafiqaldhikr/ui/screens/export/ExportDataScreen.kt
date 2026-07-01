package app.rafiqaldhikr.ui.screens.export

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

@Composable
fun ExportDataScreen(navController: NavHostController) {
    val context = LocalContext.current
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "إدارة البيانات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                // Back Button
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width
                        val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f)
                            lineTo(w * 0.70f, h * 0.50f)
                            lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    "بياناتك تحت سيطرتك 🔐",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "جميع بياناتك مخزنة محلياً على جهازك. يمكنك تصديرها أو حذفها في أي وقت.",
                    fontSize = 14.sp,
                    color = rc.inkMed
                )

                Spacer(Modifier.height(24.dp))

                // Export
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = rc.emerald)
                        Spacer(Modifier.width(12.dp))
                        Text("تصدير البيانات", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = rc.ink)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "تصدير جميع بيانات التقدم والعلامات والتفضيلات كملف JSON",
                        fontSize = 13.sp,
                        color = rc.inkMed
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(rc.emerald.copy(alpha = 0.1f))
                            .clickable {
                                val shareText = "// بيانات رفيق الذكر — تصدير\n// سيتم اضافة التصدير الكامل في تحديث قادم"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "تصدير البيانات"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = rc.emerald)
                            Spacer(Modifier.width(8.dp))
                            Text("تصدير البيانات", color = rc.emerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Delete
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.error.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = rc.error)
                        Spacer(Modifier.width(12.dp))
                        Text("حذف جميع البيانات", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = rc.ink)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠️ هذا الإجراء لا يمكن التراجع عنه! سيتم حذف جميع بيانات التقدم والتفضيلات.",
                        fontSize = 13.sp,
                        color = rc.error.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(rc.error)
                            .clickable { showDeleteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("حذف كل شيء", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("تأكيد الحذف", color = rc.ink, fontWeight = FontWeight.Bold) },
            text  = { Text("هل أنت متأكد من حذف جميع بياناتك؟ لا يمكن استعادتها بعد الحذف.", color = rc.inkMed) },
            containerColor = rc.card,
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = rc.error)
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء", color = rc.emerald) }
            }
        )
    }
}
