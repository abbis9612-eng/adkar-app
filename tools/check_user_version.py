#!/usr/bin/env python3
"""
حارسُ `PRAGMA user_version` — الحقلُ ملكُ SQLDelight لا ملكُنا.

**العطبُ الذي كتب هذا الحارس.**

`AndroidSqliteDriver` يمرّر `RafiqDatabase.Schema.version` إلى
`SupportSQLiteOpenHelper` بوصفه نسخةَ القاعدة، وأندرويد يخزّنها في
`PRAGMA user_version` بعينه. فحين كتب نظامُ الترحيل عندنا رقمَه في نفس
الحقل، تصادما:

    أوّلُ تشغيل  → أندرويد يكتب ١، ثمّ نكتب نحن ٢. التطبيق يعمل.
    ما بعده     → الفتحُ يجد ٢ والمطلوب ١ ← «تنزيلُ نسخة» ← onDowngrade

و`AndroidSqliteDriver$Callback` لا يُعرّف `onDowngrade` (فُحص البايتكود)،
فيعمل الأساسُ ويرمي:

    SQLiteException: Can't downgrade database from version 2 to 1

والرميُ عند **بناء المحرِّك** — قبل أيّ كودٍ لنا وقبل أن تُرسم شاشة. أي
تطبيقٌ يفتح ويُغلق فوراً في كل مرّةٍ بعد الأولى، ولا شيء داخله ينقذه.

فرقمُ ترحيلنا في جدول `RafiqMigration`، و`user_version` يُترك لصاحبه.
والاستثناءُ الوحيد `DatabaseDriverFactory`: هناك يُقرأ ويُعاد إلى نسخة
المخطّط لإصلاح الأجهزة التي عطبت — وذلك عكسُ العطب لا تكرارُه.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC  = [ROOT / "shared/src", ROOT / "androidApp/src/main/kotlin"]

WRITE = re.compile(r"user_version\s*=|\.version\s*=\s*(?!=)")
ALLOWED = {"DatabaseDriverFactory.kt": "يُصلح الحقلَ ويعيده إلى نسخة المخطّط — عكسُ العطب."}

def without_comments(src: str) -> str:
    """
    يمحو الشروحَ ويُبقي الأسطرَ — فالأرقامُ تبقى صحيحة.

    والبادئةُ المجرّدة لا تكفي: شرحُ هذا العطبِ نفسِه كتلةٌ فيها أسطرٌ
    تبدأ بنقطةٍ لا بنجمة، فكان الحارسُ يشتكي من الشرح الذي يفسّره.
    """
    out, i, n = [], 0, len(src)
    while i < n:
        if src.startswith("/*", i):
            end = src.find("*/", i + 2)
            end = n if end < 0 else end + 2
            out.append("".join(c if c == "\n" else " " for c in src[i:end]))
            i = end
        elif src.startswith("//", i):
            end = src.find("\n", i)
            end = n if end < 0 else end
            out.append(" " * (end - i))
            i = end
        else:
            out.append(src[i])
            i += 1
    return "".join(out)


problems = 0
checked  = 0
for root in SRC:
    for f in sorted(root.rglob("*.kt")):
        if "/build/" in str(f):
            continue
        code = without_comments(f.read_text(encoding="utf-8"))
        for i, line in enumerate(code.split("\n"), 1):
            if "user_version" not in line:
                continue
            checked += 1
            if not WRITE.search(line):              # قراءةٌ مجرّدة — لا بأس
                continue
            if f.name in ALLOWED:
                continue
            print(f"  ✗ {f.relative_to(ROOT)}:{i}")
            print("      كتابةٌ في PRAGMA user_version — الحقلُ ملكُ SQLDelight.")
            print("      رقمُ الترحيل يُحفظ في جدول RafiqMigration.")
            problems += 1

print(f"فُحص {checked} سطراً يذكر user_version · مشاكل {problems}")
sys.exit(1 if problems else 0)
