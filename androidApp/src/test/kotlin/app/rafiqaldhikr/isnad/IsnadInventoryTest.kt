package app.rafiqaldhikr.isnad

import app.rafiq.domain.model.isnadPercent
import app.rafiq.domain.model.parseIsnadFile
import app.rafiq.domain.model.summarizeIsnad
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * الجردُ على الأصول الحقيقيّة.
 *
 * **وهذا هو الاختبارُ الذي يمنع الشاشةَ أن تكذب.** تقول «مئةٌ بالمئة»،
 * فيُقرأ هنا ما تقرؤه هي من الأصول نفسِها، ويُقابَل بما يفحصه
 * `tools/check_religious_sources.py`. فلو أُضيف نصٌّ بلا إسناد، أو ملفُّ
 * محتوًى لا تراه قاعدةُ الاستكشاف، سقط هذا الاختبار قبل أن تُعرَض الدعوى
 * على أحد.
 */
class IsnadInventoryTest {

    private val assets = listOf(
        File("src/main/assets"),
        File("androidApp/src/main/assets"),
    ).firstOrNull { it.isDirectory }

    private fun entries() = requireNotNull(assets) { "assets directory not found" }
        .listFiles().orEmpty().filter { it.isFile }.sortedBy { it.name }
        .flatMap { parseIsnadFile(it.name, it.readText(Charsets.UTF_8)) }

    /** العددُ الذي يعرضه الحارسُ هو العددُ الذي تعرضه الشاشة. */
    @Test
    fun inventoryMatchesGuardCount() {
        val s = summarizeIsnad(entries())
        assertEquals(110, s.total)
    }

    /** **ولا نصَّ واحداً بلا مصدرٍ ودرجة.** */
    @Test
    fun everyTextCarriesSourceAndGrade() {
        val rows = entries()
        val naked = rows.filter { it.source.isBlank() || it.grade.isBlank() }
        assertTrue("نصوصٌ بلا إسناد: ${naked.map { it.text.take(30) }}", naked.isEmpty())
        assertEquals(100, isnadPercent(summarizeIsnad(rows)))
    }

    /** والدرجاتُ من المجموعة المعروفة — لا درجةَ مخترَعة. */
    @Test
    fun gradesComeFromTheKnownSet() {
        val known = setOf("صحيح", "حسن", "حسن صحيح", "قرآن", "من كلام أهل العلم")
        val unknown = summarizeIsnad(entries()).byGrade.map { it.first }.filterNot { it in known }
        assertTrue("درجاتٌ غير معروفة: $unknown", unknown.isEmpty())
    }

    /** وتوزيعُها كما هو اليوم — فتبدّلُه يُرى ولا يمرّ صامتاً. */
    @Test
    fun gradeDistributionIsStable() {
        val byGrade = summarizeIsnad(entries()).byGrade.toMap()
        assertEquals(80, byGrade["صحيح"])
        assertEquals(15, byGrade["حسن"])
        assertEquals(13, byGrade["قرآن"])
        assertEquals(2, byGrade["من كلام أهل العلم"])
    }
}
