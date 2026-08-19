#!/usr/bin/env python3
"""
حارس المحتوى الديني.

القاعدة في AGENTS.md: لا نصّ ديني بلا مصدر موثَّق، ولا يُعاد صوغ نصّ ولا
يُختصر. هذا الحارس يفرض الشطر الأول آلياً — الشطر الثاني مسؤولية بشرية.

يفحص:
  ١) كل نصّ في ملفات المحتوى له source و source_grade غير فارغين.
  ٢) الدرجة من مجموعة معروفة — لا درجة مخترَعة ولا مبهمة.
  ٣) لا ملف محتوى ميت: كل ملف يقرؤه DatabaseSeeder فعلاً.
     (كان adhkar.json يحمل ٢٢ نصاً لا يقرؤها أحد، فيظنّها القارئ معروضة.)
"""
import json, re, sys, pathlib

ROOT   = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "androidApp/src/main/assets"
SEEDER = ROOT / "shared/src/commonMain/kotlin/app/rafiq/data/db/DatabaseSeeder.kt"

# الدرجات المقبولة. أي درجة خارجها قرارٌ يحتاج مراجعة بشرية لا تمريراً صامتاً.
GRADES = {"صحيح", "حسن", "حسن صحيح", "قرآن"}

# ملفات ليست نصوصاً دينية مفردة (القرآن والتفسير لهما مسارهما وحجمهما)
SKIP = {"quran_uthmani.json", "tafsir_muyassar.json", "surah_metadata.json"}

seeder = SEEDER.read_text()
problems, checked = [], 0

for f in sorted(ASSETS.glob("*.json")):
    if f.name in SKIP:
        continue
    if f'"{f.name}"' not in seeder:
        problems.append(f"{f.name}: ملف محتوى لا يقرؤه DatabaseSeeder — نصوصه لا يراها أحد")
        continue
    items = json.loads(f.read_text())
    if not isinstance(items, list):
        items = sum((v for v in items.values() if isinstance(v, list)), [])
    for i, it in enumerate(items):
        checked += 1
        text  = (it.get("text_ar") or "").strip()
        src   = (it.get("source") or "").strip()
        grade = (it.get("source_grade") or "").strip()
        where = f"{f.name}[{i}] {text[:35]}…"
        if not src:
            problems.append(f"{where}: بلا مصدر")
        if not grade:
            problems.append(f"{where}: بلا درجة")
        elif grade not in GRADES:
            problems.append(f"{where}: درجة غير معروفة {grade!r} (المقبول: {', '.join(sorted(GRADES))})")

print(f"فُحص {checked} نصاً دينياً · مشاكل {len(problems)}")
if problems:
    for p in problems:
        print("  ✗ " + p)
    sys.exit(1)
print("كل نصّ له مصدر ودرجة معروفة، ولا ملف محتوى ميت.")
