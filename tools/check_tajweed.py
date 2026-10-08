#!/usr/bin/env python3
"""
حارسُ أصل التجويد.

القاعدة: **لا يُلوَّن حرفٌ بحكمٍ لم يقع عليه.** وتلوينُ حرفٍ خاطئٍ في
كتاب الله أسوأُ من ألّا يكون تلوينٌ أصلاً.

يفحص:
  ١) كلُّ مدًى داخلَ حدود آيته — لا يتجاوز طولَ النصّ.
  ٢) كلُّ حكمٍ له حرفٌ معلومٌ يقع عليه فعلاً:
       همزةُ الوصل (ٱ) · اللامُ الشمسيّة (ل) · القلقلة (ق ط ب ج د)
  ٣) فهرسُ الحكم داخلَ الثمانية عشر المعروفة.

ومن عدّل `tools/build_tajweed.py` أو بدّل نصَّ المصحف يسقط هنا قبل أن
يصل إلى جهازِ أحد.
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TAJ = ROOT / "androidApp/src/main/assets/tajweed.txt"
QURAN = ROOT / "androidApp/src/main/assets/quran_uthmani.json"

EXPECT = {15: "ٱ", 16: "ل", 14: "قطبجد"}
RULES = 18

if not TAJ.exists():
    print("لا أصلَ تجويد — الميزةُ غيرُ مبنيّة، ولا شيءَ يُفحص.")
    sys.exit(0)

q = json.loads(QURAN.read_text(encoding="utf-8"))
items = q if isinstance(q, list) else list(q.values())[0]
ours = {(a["surah"], a["ayah"]): a["text_uthmani"] for a in items}

problems = []
spans = checked = 0

for ln, line in enumerate(TAJ.read_text(encoding="utf-8").splitlines(), 1):
    if not line.strip():
        continue
    p = line.split("|")
    if len(p) != 3:
        problems.append(f"سطر {ln}: بنيةٌ غيرُ صحيحة")
        continue
    s, a = int(p[0]), int(p[1])
    text = ours.get((s, a))
    if text is None:
        problems.append(f"{s}:{a} آيةٌ لا وجودَ لها في المصحف")
        continue
    for sp in p[2].split(";"):
        f = sp.split(",")
        if len(f) != 3:
            problems.append(f"{s}:{a} مدًى غيرُ صحيح: {sp}")
            continue
        st, en, rule = int(f[0]), int(f[1]), int(f[2])
        spans += 1
        if not (0 <= st < en <= len(text)):
            problems.append(f"{s}:{a} مدًى خارج الآية: {st}..{en} والطول {len(text)}")
            continue
        if not (0 <= rule < RULES):
            problems.append(f"{s}:{a} حكمٌ مجهول: {rule}")
            continue
        want = EXPECT.get(rule)
        if want is not None:
            checked += 1
            if not any(c in want for c in text[st:en]):
                problems.append(
                    f"{s}:{a} حكم {rule} على {text[st:en]!r} ولا حرفَ منه فيه"
                )

print(f"فُحص {spans} حكماً في {TAJ.name} · منها {checked} بحرفٍ معلوم · "
      f"مشاكل {len(problems)}")
for p_ in problems[:10]:
    print("  ✗", p_)
if problems:
    print("أصلُ التجويد لا يُشحن وفيه حكمٌ على حرفٍ لم يقع عليه.")
    sys.exit(1)
print("كلُّ حكمٍ في موضعه.")
