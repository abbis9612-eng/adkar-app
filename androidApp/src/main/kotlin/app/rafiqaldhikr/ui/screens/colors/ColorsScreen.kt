package app.rafiqaldhikr.ui.screens.colors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.*

/* ═══════════════════════════════════════════════════════════════════
   شاشة الألوان

   المستخدم يختار اثنين: الورق واللهجة. وكلُّ ما عداهما يُشتقّ في
   [tuned] — الحبر واتجاهه، والثانوي، والفاصل، والبطاقة، ونصّ اللهجة،
   ولون المحتوى فوقها.

   ولماذا لا نتركه يختار كلَّ لونٍ على حدة؟ لأن ذلك يعني السماح بكسر
   القراءة. حرّيةٌ تنتهي بتطبيقٍ لا يُقرأ ليست حرّية — هي فخّ. فالحرّية
   هنا في اللونين اللذين يحدّدان الشخصية، والقراءة مضمونة بالحساب.

   والقياس معروضٌ على الشاشة: يرى المستخدم الأرقام بنفسه فيثق أن
   اختياره سليم، ولا يُطلب منه أن يثق بلا دليل.
═══════════════════════════════════════════════════════════════════ */

@Composable
fun ColorsScreen(navController: NavHostController) {
    val prefs = rememberColorPrefs()
    var paper  by remember { mutableStateOf(prefs.paper()) }
    var accent by remember { mutableStateOf(prefs.accent()) }
    var tick   by remember { mutableIntStateOf(0) }

    fun save(p: Color?, a: Color?) {
        paper = p; accent = a; prefs.set(p, a); tick++
    }

    // اللوحة المعروضة تُشتقّ من الاختيار الحاليّ فوراً — والمعاينة حقيقية
    val base = LocalRafiqColors.current
    val rc = remember(paper, accent, tick) { base.tuned(paper, accent) }

    CompositionLocalProvider(LocalRafiqColors provides rc, LocalColorTick provides tick) {
        Column(
            Modifier
                .fillMaxSize()
                .background(rc.bg)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            RafiqTopBar(
                title    = "الألوان",
                subtitle = "لونان تختارهما، والباقي يُحسب ليبقى مقروءاً",
                onBack   = { navController.popBackStack() },
            )

            Preview()

            SectionTitle("تركيبات جاهزة")
            ColorPresets.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { preset ->
                        PresetChip(
                            preset   = preset,
                            selected = paper == preset.paper && accent == preset.accent,
                            modifier = Modifier.weight(1f),
                        ) { save(preset.paper, preset.accent) }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            SectionTitle("الورق")
            Swatches(PaperSwatches, paper) { save(it, accent) }

            SectionTitle("اللهجة")
            Swatches(AccentSwatches, accent) { save(paper, it) }

            SectionTitle("القياس")
            Measures(rc)

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, rc.divider, RoundedCornerShape(16.dp))
                    .clickable { save(null, null) },
                contentAlignment = Alignment.Center,
            ) {
                Text("إرجاع الألوان الأصلية", style = RafiqType.titleM, color = rc.ink)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val rc = LocalRafiqColors.current
    Text(
        text,
        style = RafiqType.titleM,
        color = rc.emerald,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 6.dp),
    )
}

/** معاينةٌ حقيقية: عناصرُ الرئيسية نفسها بالألوان المختارة. */
@Composable
private fun Preview() {
    val rc = LocalRafiqColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(rc.bg)
            .border(1.dp, rc.divider, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 5.dp))
                    .background(rc.emeraldFill),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("رفيق الذِّكر", style = RafiqType.titleM, color = rc.emerald)
                Text("رفيقُك في يومك", style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", style = RafiqType.titleM, color = rc.ink)
        Spacer(Modifier.height(4.dp))
        Text("القرآن الكريم · الرعد ٢٨", style = RafiqType.bodyS, color = rc.inkMed)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(rc.emeraldFill)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) { Text("ابدأ", style = RafiqType.titleM, color = rc.onEmeraldFill) }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(rc.card)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) { Text("بطاقة", style = RafiqType.bodyS, color = rc.inkMed) }
        }
    }
}

@Composable
private fun PresetChip(
    preset: ColorPreset,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) rc.emeraldFill else rc.divider,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(preset.paper)
                .border(1.dp, rc.divider, CircleShape))
            Box(Modifier.size(22.dp).clip(CircleShape).background(preset.accent))
        }
        Spacer(Modifier.height(7.dp))
        Text(preset.name, style = RafiqType.bodyS, color = rc.ink, maxLines = 1)
    }
}

@Composable
private fun Swatches(colors: List<Color>, selected: Color?, onPick: (Color) -> Unit) {
    val rc = LocalRafiqColors.current
    colors.chunked(5).forEach { row ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { c ->
                val on = selected == c
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(c)
                        .border(
                            if (on) 3.dp else 1.dp,
                            if (on) rc.emeraldFill else rc.divider,
                            RoundedCornerShape(14.dp),
                        )
                        .clickable { onPick(c) },
                )
            }
            repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** الأرقام معروضةٌ لأن الثقة تُبنى بالدليل لا بالطلب. */
@Composable
private fun Measures(p: RafiqPalette) {
    val rows = listOf(
        Triple("النصّ على الورق",      contrast(p.ink, p.bg),                 4.5f),
        Triple("الثانوي على البطاقة",  contrast(p.inkMed, p.card),            4.5f),
        Triple("لون اللهجة نصّاً",      contrast(p.emerald, p.card),           4.5f),
        Triple("نصّ الزرّ",             contrast(p.onEmeraldFill, p.emeraldFill), 4.5f),
        Triple("الأيقونات",            contrast(p.inkLight, p.card),          3.0f),
        Triple("نبرة البطاقة",         contrast(p.card, p.bg),                1.2f),
    )
    rows.forEach { (label, value, min) ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (value >= min) "✓" else "✕",
                style = RafiqType.titleM,
                color = if (value >= min) p.emerald else p.error,
            )
            Spacer(Modifier.width(10.dp))
            Text(label, style = RafiqType.bodyS, color = p.inkMed, modifier = Modifier.weight(1f))
            Text(
                "%.2f".format(value),
                style = RafiqType.titleM,
                fontWeight = FontWeight.Bold,
                color = if (value >= min) p.ink else p.error,
            )
        }
    }
}
