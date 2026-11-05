#!/usr/bin/env python3
"""
حارسُ السياق البصريّ — واجهاتٌ تسقط إن نُودِيت بسياق التطبيق.

بعضُ واجهات أندرويد لا تعمل إلّا على «سياقٍ بصريّ»: نشاطٍ أو سياقِ نافذة.
ونداؤها على سياق التطبيق لا يفشل بقيمةٍ فارغة — بل **يرمي**:

    UnsupportedOperationException: Tried to obtain display from a Context
    not associated with one.

وقد أسقط هذا التطبيقَ فعلاً. `CompassManager` يُبنى في Koin بـ
`androidContext()` — أي سياق التطبيق — وكان ينادي `context.display` داخل
`onSensorChanged`. فعند أوّل قراءةِ بوصلةٍ بعد فتح شاشة القبلة، على أندرويد
١١ فأعلى، يُرمى النازلُ من ردّ نداء المستشعر فتموت العملية.

ثمّ يستعيد أندرويدُ آخرَ شاشةٍ عند الفتح التالي فيعود إلى القبلة فيسقط
قبل أن يظهر — **حلقةٌ لا تنكسر بإعادة الفتح**. أي أنّ عطباً في شاشةٍ
واحدةٍ جعل التطبيقَ كلَّه لا يُفتح.

والبديلُ في كل حالة موجودٌ ويعمل من أيّ سياق:
    context.display          →  DisplayManager.getDisplay(Display.DEFAULT_DISPLAY)
    windowManager.defaultDisplay  →  المثلُ (وهي مهجورةٌ أيضاً)
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC  = ROOT / "androidApp/src/main/kotlin"

BANNED = [
    (re.compile(r"\bcontext\.display\b"),
     "context.display — يرمي على سياق التطبيق. استعمل DisplayManager."),
    (re.compile(r"\bdefaultDisplay\b"),
     "defaultDisplay — مهجورةٌ وتتطلّب سياقاً بصرياً. استعمل DisplayManager."),
]

problems = 0
for f in sorted(SRC.rglob("*.kt")):
    for i, line in enumerate(f.read_text(encoding="utf-8").split("\n"), 1):
        s = line.strip()
        # الشروحُ تذكر الاسمَ لتفسير العطب — وهي التوثيقُ لا العطب.
        if s.startswith(("//", "*", "/*")):
            continue
        for rx, why in BANNED:
            if rx.search(line):
                print(f"  ✗ {f.relative_to(ROOT)}:{i}")
                print(f"      {why}")
                problems += 1

print(f"مشاكل السياق البصريّ: {problems}")
sys.exit(1 if problems else 0)
