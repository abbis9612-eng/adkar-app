package app.rafiqaldhikr.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.rafiqaldhikr.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar

@Composable
fun AboutScreen(navController: NavHostController) {
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
            RafiqTopBar(
                title  = stringResource(R.string.about),
                onBack = {navController.popBackStack()},
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RafiqIcon(RIcon.Moon, 64.dp, rc.emerald)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.app_name), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Text("الإصدار " + app.rafiqaldhikr.BuildConfig.VERSION_NAME,
                    color = rc.inkMed, style = RafiqType.bodyS)
                Spacer(Modifier.height(24.dp))
                Text("رفيقك اليومي في رحلة الإيمان.\nأذكار، قرآن، أدعية، مسبحة، ومواقيت الصلاة\nكل ذلك في تطبيق واحد.",
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    color = rc.inkMed, style = RafiqType.body)
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.about_made_with), color = rc.emerald, style = RafiqType.bodyS)

                Spacer(Modifier.height(36.dp))

                /*  العزوُ الذي لم يكن.
                 *
                 *  خطوطُ المصحف مِلكُ مجمَّع الملك فهد، ورخصتُه تمنح
                 *  الاستعمالَ والنسخَ والتوزيعَ مجاناً **بشرط أن ترافقَ
                 *  الرخصةُ الخطَّ**: "any person obtaining a copy of this
                 *  Font accompanying this license". والتطبيقُ كان
                 *  يُنزّل تسعين ميغابايت من خطوطهم ولا يذكرهم في موضعٍ
                 *  واحدٍ يراه المستخدم.
                 *
                 *  والنصُّ الكاملُ في `tools/licenses/`. */
                Text(
                    stringResource(R.string.about_credits),
                    style = RafiqType.caption,
                    color = rc.inkLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
