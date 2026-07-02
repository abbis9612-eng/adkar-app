package app.rafiqaldhikr.ui.screens.quran

import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import app.rafiqaldhikr.service.QuranAudioService
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import com.google.common.util.concurrent.MoreExecutors
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun QuranAudioPlayer(
    surahNumber: Int,
    navController: NavHostController
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentReciter by remember { mutableStateOf("عبد الباسط عبد الصمد") }
    var progress by remember { mutableFloatStateOf(0f) }
    val rc = LocalRafiqColors.current

    val reciters = listOf(
        "عبد الباسط عبد الصمد",
        "مشاري العفاسي",
        "ماهر المعيقلي",
        "سعود الشريم",
        "عبد الرحمن السديس"
    )

    // Build audio URL for the surah
    val surahStr = surahNumber.toString().padStart(3, '0')
    val audioUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahStr.mp3"

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
                    "مشغل القرآن",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier            = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Surah info
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("سورة", fontSize = 16.sp, color = rc.inkMed)
                        Text("$surahNumber", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(currentReciter, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = rc.ink)

                Spacer(Modifier.height(24.dp))

                // Progress
                Slider(
                    value         = progress,
                    onValueChange = { progress = it },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = SliderDefaults.colors(
                        thumbColor = rc.emerald,
                        activeTrackColor = rc.emerald,
                        inactiveTrackColor = rc.emeraldPastel
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0:00",  fontSize = 12.sp, color = rc.inkLight)
                    Text("--:--", fontSize = 12.sp, color = rc.inkLight)
                }

                Spacer(Modifier.height(24.dp))

                // Controls
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Previous
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { /* Previous */ },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(Modifier.size(24.dp)) {
                            val w = size.width
                            val h = size.height
                            drawPath(androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.8f, h * 0.2f)
                                lineTo(w * 0.3f, h * 0.5f)
                                lineTo(w * 0.8f, h * 0.8f)
                                close()
                            }, rc.inkMed)
                            drawLine(rc.inkMed, androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f), androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.8f), w * 0.15f)
                        }
                    }

                    // Play/Pause
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(rc.emeraldMed, rc.emerald)))
                            .clickable {
                                isPlaying = !isPlaying
                                if (isPlaying) {
                                    startAudio(context, audioUrl)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(Modifier.size(32.dp)) {
                            val w = size.width
                            val h = size.height
                            if (isPlaying) {
                                drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.2f), androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.8f), w * 0.2f, androidx.compose.ui.graphics.StrokeCap.Round)
                                drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.2f), androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.8f), w * 0.2f, androidx.compose.ui.graphics.StrokeCap.Round)
                            } else {
                                drawPath(androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.3f, h * 0.1f)
                                    lineTo(w * 0.8f, h * 0.5f)
                                    lineTo(w * 0.3f, h * 0.9f)
                                    close()
                                }, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.1f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                            }
                        }
                    }

                    // Next
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { /* Next */ },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(Modifier.size(24.dp)) {
                            val w = size.width
                            val h = size.height
                            drawPath(androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.2f, h * 0.2f)
                                lineTo(w * 0.7f, h * 0.5f)
                                lineTo(w * 0.2f, h * 0.8f)
                                close()
                            }, rc.inkMed)
                            drawLine(rc.inkMed, androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.2f), androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.8f), w * 0.15f)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Reciter picker
                Text("اختر القارئ", fontSize = 14.sp, color = rc.inkMed)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    reciters.take(3).forEach { name ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentReciter == name) rc.emeraldPastel else rc.card)
                                .border(1.dp, if (currentReciter == name) rc.emerald else rc.gold.copy(alpha=0.1f), RoundedCornerShape(8.dp))
                                .clickable { currentReciter = name }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, fontSize = 12.sp, color = if (currentReciter == name) rc.emerald else rc.inkMed, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

private fun startAudio(context: Context, url: String) {
    val sessionToken = SessionToken(context, ComponentName(context, QuranAudioService::class.java))
    val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
    controllerFuture.addListener({
        val controller = controllerFuture.get()
        controller.setMediaItem(MediaItem.fromUri(url))
        controller.prepare()
        controller.play()
    }, MoreExecutors.directExecutor())
}
