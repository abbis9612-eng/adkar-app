package app.rafiqaldhikr.ui.screens.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun ContactScreen(navController: NavHostController) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
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
            // u2550u2550u2550 HEADER u2550u2550u2550
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RafiqBackButton(onClick = { navController.popBackStack() })

                Text(
                    text = "تواصل معنا",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    "نسعد بتواصلك! 💬",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(24.dp))

                // Email
                ContactItem(
                    icon = Icons.Default.Email,
                    title = "البريد الإلكتروني",
                    desc = "support@rafiqaldhikr.app",
                    rc = rc,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@rafiqaldhikr.app")
                            putExtra(Intent.EXTRA_SUBJECT, "رفيق الذكر — ملاحظة")
                        }
                        context.startActivity(intent)
                    }
                )

                ContactItem(
                    icon = Icons.Default.BugReport,
                    title = "الإبلاغ عن خطأ",
                    desc = "أخبرنا عن أي خطأ تقني",
                    rc = rc,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:bugs@rafiqaldhikr.app")
                            putExtra(Intent.EXTRA_SUBJECT, "رفيق الذكر — بلاغ خطأ")
                        }
                        context.startActivity(intent)
                    }
                )

                ContactItem(
                    icon = Icons.Default.Lightbulb,
                    title = "اقتراح ميزة",
                    desc = "شاركنا أفكارك لتطوير التطبيق",
                    rc = rc,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ideas@rafiqaldhikr.app")
                            putExtra(Intent.EXTRA_SUBJECT, "رفيق الذكر — اقتراح")
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("رسالتك", color = rc.inkMed) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = rc.emerald,
                        unfocusedBorderColor = rc.gold.copy(alpha = 0.3f),
                        cursorColor = rc.emerald,
                        focusedTextColor = rc.ink,
                        unfocusedTextColor = rc.ink
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@rafiqaldhikr.app")
                            putExtra(Intent.EXTRA_SUBJECT, "رفيق الذكر — رسالة من المستخدم")
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = message.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rc.emerald,
                        contentColor = rc.bg,
                        disabledContainerColor = rc.emerald.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("إرسال", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, desc: String, rc: RafiqPalette, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(rc.emerald.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = rc.emerald, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = rc.ink)
            Text(desc, fontSize = 14.sp, color = rc.inkMed)
        }
    }
}
