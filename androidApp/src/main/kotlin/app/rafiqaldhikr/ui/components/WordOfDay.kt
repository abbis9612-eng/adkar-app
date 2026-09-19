package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.rafiq.domain.model.Wisdom
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType

/* ══════════════════════════════════════════════════════════════
   كلمةُ اليوم — انتقلت من الرئيسية إلى «أوراقي»

   كانت في الشاشة الأولى، فاجتمع فيها **ثلاثةُ نصوص**: موعظةُ الطور،
   وفضلُ المحطّة، وهذه. وفي الباب نفسِه من صحيح البخاري: «كانَ النَّبيُّ
   ﷺ يَتَخَوَّلُنا بالمَوْعِظَةِ في الأيَّامِ **كَراهَةَ السَّآمَةِ
   عَلَيْنا**» (٦٨)، وبعده «يَسِّروا ولا تُعَسِّروا، وبَشِّروا ولا
   تُنَفِّروا» (٦٩) — وعنوانُ البابِ: «كي لا يَنفِروا».

   فبقي في الشاشة الأولى **نصٌّ واحد**، وانتقلت هذه إلى الورقة: من فتحها
   فقد طلب المزيد، ومن لم يفتحها لم يُثقَل بها.

   ولم تُحذف: حذفُ محتوًى موثَّقٍ لتقصير شاشةٍ خسارةٌ لا تبسيط.
══════════════════════════════════════════════════════════════ */

/**
 * الاقتباسُ ثمّ سندُه: خيطٌ أخضرُ رأسيٌّ فاسمُ القائل فكتابُه — وهي
 * علامةُ السند نفسُها التي تتكرّر حيثما ورد نصٌّ دينيٌّ في التطبيق.
 */
@Composable
fun WordOfDay(w: Wisdom, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Column(
        modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            w.text,
            //  أميريّ — وهو هنا في موضعه: نصٌّ منقولٌ عن عالِمٍ بمصدره.
            style = RafiqType.ayah,
            color = rc.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(rc.emeraldFill),
            )
            Spacer(Modifier.width(9.dp))
            Column {
                Text(w.author, style = RafiqType.titleM, color = rc.emerald)
                Text(w.source, style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
    }
}
