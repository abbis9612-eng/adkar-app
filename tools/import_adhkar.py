#!/usr/bin/env python3
"""
مستورِدُ أذكار الصباح والمساء من قاعدةٍ مرخَّصةٍ بـMIT.

المصدر: https://github.com/Seen-Arabic/Morning-And-Evening-Adhkar-DB
الرخصة: MIT © 2024 Seen Arabic — نسخةٌ منها في `tools/licenses/`.

═══ لماذا هذا المصدر بالذات ═══

بحثتُ في القوائم المنشورة كلِّها فوجدتُها إمّا بلا ترخيصٍ معلَن، وإمّا بلا
تخريج، وإمّا يقول صاحبُها صراحةً إنّ بياناته غير محقَّقة. وهذا وحدَه يجمع
الثلاثة: رخصةٌ صريحة، وتخريجٌ كاملٌ بأرقام الأحاديث، وفضلٌ لكل ذكر.

═══ ما لا يفعله هذا السكربت ═══

**لا يحرّر نصّاً عربياً ولا يختصره ولا يعيد صوغَه.** النصُّ والتخريجُ
والفضلُ تُنقل حرفاً بحرف. وقاعدةُ AGENTS.md صريحة في هذا.

**ولا يخترع درجةً لحديث.** الدرجةُ تُشتقّ بقاعدتين لا ثالثة لهما:

  ١) ما أخرجه البخاريُّ أو مسلمٌ فهو صحيحٌ بالاتّفاق — وهذا ليس اجتهاداً
     منّي بل إجماعُ أهل الحديث على صحّة ما في الصحيحين.
  ٢) وما عداه: يُقرأ لفظُ الحكم من نصّ التخريج نفسِه («وصححه الألباني»،
     «وحسّنه»). فإن لم يُذكر حكمٌ صريح **يُترك الذكرُ ولا يُستورَد** —
     ولا يُخمَّن.

ويطبع السكربت ما تركه ولماذا، فلا يمرّ نقصٌ صامتاً.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "androidApp/src/main/assets"

#  ══ ١) التضعيفُ يسبق كلَّ شيء ══
#
#  إحالةٌ إلى «ضعيف الترغيب» أو «السلسلة الضعيفة» تعني أنّ الشيخ ضعّفه،
#  ولو ذُكر في الموضع نفسِه إسنادٌ «جيّد» عند غيره. فيُترك ولا يُستورَد.
#  وهذا ليس فرضاً نظرياً: في هذه القاعدة حديثٌ (رقم ٢٩) يقول تخريجُه
#  «بإسنادين أحدهما جيّد» ثمّ يُحيل إلى الضعيفة برقم ٥٧٨٨.
WEAK = re.compile(r"ضعيف\s+الترغيب|السلسلة\s+الضعيفة|ضعّفه|ضعفه|ضعيف\s+الجامع|إسناده\s+ضعيف")

#  ══ ٢) ما في الصحيحين ══
#  صحيحٌ بالاتّفاق — إجماعُ أهل الحديث لا اجتهادٌ منّي.
SAHIHAYN = re.compile(r"(?:صحيح\s+)?(?:ال)?(?:بخاري|مسلم)\s*[:،(]|متفق\s+عليه")

#  ══ ٣) كتبُ الألباني المخصَّصة للصحيح ══
#  إحالةُ حديثٍ إلى «صحيح الترمذي» أو «صحيح أبي داود» أو «صحيح الجامع»
#  تخريجٌ لحكمه: تلك الكتبُ لا تضمّ إلّا ما صحّحه.
ALBANI_SAHIH = re.compile(
    r"صحيح\s+(?:الترمذي|أبي\s+داود|ابن\s+ماجه|النسائي|الجامع|الترغيب|الأدب)"
    r"|السلسلة\s+الصحيحة"
)

GRADE_SAHIH = re.compile(r"صحّح|صححه|وصححه|صحيح\s+الإسناد|إسناده\s+صحيح")
GRADE_HASAN = re.compile(
    r"حسّن|حسنه|وحسنه|حسن\s+إسناده|حسّن\s+إسناده|حسن\s+الإسناد|إسناده\s+حسن"
)


def grade_of(source: str) -> str | None:
    """
    درجةُ الحديث، أو None إن لم تُعرف.

    الترتيبُ مقصود: التضعيفُ يُفحص أوّلاً فيُسقط الحديثَ مهما جاء بعده.
    """
    if WEAK.search(source):
        return None
    if SAHIHAYN.search(source) or ALBANI_SAHIH.search(source) or GRADE_SAHIH.search(source):
        return "صحيح"
    if GRADE_HASAN.search(source):
        return "حسن"
    return None


def clean(s: str) -> str:
    """مسافاتٌ موحّدة لا غير — لا يُحذف حرفٌ ولا تُغيَّر كلمة."""
    return re.sub(r"\s+", " ", s or "").strip()


def main() -> int:
    if len(sys.argv) != 2:
        print("الاستعمال: import_adhkar.py <مجلَّد فيه ar.json و en.json>")
        return 2
    src = pathlib.Path(sys.argv[1])
    ar = json.loads((src / "ar.json").read_text(encoding="utf-8"))
    en = json.loads((src / "en.json").read_text(encoding="utf-8"))
    en_by_order = {x["order"]: x for x in en}

    morning, evening, skipped = [], [], []

    for row in ar:
        source = clean(row["source"])
        grade = grade_of(source)
        if grade is None:
            skipped.append((row["order"], clean(row["content"])[:48], source[:70]))
            continue

        #  الترجمةُ في `translation` لا في `content` — الأخيرُ عربيٌّ في
        #  الملفَّين معاً (نصُّ الذكر لا يُترجَم، إنّما يُشرح).
        e = en_by_order.get(row["order"], {})
        record = {
            "text_ar": clean(row["content"]),
            "text_en": clean(e.get("translation", "")),
            "translit": clean(e.get("transliteration", "")),
            "source": source,
            "source_grade": grade,
            "virtue": clean(row.get("fadl", "")),
            "virtue_en": clean(e.get("fadl", "")),
            "count": int(row.get("count") or 1),
        }
        # type: 0 = الوقتان · 1 = الصباح · 2 = المساء
        if row["type"] in (0, 1):
            morning.append(dict(record))
        if row["type"] in (0, 2):
            evening.append(dict(record))

    for name, rows in (("morning", morning), ("evening", evening)):
        for i, r in enumerate(rows, start=1):
            r["sort_order"] = i
        path = ASSETS / f"adhkar_{name}.json"
        path.write_text(
            json.dumps(rows, ensure_ascii=False, indent=1) + "\n", encoding="utf-8"
        )
        print(f"{path.name}: {len(rows)} ذكراً")

    if skipped:
        print(f"\nتُرك {len(skipped)} — لا حكمَ صريحاً ولا إخراجَ في الصحيحين:")
        for order, text, source in skipped:
            print(f"  · [{order}] {text}…\n      {source}…")
        print("  (لا يُخمَّن حكمُ حديث. تُضاف يدوياً إن حُقّقت.)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
