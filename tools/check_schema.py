#!/usr/bin/env python3
"""
حارسُ المخطّط.

يفشل إن تغيّر مخطّطُ القاعدة (`.sq`) بلا ترحيلٍ يقابله في
`RafiqMigrations.MIGRATIONS`.

═══ لماذا ═══

عمودٌ جديدٌ في ملفّ `.sq` يعمل على كل جهازٍ يُثبَّت عليه التطبيق نظيفاً —
ويفشل على كل جهازٍ يُرقّيه، بـ`no such column`، بعد أسابيع. ولا مترجمَ
يمسكه ولا اختبارَ وحدة ولا CI: كلُّ تشغيلٍ هناك تثبيتٌ نظيف.

وهذا بالضبط ما وقع: القائمةُ اليدوية كانت تغطّي ثلاثةَ أعمدةٍ وثلاثةَ
جداول، والمخطّطُ راكم بعدها خمسةَ عشرَ عموداً وجدولين بلا أن يلاحظ أحد.

═══ كيف ═══

يُحسب توقيعُ كلِّ تعريفات الجداول والفهارس في `.sq` ويُقارن بالتوقيع
المحفوظ هنا. فإن اختلفا:

  ١) أضِف ترحيلاً جديداً إلى `MIGRATIONS` يُطبّق التغيير على قاعدةٍ قائمة.
  ٢) حدّث `EXPECTED_SCHEMA_HASH` أدناه بالتوقيع الذي يطبعه هذا السكربت.

والخطوةُ الثانية وحدَها لا تكفي — لكنّها تُجبر من غيّر المخطّط على أن
يقف عند الأولى ويفكّر فيها.
"""
import hashlib
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SQ_DIR = ROOT / "shared/src/commonMain/sqldelight/app/rafiq/db"
MIGRATIONS = ROOT / "shared/src/androidMain/kotlin/app/rafiq/data/db/RafiqMigrations.kt"

#  يُحدَّث مع كل تغييرٍ في المخطّط — بعد إضافة ترحيلٍ يقابله.
EXPECTED_SCHEMA_HASH = "ab589e8230e82b0a"
#  عددُ الترحيلات المتوقَّع. يزيد واحداً مع كل تغييرٍ في المخطّط.
EXPECTED_MIGRATIONS = 2

DDL = re.compile(
    r"^\s*(CREATE\s+(?:TABLE|INDEX|VIEW|TRIGGER|UNIQUE\s+INDEX)\b[\s\S]*?;)",
    re.MULTILINE | re.IGNORECASE,
)


def schema_text() -> str:
    """كلُّ تعريفات البنية من كل ملفّ .sq، مرتَّبةً ومجرَّدةً من المسافات."""
    chunks = []
    for f in sorted(SQ_DIR.glob("*.sq")):
        for m in DDL.finditer(f.read_text(encoding="utf-8")):
            chunks.append(re.sub(r"\s+", " ", m.group(1)).strip())
    return "\n".join(sorted(chunks))


def migration_count() -> int:
    """عددُ الترحيلات — تُعدّ قوائمُ `listOf(` المباشرة داخل MIGRATIONS."""
    src = MIGRATIONS.read_text(encoding="utf-8")
    body = src.split("val MIGRATIONS", 1)[1]
    # كل ترحيلٍ يبدأ بسطرٍ فيه تعليقٌ من نمط «═══ رقم — »
    return len(re.findall(r"//\s*═+\s*\d+\s*—", body))


def main() -> int:
    text = schema_text()
    digest = hashlib.sha256(text.encode("utf-8")).hexdigest()[:16]
    count = migration_count()

    print(f"جداولُ وفهارس: {len(text.splitlines())} · "
          f"توقيع: {digest} · ترحيلات: {count}")

    problems = []
    if EXPECTED_SCHEMA_HASH == "PLACEHOLDER":
        problems.append(
            f"‏EXPECTED_SCHEMA_HASH غير مضبوط. ضع فيه: {digest}"
        )
    elif digest != EXPECTED_SCHEMA_HASH:
        problems.append(
            f"تغيّر المخطّط (التوقيع {digest} والمتوقَّع {EXPECTED_SCHEMA_HASH}).\n"
            f"     أضِف ترحيلاً في RafiqMigrations.MIGRATIONS يُطبّق التغيير على\n"
            f"     قاعدةٍ قائمة، ثمّ ضع التوقيعَ الجديد هنا."
        )
    if count != EXPECTED_MIGRATIONS:
        problems.append(
            f"عددُ الترحيلات {count} والمتوقَّع {EXPECTED_MIGRATIONS} — "
            f"حدّث EXPECTED_MIGRATIONS معه."
        )

    if problems:
        for p in problems:
            print("  ✗ " + p)
        return 1
    print("المخطّطُ يطابق الترحيلات.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
