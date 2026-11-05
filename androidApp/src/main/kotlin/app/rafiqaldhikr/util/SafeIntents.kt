package app.rafiqaldhikr.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.rafiqaldhikr.R

/**
 * يفتح تطبيقَ بريدٍ برسالةٍ مُعدَّة — أو يعتذر بدل أن يسقط.
 *
 * **الخطأ الذي كان:** أربعةُ مواضع في شاشة التواصل تُنادي
 * `context.startActivity(Intent(ACTION_SENDTO))` بلا حراسة. وأيُّ جهازٍ بلا
 * تطبيق بريدٍ مثبَّت — وهو حالٌ شائعةٌ على اللوحيّات وعلى الأجهزة بلا
 * خدمات جوجل — يرمي `ActivityNotFoundException` فيسقط التطبيق. أي أنّ
 * الضغط على «تواصل معنا» كان يُنهي التطبيق عند طائفةٍ من المستخدمين.
 *
 * @return true إن فُتح تطبيقُ بريد.
 */
fun Context.sendMail(
    to: String,
    subject: String,
    body: String? = null,
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$to")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        if (body != null) putExtra(Intent.EXTRA_TEXT, body)
    }
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        // العنوانُ نفسُه في الرسالة: من لا بريدَ عنده يستطيع نسخَه ومراسلتَنا
        // من مكانٍ آخر، بدل أن يبقى بلا سبيل.
        Toast.makeText(this, getString(R.string.contact_no_mail_app, to), Toast.LENGTH_LONG).show()
        false
    }
}
