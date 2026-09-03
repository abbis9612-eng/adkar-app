#!/usr/bin/env python3
"""
حارسُ النصّ العربي المكتوب في الواجهة.

يفشل إن زاد عددُ النصوص العربية المضمَّنة في Kotlin عن السقف المسموح.

═══ لماذا سقفٌ لا صفر ═══

يبقى في المستودع نصٌّ عربيٌّ مقصودٌ لا يُترجَم:

  · `DayCompanionViewModel` — حديثُ كلِّ محطّةٍ وتخريجُه. وترجمةُ الحديث
    تفسيرٌ له لا نقلٌ حرفيّ، وقاعدةُ AGENTS.md أن لا يُصاغ نصٌّ ديني ولا
    يُعاد صوغُه. فيُعرَض بلفظه في اللغتين.
  · `TasbeehScreen` — ألفاظُ الباقيات الصالحات، وهي موضوعُ الشاشة نفسِها
    (وهي في قائمة السماح في `check_devotional_in_code.py`).
  · `MushafDownloader` — رسائلُ خطأٍ داخلية تُغلَّف قبل أن تصل الواجهة.

وكان العددُ ٦٤٧ نصّاً حين بدأ هذا العمل: الشاشةُ الرئيسيةُ والمصحفُ
والمسبحةُ والقبلةُ والشاشاتُ القانونية كلُّها بلا `stringResource` واحدة.
فالسقفُ يمنع العودة، ويُخفَّض كلّما أُخرج نصٌّ إلى الموارد.
"""
import collections
import os
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "androidApp/src/main/kotlin"

#  لم يبقَ في المستودع شاشةٌ مخفيّة: ثلاثٌ حُذفت وأربعٌ رُفع عنها
#  الإخفاءُ وصار لها مدخلٌ ونصوصُها في strings.xml. فالمجموعةُ فارغة،
#  وتبقى لتُملأ إن أُجّلت شاشةٌ يوماً بقرارٍ مكتوب.
HIDDEN: set[str] = set()

#  استثناءٌ واحدٌ مكتوبُ السبب: `Baqiyat.kt` ليس نصَّ واجهةٍ يُترجَم، بل
#  ألفاظُ ذكرٍ تُقال بلفظها — من سبّح بالإنجليزية لم يُسبّح. فلا محلَّ
#  لها في strings.xml، والحارسُ لا يشتكي منها.
EXEMPT = {
    "Baqiyat",
    #  شاشةُ تقرير الانهيار وسجلُّه: لا يجوز أن يمرّا بـ`stringResource`.
    #
    #  هما يعملان حين يكون التطبيقُ نفسُه معطوباً — وقد يكون العطبُ في
    #  تحميل الموارد أو في Koin أو في السمة. فشاشةٌ تشرح الانهيارَ
    #  تنهار بنفس سببه لا تشرح شيئاً. ولذلك ألوانُها ونصوصُها مكتوبةٌ
    #  فيها، ولا تُترجَم: من يراها عربيٌّ يقرأ ما نكتبه له.
    "CrashReportScreen",
    "CrashLog",
}

#  السقفُ الحاليّ. يُخفَّض ولا يُرفَع.
CEILING = 70

ARABIC = re.compile(r'"([^"\\\n]*[؀-ۿ][^"\\\n]*)"')


def strip_comments(s: str) -> str:
    s = re.sub(r'//.*', '', s)
    return re.sub(r'/\*[\s\S]*?\*/', '', s)


def strip_logs(s: str) -> str:
    """
    يمحو نداءاتِ `Log.*` — رسائلُها لا تصل المستخدمَ أبداً.

    كان الحارسُ يعدّها نصَّ واجهةٍ فيطالب بإخراجها إلى `strings.xml`،
    وهو عبثٌ مضاعف: تُترجَم رسالةٌ لا يقرؤها إلّا مطوِّرٌ في Logcat،
    ويُحشى ملفُّ الترجمة بما ليس منه. وثلاثُ رسائلَ كهذه كانت داخلةً
    في السقف من قبلُ فرفعته بلا سبب.

    والمسحُ بموازنة الأقواس لا بالسطر: النداءُ قد يمتدّ أسطراً.
    """
    out, i, n = [], 0, len(s)
    while i < n:
        m = re.compile(r'\bLog\.[edwiv]\s*\(').search(s, i)
        if not m:
            out.append(s[i:])
            break
        out.append(s[i:m.start()])
        j, depth, instr = m.end(), 1, False
        while j < n and depth:
            ch = s[j]
            if instr:
                if ch == '\\':
                    j += 1
                elif ch == '"':
                    instr = False
            elif ch == '"':
                instr = True
            elif ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            j += 1
        #  تُبقى الأسطرُ كما هي فلا تختلّ أرقامُها لو احتيج إليها
        out.append('\n' * s.count('\n', m.start(), j))
        i = j
    return ''.join(out)


counts = collections.Counter()
for path in SRC.rglob("*.kt"):
    if path.stem in HIDDEN or path.stem in EXEMPT:
        continue
    body = strip_logs(strip_comments(path.read_text(encoding="utf-8")))
    n = len(ARABIC.findall(body))
    if n:
        counts[str(path.relative_to(ROOT))] = n

total = sum(counts.values())
print(f"نصوصٌ عربيةٌ في Kotlin (الشاشات المعروضة): {total} · السقف {CEILING}")

if total > CEILING:
    print(f"  ✗ زاد {total - CEILING} عن السقف. أخرِج النصَّ الجديد إلى "
          f"res/values/strings.xml و values-en.")
    for p, n in counts.most_common(8):
        print(f"      {n:4d}  {p}")
    sys.exit(1)

if total < CEILING:
    print(f"  ✗ نزل إلى {total} — اخفِض CEILING في هذا الملفّ إليه "
          f"حتى لا يُسمح بالعودة.")
    sys.exit(1)

print("لا نصَّ عربيّاً جديداً في الواجهة.")
