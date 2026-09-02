package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import androidx.compose.ui.unit.dp

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    /*  اللوحةُ لوحةُ التطبيق لا لوحةُ Material.
     *
     *  كانت الحالاتُ الثلاث (خطأ، فراغ، تحميل) تُلوَّن من
     *  `MaterialTheme.colorScheme` بينما ٩٥٪ من الواجهة تُلوَّن من
     *  `LocalRafiqColors`. فمع «الألوان الديناميكية» تظهر هذه الشاشاتُ
     *  الثلاثُ وحدَها بألوان خلفية الجهاز، غريبةً عن التطبيق.  */
    val rc = LocalRafiqColors.current
    Column(
        modifier              = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        IcoAlert(64.dp, rc.error.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = message,
            style     = RafiqType.body,
            color     = rc.ink,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = rc.emerald,
                    contentColor   = rc.onEmerald,
                ),
            ) {
                // كان مكتوباً في الكود، فيُقرأ عربياً في الواجهة الإنجليزية.
                Text(stringResource(R.string.retry))
            }
        }
    }
}
