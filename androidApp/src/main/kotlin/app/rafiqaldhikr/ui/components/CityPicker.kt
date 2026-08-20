package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import app.rafiq.domain.model.City
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType

/* ═══════════════════════════════════════════════════════════════════
   اختيار المدينة يدوياً

   بحذف الاحتياطيّ الصامت صار من يرفض إذن الموقع — أو يكون على جهاز بلا
   GPS — بلا مواقيت ولا قبلة ولا ورقة. هذا الباب الثاني: يختار مدينته
   بنفسه، فيعرف على أي أساس حُسِبت أوقاته بدل أن يُخمَّن له.

   القائمة داخل التطبيق (assets/cities.json) فيعمل الاختيار بلا اتصال —
   وهو الفارق الجوهري عن الترميز العكسي الذي يحتاج شبكة.

   إحداثيات كل مدينة يفحصها tools/check_cities.py مقابل صندوق حدود بلدها
   في CI، لأن تبديلاً بين خط الطول والعرض لا يُنتج رقماً غريباً ظاهراً بل
   جدولَ صلاةٍ معقولاً تماماً لمدينةٍ في نصف الكرة الآخر.
═══════════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    onDismiss: () -> Unit,
    onPick: (City) -> Unit,
    vm: CityListViewModel = org.koin.androidx.compose.koinViewModel(),
) {
    val rc     = LocalRafiqColors.current
    val cities by vm.cities.collectAsState()
    var query  by remember { mutableStateOf("") }

    val shown = remember(cities, query) {
        val q = query.trim()
        if (q.isEmpty()) cities else cities.filter { it.matches(q) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = rc.card,
        shape            = RafiqShape.sheetTop,
        dragHandle       = { BottomSheetDefaults.DragHandle(color = rc.divider) },
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 12.dp)) {

            Text("اختر مدينتك", style = RafiqType.titleM, color = rc.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "تُحسب المواقيت من إحداثيات المدينة المختارة.",
                style = RafiqType.bodyS, color = rc.inkMed,
            )
            Spacer(Modifier.height(14.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RafiqShape.item)
                    .background(rc.chipBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (query.isEmpty()) {
                    Text("ابحث بالعربية أو بالإنجليزية…", style = RafiqType.body, color = rc.inkLight)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = RafiqType.body.copy(color = rc.ink),
                    cursorBrush = SolidColor(rc.emerald),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(10.dp))

            if (shown.isEmpty()) {
                Text(
                    "لا مدينة بهذا الاسم في القائمة. جرّب اسم أقرب مدينة كبيرة إليك، " +
                        "أو فعّل الموقع ليُحدَّد بدقّة.",
                    style = RafiqType.bodyS, color = rc.inkMed,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    items(shown, key = { it.ar }) { city ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RafiqShape.item)
                                .clickable { onPick(city) }
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(city.ar, style = RafiqType.body, color = rc.ink,
                                 modifier = Modifier.weight(1f))
                            Text(city.countryAr, style = RafiqType.caption, color = rc.inkMed)
                        }
                        HorizontalDivider(color = rc.divider.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
