#!/usr/bin/env python3
"""
حارس مراسي الجولة التعريفيّة.

═══ العطبُ الذي يصمت ═══

خطوةٌ في `RAFIQ_TOUR` تشير إلى مرساةٍ لا يسجّلها أحد **لا تُسقط بناءً
ولا تُلقي استثناءً**: `SpotlightTour` مكتوبةٌ لتعرض البطاقةَ في الوسط
بلا ثقبٍ إن لم تُقَس المرساة. وذاك سلوكٌ مقصودٌ — البديلُ أن يُثقَب
الحجابُ على مربّعٍ فارغٍ ويُقال «هذا زرُّ كذا» وليس هناك زرّ.

فيصير العطبُ: بطاقةٌ تشرح زرّاً **ولا تشير إليه**. وهو لا يُرى إلّا بفتح
الجولة على الجهاز ومتابعتها خطوةً خطوة — ومن يُعيد ترتيبَ الشريط السفليّ
أو يحذف تبويباً لا يمرّ على ذلك.

ويُمسَك هنا بمقابلةِ ما تطلبه الخطواتُ بما تسجّله `tourAnchor` في الكود.

وبالعكس كذلك: مرساةٌ مسجَّلةٌ لا تطلبها خطوةٌ ليست عطباً — قد تُسجَّل
لجولةٍ قادمة — لكنّها تُطبع تنبيهاً فلا تبقى منسيّةً بلا سبب.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "androidApp/src/main/kotlin"
TOUR = SRC / "app/rafiqaldhikr/ui/components/Spotlight.kt"


def main() -> int:
    text = TOUR.read_text(encoding="utf-8")

    #  الخطواتُ: TourStep("key", ...) داخل RAFIQ_TOUR
    block = re.search(r"val RAFIQ_TOUR[^=]*=\s*listOf\((.*?)\n\)", text, re.S)
    if not block:
        print("  ✗ لم تُعثر قائمةُ RAFIQ_TOUR في Spotlight.kt")
        return 1
    wanted = re.findall(r'TourStep\(\s*"([^"]+)"', block.group(1))

    if not wanted:
        print("  ✗ قائمةُ RAFIQ_TOUR فارغة")
        return 1

    dupes = sorted({k for k in wanted if wanted.count(k) > 1})
    if dupes:
        print(f"  ✗ مرساةٌ مكرَّرةٌ في الخطوات: {' · '.join(dupes)}")
        return 1

    #  المسجَّلة: tourAnchor("key") أو tourAnchor(item.tourKey) ← ومع
    #  الأخيرة تُقرأ القيمُ من حيث تُبنى.
    registered = set()
    for f in SRC.rglob("*.kt"):
        if f == TOUR:
            continue
        body = f.read_text(encoding="utf-8")
        registered.update(re.findall(r'tourAnchor\(\s*"([^"]+)"', body))
        if re.search(r"tourAnchor\(\s*\w+\.tourKey", body):
            #  المرساةُ تُمرَّر حقلاً — فتُجمع قيمُ الحقل من نفس الملفّ
            registered.update(re.findall(r'tourKey\s*=\s*"([^"]+)"', body))
            registered.update(re.findall(r'RafiqRoute\.\w+,\s*"([^"]+)"\)', body))

    missing = [k for k in wanted if k not in registered]
    if missing:
        print("  ✗ خطواتٌ تشير إلى مراسٍ لا يسجّلها أحد:")
        for k in missing:
            print(f"      {k}")
        print("    فتُعرَض بطاقتُها في الوسط بلا ثقب — تشرح زرّاً ولا تشير إليه.")
        return 1

    extra = sorted(registered - set(wanted))
    print(f"  {len(wanted)} خطوةً · {len(registered)} مرساةً مسجَّلة")
    if extra:
        print(f"  · مراسٍ مسجَّلةٌ لا تطلبها خطوة: {' · '.join(extra)}")
    print("مراسي الجولة سليمة.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
