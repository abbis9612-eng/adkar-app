#!/usr/bin/env python3
"""
حارس المسارات اليتيمة.

سبب وجوده: تراكمت في التطبيق ست شاشات مكتوبة بالكامل ومسجَّلة في الرسم
البياني لا يصلها زرّ واحد — لأن لا شيء كان يمنع ذلك. هذا الفحص يفشل
البناء على أي مسار جديد بلا مدخل، فلا تتكرّر الحكاية.

الاستثناء الوحيد: وسم @HiddenInV1 على تعريف المسار — قرار صريح بتأجيل
شاشة إلى ما بعد V1 مع إبقاء كودها.
"""
import re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
KT   = ROOT / "androidApp/src/main/kotlin"
NAV  = KT / "app/rafiqaldhikr/ui/navigation"

src = (NAV / "RafiqRoute.kt").read_text()
routes = re.findall(
    r'^\s*(@HiddenInV1\s+)?data object (\w+)\s*:\s*RafiqRoute\("([^"]+)"\)',
    src, re.M,
)

consumers = {}
for p in KT.rglob("*.kt"):
    if p.parent == NAV and p.name in ("RafiqRoute.kt", "RafiqNavGraph.kt"):
        continue
    consumers[p] = p.read_text()

orphans, hidden = [], []
for mark, name, path in routes:
    if mark:
        hidden.append(name)
        continue
    base = path.split("/")[0]
    if any(re.search(rf'RafiqRoute\.{name}\b', t) or re.search(rf'"{re.escape(base)}(?:/|")', t)
           for t in consumers.values()):
        continue
    orphans.append((name, path))

print(f"{len(routes)} مسار · {len(hidden)} مؤجَّل بوسم @HiddenInV1 · {len(orphans)} يتيم")
if hidden:
    print("  مؤجَّلة: " + "، ".join(hidden))
if orphans:
    print("\nمسارات لا يصلها أي مدخل — أضف زرّاً، أو احذف الشاشة، أو ضع @HiddenInV1:")
    for n, p in orphans:
        print(f"  ✗ {n}  ({p})")
    sys.exit(1)
print("لا مسار يتيم.")
