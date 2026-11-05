#!/usr/bin/env python3
"""
حارس ملفات النصوص.

سببان اثنان:

١) الفاصلة العليا غير المهروبة. `Today's` يرفضها aapt2، ورسالته
   «Can not extract resource from ParsedResource@…» لا تذكر الفاصلة ولا
   السطر — فيضيع الوقت في البحث عن خطأ في XML سليم تماماً.

٢) مفتاح عربي بلا مقابل إنجليزي. كانت ١٥ سلسلة ناقصة، فتسقط الواجهة
   الإنجليزية إلى العربية في منتصفها. lint يمسكها لكنه أبطأ بكثير.
"""
import re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
RES  = ROOT / "androidApp/src/main/res"

def strings(path):
    t = path.read_text()
    return {m.group(1): m.group(2)
            for m in re.finditer(r'<string name="([^"]+)"[^>]*>(.*?)</string>', t, re.S)}


def keys_in_order(path):
    """كلُّ المفاتيح بترتيبها — لكشف التكرار الذي يبتلعه القاموس."""
    return re.findall(r'<(?:string|plurals|string-array) name="([^"]+)"',
                      path.read_text())

problems = []

#  مفتاحٌ مكرَّر يُفشل `mergeDebugResources` برسالةٍ لا تذكر السطر، وبعد
#  دقيقتين من عمل aapt2. ورأيتُه يقع: `action_delete` أُضيف مرّتين في
#  دفعتين متتاليتين من الترجمة. والقاموسُ في `strings()` يبتلع التكرار
#  فلا يظهر في فحص التوازي — فيُعدّ هنا بالترتيب.
for f in sorted(RES.rglob("*.xml")):
    if f.name not in ("strings.xml", "arrays.xml"):
        continue
    ks = keys_in_order(f)
    for k in sorted({x for x in ks if ks.count(x) > 1}):
        problems.append(f"{f.relative_to(ROOT)} → المفتاح {k!r} مكرَّر {ks.count(k)} مرّات")

for f in sorted(RES.rglob("strings.xml")):
    for key, value in strings(f).items():
        # محاطة بعلامتَي تنصيص = الهروب غير مطلوب
        if value.strip().startswith('"') and value.strip().endswith('"'):
            continue
        if re.search(r"(?<!\\)'", value):
            problems.append(f"{f.relative_to(ROOT)} → {key}: فاصلة عليا غير مهروبة في {value!r}")

ar = strings(RES / "values/strings.xml")
en = strings(RES / "values-en/strings.xml")
for k in sorted(ar.keys() - en.keys()):
    problems.append(f"values-en/strings.xml → ينقصه المفتاح {k!r} (العربي: {ar[k]!r})")
for k in sorted(en.keys() - ar.keys()):
    problems.append(f"values/strings.xml → ينقصه المفتاح {k!r} (الإنجليزي: {en[k]!r})")

print(f"العربية {len(ar)} · الإنجليزية {len(en)} · مشاكل {len(problems)}")
if problems:
    for p in problems:
        print("  ✗ " + p)
    sys.exit(1)
print("ملفات النصوص سليمة.")
