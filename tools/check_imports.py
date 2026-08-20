#!/usr/bin/env python3
"""
حارس الاستيرادات — الاتجاه الذي أهملتُه مرّتين.

كان عندي فحص يسأل: «كل استيراد، هل يُستعمل؟» وهو لا يكشف العكس:
رمزٌ يُستعمل بلا استيراد. وقد كسر ذلك البناء مرّتين في يوم واحد — آخرها
حين أدرجتُ stringResource في ملف يستورد الحزمة بـ * فلم يُطابَق المرساة
الذي بحث عنه سكربتي، فمرّ التعديل بلا استيراد وبلا إنذار.

يفحص هنا الرموز التي يحتاجها كل تعديل i18n تحديداً، مع احترام
الاستيراد بالنجمة.
"""
import re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC  = ROOT / "androidApp/src/main/kotlin"

# رمز مستعمَل → (نمط الاستعمال، الاستيراد الصريح، حزمة النجمة المقبولة)
NEEDS = [
    (r'\bstringResource\s*\(',       "androidx.compose.ui.res.stringResource",       "androidx.compose.ui.res"),
    (r'\bpluralStringResource\s*\(', "androidx.compose.ui.res.pluralStringResource", "androidx.compose.ui.res"),
    (r'@StringRes\b',                "androidx.annotation.StringRes",                "androidx.annotation"),
    (r'\bR\.(string|plurals|drawable)\b', "app.rafiqaldhikr.R",                      None),
]

problems = 0
for f in sorted(SRC.rglob("*.kt")):
    text = f.read_text()
    body = "\n".join(l for l in text.split("\n") if not l.startswith("import "))
    imports = {l.strip() for l in text.split("\n") if l.startswith("import ")}
    for pattern, explicit, star_pkg in NEEDS:
        if not re.search(pattern, body):
            continue
        ok = f"import {explicit}" in imports
        if not ok and star_pkg:
            ok = f"import {star_pkg}.*" in imports
        if not ok:
            print(f"  ✗ {f.relative_to(ROOT)}: يستعمل {explicit.rsplit('.',1)[1]} بلا استيراد")
            problems += 1

print(f"مشاكل استيراد: {problems}")
sys.exit(1 if problems else 0)
