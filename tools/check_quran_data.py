#!/usr/bin/env python3
"""
حارسُ بيانات القرآن.

يفشل البناء إن اختلّ شيءٌ في `quran_uthmani.json` أو `surah_metadata.json`،
أو إن افترق تطبيعُ البحث في بايثون عن تَوأمِه في Kotlin.

ولِمَ حارسٌ لهذا بالذات؟ لأنّ عطبَي هذا الملفّ عاشا في التطبيق ولم يكشفهما
شيء: بحثٌ يعطي صفر نتائج دائماً (‏`text_simple` كان مشكولاً)، وبسملةٌ
ملتصقةٌ بأوّل آيةٍ في ١١٢ سورة. كلاهما بياناتٌ لا كود، فلا مترجمَ يمسكهما
ولا اختبارَ وحدة.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "androidApp/src/main/assets"
KOTLIN = ROOT / "shared/src/commonMain/kotlin/app/rafiq/domain/model/ArabicSearch.kt"

sys.path.insert(0, str(ROOT / "tools"))
from build_quran_assets import searchable, KEEP_WHOLE  # noqa: E402

EXPECTED_AYAT = 6236
EXPECTED_SURAHS = 114
EXPECTED_PAGES = 604
# ١١٤ سورة، إلّا الفاتحة (بسملتُها آيةٌ أولى) والتوبة (بلا بسملة).
EXPECTED_BISMILLAH = 112

problems = []


def fail(msg):
    problems.append(msg)


quran = json.loads((ASSETS / "quran_uthmani.json").read_text(encoding="utf-8"))
surahs = json.loads((ASSETS / "surah_metadata.json").read_text(encoding="utf-8"))

# ═══ العدد والبنية ═══
if len(quran) != EXPECTED_AYAT:
    fail(f"عدد الآيات {len(quran)} والمتوقَّع {EXPECTED_AYAT}")
if len(surahs) != EXPECTED_SURAHS:
    fail(f"عدد السور {len(surahs)} والمتوقَّع {EXPECTED_SURAHS}")

seen = set()
for r in quran:
    key = (r["surah"], r["ayah"])
    if key in seen:
        fail(f"آيةٌ مكرَّرة: {key}")
    seen.add(key)
    for field in ("surah", "ayah", "text_uthmani", "text_simple", "juz", "hizb", "page", "bismillah"):
        if field not in r:
            fail(f"{key}: ينقصه الحقل {field!r}")
    if not (1 <= r["page"] <= EXPECTED_PAGES):
        fail(f"{key}: صفحةٌ خارج المدى: {r['page']}")
    if not (1 <= r["juz"] <= 30):
        fail(f"{key}: جزءٌ خارج المدى: {r['juz']}")

# كلُّ سورةٍ عددُ آياتها كما في البيانات الوصفية
counts = {}
for r in quran:
    counts[r["surah"]] = counts.get(r["surah"], 0) + 1
for s in surahs:
    if counts.get(s["number"]) != s["ayah_count"]:
        fail(f"سورة {s['number']}: {counts.get(s['number'])} آية والوصف يقول {s['ayah_count']}")

# ═══ البسملة ═══
bism = {r["surah"] for r in quran if r["ayah"] == 1 and r["bismillah"]}
if len(bism) != EXPECTED_BISMILLAH:
    fail(f"سورٌ لها بسملةٌ مفصولة: {len(bism)} والمتوقَّع {EXPECTED_BISMILLAH}")
for s in KEEP_WHOLE:
    if s in bism:
        fail(f"سورة {s} لا يجوز فصلُ بسملةٍ منها")

# لا آيةَ يبدأ متنُها بالبسملة بعد الفصل — عدا الفاتحة ١.
BISM_HEAD = re.compile(r"^\s*ب\S*\s+[ٱا]?ل?ل\S*\s+[ٱا]?ل?ر\S*\s+[ٱا]?ل?ر\S*")
for r in quran:
    if (r["surah"], r["ayah"]) == (1, 1):
        continue
    if BISM_HEAD.match(r["text_uthmani"]):
        fail(f"{r['surah']}:{r['ayah']}: ما يزال متنُها يبدأ بالبسملة")

# ═══ عمود البحث ═══
DIAC = re.compile("[ؐ-ًؚ-ٰٟۖ-ۭـ]")
for r in quran:
    if DIAC.search(r["text_simple"]):
        fail(f"{r['surah']}:{r['ayah']}: بقي تشكيلٌ في text_simple")
        break
    if r["text_simple"] != searchable(r["text_uthmani"]):
        fail(f"{r['surah']}:{r['ayah']}: text_simple لا يطابق المولّد — "
             f"شغّل tools/build_quran_assets.py")
        break

# استعلاماتٌ يجب أن تجد نتائج — لو رجع صفرٌ فالبحث معطَّل ثانيةً.
MUST_FIND = ["الله", "الرحمن", "رب العالمين", "الصلاة",
             "يا أيها الذين آمنوا", "قل هو الله أحد"]
for q in MUST_FIND:
    n = sum(1 for r in quran if searchable(q) in r["text_simple"])
    if n == 0:
        fail(f"بحثُ «{q}» يعطي صفر نتائج")

# ═══ توأمُ التطبيع في Kotlin ═══
# التطبيعُ مكتوبٌ مرّتين — هنا وفي الواجهة. لو افترقا لَما التقى استعلامُ
# المستخدم بالعمود المولَّد، وعاد البحثُ إلى الصفر بلا أن يفشل شيء.
if not KOTLIN.exists():
    fail(f"مفقود: {KOTLIN.relative_to(ROOT)} — تطبيعُ الاستعلام في الواجهة")
else:
    src = KOTLIN.read_text(encoding="utf-8")
    for token in ('"ى" to "ي"', '"ة" to "ه"', 'replace("ا", "")', "MIN_QUERY"):
        if token not in src:
            fail(f"ArabicSearch.kt: ينقصه {token} — افترق عن مولّد بايثون")

print(f"فُحصت {len(quran)} آية · {len(surahs)} سورة · "
      f"{len(bism)} بسملة مفصولة · مشاكل {len(problems)}")
if problems:
    for p in problems[:20]:
        print("  ✗ " + p)
    sys.exit(1)
print("بيانات القرآن سليمة.")
