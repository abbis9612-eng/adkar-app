package app.rafiqaldhikr.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import app.rafiqaldhikr.R
import java.io.File

/*
 * بطاقةُ الآية — صورةٌ تُشارَك
 * ═══════════════════════════
 *
 * **تُرسم رسماً أصليّاً لا تُلتقط من الشاشة.**
 *
 * والتقاطُ ما هو معروضٌ أسهل، لكنّه يُخرج بطاقةً بمقاس جهاز صاحبها: فمن
 * شارك من هاتفٍ صغيرٍ أخرج صورةً صغيرةً مضغوطةَ الأسطر، ومن شارك وقد
 * كبّر خطَّ نظامه أخرج أسطراً مقطوعة. والصورةُ تخرج من الجهاز إلى
 * غيره، فلا يصحّ أن تتبدّل بتبدّل الجهاز.
 *
 * فالمقاسُ هنا ثابتٌ — ١٠٨٠ عرضاً وارتفاعٌ يتبع النصّ — والخطُّ خطُّ
 * التطبيق المشحون، والألوانُ تُمرَّر من اللوحة فتتبع السمة.
 *
 * **والنصُّ القرآنيُّ يُرسم كما هو حرفاً بحرف** — لا يُقتطع ولا تُحذف
 * منه علامةٌ لتستقيم البطاقة. فإن طال طالت البطاقةُ معه.
 */

/** عرضُ البطاقة بالبكسل — ثابتٌ لا يتبع الجهاز. */
private const val W = 1080

/** الحشوةُ الجانبيّة. */
private const val PAD = 96f

/**
 * يرسم بطاقةَ الآية ويُرجعها صورةً.
 *
 * @param ayahText النصُّ العثمانيُّ كما هو في القاعدة
 * @param label    «البقرة · الآية ١٣»
 * @param bg       لونُ الورق · [ink] لونُ الحبر · [gold] لونُ الإسناد
 */
fun renderAyahCard(
    ctx: Context,
    ayahText: String,
    label: String,
    appName: String,
    bg: Int,
    ink: Int,
    gold: Int,
): Bitmap {
    val quran: Typeface = ResourcesCompat.getFont(ctx, R.font.scheherazade_regular)
        ?: Typeface.SERIF
    val ui: Typeface = ResourcesCompat.getFont(ctx, R.font.noto_sans_arabic_medium)
        ?: Typeface.SANS_SERIF

    val textW = (W - PAD * 2).toInt()

    val ayahPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = quran
        textSize = 58f
        color = ink
    }
    //  ١٫٨ ارتفاعَ سطرٍ للنصّ المشكول — أقلُّ منه تتلاصق الحركات
    val ayahLayout = StaticLayout.Builder
        .obtain(ayahText, 0, ayahText.length, ayahPaint, textW)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(0f, 1.8f)
        .setIncludePad(false)
        .build()

    val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ui
        textSize = 34f
        color = gold
        textAlign = Paint.Align.CENTER
    }
    val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ui
        textSize = 26f
        color = ink
        alpha = 110
        textAlign = Paint.Align.CENTER
    }

    val topPad = 104f
    val gapAfterAyah = 56f
    val ruleH = 2f
    val gapAfterRule = 44f
    val gapAfterLabel = 64f
    val bottomPad = 72f

    val h = (
        topPad + ayahLayout.height + gapAfterAyah + ruleH + gapAfterRule +
            (labelPaint.descent() - labelPaint.ascent()) + gapAfterLabel +
            (namePaint.descent() - namePaint.ascent()) + bottomPad
        ).toInt()

    val bmp = Bitmap.createBitmap(W, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(bg)

    //  إطارٌ ذهبيٌّ رفيعٌ يحدّ البطاقة — نفسُ نبرة الإسناد في التطبيق
    val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = gold
        alpha = 64
    }
    c.drawRoundRect(22f, 22f, W - 22f, h - 22f, 34f, 34f, frame)

    c.save()
    c.translate(PAD, topPad)
    ayahLayout.draw(c)
    c.restore()

    var y = topPad + ayahLayout.height + gapAfterAyah

    //  خيطٌ قصيرٌ يفصل النصَّ عن إسناده — بعرض الخُمسين لا بعرض البطاقة
    val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; alpha = 90 }
    c.drawRect(W / 2f - 90f, y, W / 2f + 90f, y + ruleH, rule)
    y += ruleH + gapAfterRule - labelPaint.ascent()

    c.drawText(label, W / 2f, y, labelPaint)
    y += labelPaint.descent() + gapAfterLabel - namePaint.ascent()

    c.drawText(appName, W / 2f, y, namePaint)
    return bmp
}

/**
 * يكتب الصورةَ في `cacheDir/shares` ويُرسلها عبر `FileProvider`.
 *
 * نفسُ المزوّد الذي يخدم التصدير وبطاقةَ الإنجاز — لا مزوّدَ ثانيَ ولا
 * إذنَ تخزين: `FLAG_GRANT_READ_URI_PERMISSION` وحدَه يفتح الملفَّ
 * للمستقبِل.
 *
 * @return false إن لم يوجد تطبيقٌ يقبل الصورة — فتُعرض رسالةٌ ولا يسقط
 *         التطبيق.
 */
fun shareBitmap(ctx: Context, bitmap: Bitmap, chooserTitle: String, name: String): Boolean {
    val dir = File(ctx.cacheDir, "shares").apply { mkdirs() }
    // صورةٌ واحدةٌ في الذاكرة المؤقّتة — لا تتراكم مع كلّ مشاركة.
    dir.listFiles()?.forEach { it.delete() }
    val file = File(dir, name)
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        ctx.startActivity(Intent.createChooser(intent, chooserTitle))
        true
    } catch (e: android.content.ActivityNotFoundException) {
        false
    }
}
