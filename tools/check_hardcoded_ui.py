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
EXEMPT = {"Baqiyat"}

#  السقفُ الحاليّ. يُخفَّض ولا يُرفَع.
CEILING = 80

ARABIC = re.compile(r'"([^"\\\n]*[؀-ۿ][^"\\\n]*)"')


def strip_comments(s: str) -> str:
    s = re.sub(r'//.*', '', s)
    return re.sub(r'/\*[\s\S]*?\*/', '', s)


counts = collections.Counter()
for path in SRC.rglob("*.kt"):
    if path.stem in HIDDEN or path.stem in EXEMPT:
        continue
    body = strip_comments(path.read_text(encoding="utf-8"))
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
