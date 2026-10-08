package app.rafiqaldhikr.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType

/*
 * تلميحُ أوّلِ مرّة
 * ════════════════
 *
 * ═══ ولماذا ليست جولةً من عشر بطاقات ═══
 *
 * النمطُ الشائع: عشرُ بطاقاتٍ تُعرض **قبل أن يلمس المستخدمُ شيئاً**،
 * كلُّ بطاقةٍ تُعتم الشاشةَ وتشرح زرّاً. وفيه ثلاثةُ عيوب:
 *
 *   ١. تُشرح أدواتٌ لم يحتجها بعد، فتُنسى قبل أن يحتاجها.
 *   ٢. تقف بينه وبين أوّل شيءٍ جاء من أجله.
 *   ٣. **وشاشةٌ تحتاج عشرَ بطاقاتٍ لتُفهم، شاشةٌ فاشلة** — والبطاقاتُ
 *      تُخفي العيبَ ولا تُصلحه.
 *
 * فالتلميحُ هنا **شريطٌ واحدٌ في موضعه**: يظهر أوّلَ ما تُفتح الشاشةُ
 * التي يخصّها، ويقول جملةً واحدةً عمّا يُفعل فيها، ويختفي بلمسةٍ ولا
 * يعود. تعليمٌ في موضعه لا محاضرةٌ في أوّله.
 *
 * ونمطُه مأخوذٌ من `HintBar` في المصحف — كان هناك وحدَه، فعُمّم.
 */

/** هل عُرض تلميحُ هذه الشاشة من قبل؟ */
private fun seen(ctx: Context, key: String): Boolean =
    ctx.getSharedPreferences(HINT_STORE, Context.MODE_PRIVATE).getBoolean(key, false)

private fun markSeen(ctx: Context, key: String) {
    ctx.getSharedPreferences(HINT_STORE, Context.MODE_PRIVATE)
        .edit().putBoolean(key, true).apply()
}

/** مخزنُ التلميحات — تشاركه الجولةُ في [SpotlightTour]. */
internal const val HINT_STORE = "rafiq_hints"

/**
 * يمحو كلَّ علامات «رُئي».
 *
 * فتعود التلميحاتُ السبعةُ والجولةُ كما كانت أوّلَ تثبيت. ولا يلمس هذا
 * بياناً واحداً من بيانات صاحبه: المخزنُ علاماتُ عرضٍ لا محتوى.
 */
fun clearHints(ctx: Context) {
    ctx.getSharedPreferences(HINT_STORE, Context.MODE_PRIVATE)
        .edit().clear().apply()
}

/**
 * شريطُ تلميحٍ يظهر مرّةً واحدةً لكلّ [key].
 *
 * @param key مفتاحٌ ثابتٌ للشاشة — تبديلُه يُعيد عرضَ التلميح
 * @param text نصُّ التلميح من الموارد
 */
@Composable
fun FirstHint(key: String, @StringRes text: Int, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    val ctx = LocalContext.current
    var shown by remember(key) { mutableStateOf(!seen(ctx, key)) }

    AnimatedVisibility(
        visible = shown,
        exit = fadeOut() + shrinkVertically(),
        enter = fadeIn(),
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
                .background(rc.tintGold)
                .clickable { shown = false; markSeen(ctx, key) }
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(text),
                style = RafiqType.caption,
                //  `ink` لا `inkMed`: الشريطُ ذهبيٌّ فاتح، والخافتُ عليه
                //  ينزل تحت حدّ المقروئيّة.
                color = rc.ink.copy(alpha = 0.82f),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.action_got_it),
                style = RafiqType.label,
                color = rc.emerald,
            )
        }
    }
}
