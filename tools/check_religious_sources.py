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
KOTLIN = [ROOT / "shared/src", ROOT / "androidApp/src/main"]

# الدرجات المقبولة. أي درجة خارجها قرارٌ يحتاج مراجعة بشرية لا تمريراً صامتاً.
#
# «من كلام أهل العلم» أُضيفت لـwisdom.json: كلامُ ابن القيّم وابن تيمية ليس
# حديثاً يُدرَّج صحيحاً أو حسناً — تدريجُ الحديث حكمٌ على إسنادٍ مرفوع، وهذا
# قولُ عالِمٍ لا إسناد له. مصدرُه كتابُه، وهو المطلوب. ولولا هذه الدرجة
# لاضطُرّ من يضيف كلاماً لعالِمٍ إلى وسمه «صحيحاً» — وذلك ادّعاءٌ لا يجوز.
#  ═══ درجاتٌ وردت من الموسوعة الحديثيّة بألفاظها ═══
#
#  ثمانيةُ ألفاظٍ جاءت في `athar.json` ولم تكن في القائمة. وكلُّها صيغٌ
#  علميّةٌ معتبرةٌ لا مخترَعةٌ، وقاعدةُ المشروع «لا تصغ ولا تعدّل النصوص»
#  تحكم الدرجةَ كما تحكم المتن: فدخلت **بلفظها كما حكم بها أهلُ العلم**،
#  ولم تُطبَّع إلى «صحيح» و«حسن» — وتطبيعُها تغييرٌ لحكمٍ لا تبسيطُ حقل.
#
#  والأخيرتان حكمان مركَّبان على روايتين في مدخلٍ واحد، فلفظُهما كذلك.
#
#  وهي معروضةٌ على صاحب التطبيق: ما رفضه منها يُحذف من هنا، فيُسقط
#  الحارسُ نصوصَه فتُخرَج.
#
#  وثلاثُ صيغٍ زائدةٍ من الأربعين النوويّة: «قال النووي: حديث حسن» و«قال
#  النووي: حديث صحيح» و«قال الترمذي: حديث حسن». وهي **حكمٌ مُسنَدٌ إلى
#  قائله** — أقوى في النسبة لا أضعف. وتطبيعُها إلى «حسن» يمحو مَن حكم،
#  وذاك تغييرٌ في الإسناد لا تبسيطُ حقل.
GRADES = {"صحيح", "حسن", "حسن صحيح", "قرآن", "من كلام أهل العلم",
          "صحيح لغيره", "صحيح بشواهده", "حسن بشواهده", "إسناده صحيح",
          "سنده صحيح", "حسن غريب", "صحيحان", "صحيح وحديث جابر حسن",
          "قال النووي: حديث حسن", "قال النووي: حديث صحيح", "قال الترمذي: حديث حسن"}

# القرآن والتفسير لهما مسارهما وحجمهما، وليسا نصوصاً مفردة بمصدر لكلٍّ منها
SKIP = {"quran_uthmani.json", "tafsir_muyassar.json", "surah_metadata.json"}

# «هل يقرؤه أحد؟» يُفحص على شجرة الكود كلّها لا على الباذر وحده: cities.json
# مثلاً يقرؤه CityListViewModel. الفحص على ملف واحد كان سيبلّغ عنه خطأً.
code = "\n".join(
    f.read_text() for root in KOTLIN for f in root.rglob("*.kt")
)

problems, checked = [], 0

for f in sorted(ASSETS.glob("*.json")):
    if f.name in SKIP:
        continue
    if f'"{f.name}"' not in code:
        problems.append(f"{f.name}: ملف أصول لا يقرؤه أي كود — محتواه لا يراه أحد")
        continue
    items = json.loads(f.read_text())
    if not isinstance(items, list):
        items = sum((v for v in items.values() if isinstance(v, list)), [])
    # الفحص الديني يخصّ ملفات النصوص الدينية وحدها، وعلامتها حقل text_ar
    if not any(isinstance(it, dict) and "text_ar" in it for it in items):
        continue
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
