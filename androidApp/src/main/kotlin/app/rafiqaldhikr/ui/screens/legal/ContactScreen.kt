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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.navigation.NavHostController
import app.rafiqaldhikr.util.sendMail
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.components.IcoBulb
import app.rafiqaldhikr.ui.components.IcoMail
import app.rafiqaldhikr.ui.components.IcoSend
import app.rafiqaldhikr.ui.components.IcoWarning
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard

@Composable
fun ContactScreen(navController: NavHostController) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    val rc = LocalRafiqColors.current
    // تُقرأ في التأليف: `stringResource` لا تُنادى داخل onClick.
    val subjNote = stringResource(R.string.contact_subj_note)
    val subjBug  = stringResource(R.string.contact_subj_bug)
    val subjIdea = stringResource(R.string.contact_subj_idea)
    val subjUser = stringResource(R.string.contact_subj_user)

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
                title  = stringResource(R.string.settings_contact),
                onBack = {navController.popBackStack()},
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.contact_welcome),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(24.dp))

                // Email
                ContactItem(
                    icon = { s, c -> IcoMail(s, c) },
                    title = stringResource(R.string.contact_email),
                    desc = "support@rafiqaldhikr.app",
                    rc = rc,
                    onClick = {
                        context.sendMail("support@rafiqaldhikr.app", subjNote)
                    }
                )

                ContactItem(
                    icon = { s, c -> IcoWarning(s, c) },
                    title = stringResource(R.string.contact_bug),
                    desc = stringResource(R.string.contact_bug_desc),
                    rc = rc,
                    onClick = {
                        context.sendMail("bugs@rafiqaldhikr.app", subjBug)
                    }
                )

                ContactItem(
                    icon = { s, c -> IcoBulb(s, c) },
                    title = stringResource(R.string.contact_idea),
                    desc = stringResource(R.string.contact_idea_desc),
                    rc = rc,
                    onClick = {
                        context.sendMail("ideas@rafiqaldhikr.app", subjIdea)
                    }
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.contact_your_message), color = rc.inkMed) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RafiqShape.card,
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
                        context.sendMail("support@rafiqaldhikr.app", subjUser, message)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = message.isNotBlank(),
                    shape = RafiqShape.card,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rc.emeraldFill,
                        contentColor = rc.onEmeraldFill,
                        disabledContainerColor = rc.emeraldFill.copy(alpha = 0.5f)
                    )
                ) {
                    IcoSend(20.dp, rc.bg)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_send), fontWeight = FontWeight.Bold, style = RafiqType.body)
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    icon: @Composable (androidx.compose.ui.unit.Dp, androidx.compose.ui.graphics.Color) -> Unit,
    title: String, desc: String, rc: RafiqPalette, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .rafiqCard()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RafiqShape.item)
                .background(rc.emerald.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            icon(24.dp, rc.emerald)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = rc.ink, style = RafiqType.body)
            Text(desc, color = rc.inkMed, style = RafiqType.bodyS)
        }
    }
}
