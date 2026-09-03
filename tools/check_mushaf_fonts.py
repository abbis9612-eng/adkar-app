#!/usr/bin/env python3
"""
حارسُ خطوط المصحف — الثمانيةُ والأربعون في الحزمة وكلُّها خطوطٌ صالحة.

الصفحةُ المصحفية لا تُرسم بنصٍّ عاديّ: كلُّ كلمةٍ رمزٌ في منطقة الاستعمال
الخاصّ من خطِّ صفحتها. فإن غاب خطٌّ واحدٌ خرجت ثلاثَ عشرةَ صفحةً مربّعاتٍ
فارغة — لا رسالةَ خطأ ولا تحذير، صفحةُ مصحفٍ بيضاء.

وكان هذا الحارسُ يفتح الشبكةَ ليسأل: أما زالت الخطوطُ على المستودع؟ وقد
صارت في `assets/mushaf/`، فالسؤالُ تبدّل: **أهي كاملةٌ وسليمةٌ في الشجرة؟**
وهو سؤالٌ أهمّ، لأنّ جوابَه لا يعتمد على خادمٍ لا نملكه، ولأنّ ملفّاً
تالفاً هنا يُشحن إلى كلّ مستخدم.

ويُدار مع بقيّة الحرّاس — لا إنترنتَ فيه بعد اليوم.
"""
import json
import pathlib
import struct
import sys

ROOT   = pathlib.Path(__file__).resolve().parent.parent
LAYOUT = ROOT / "androidApp/src/main/assets/mushaf_layout.json"
FONTS  = ROOT / "androidApp/src/main/assets/mushaf"

#  أدنى حجمٍ معقولٍ لخطّ صفحات: أصغرُها المقيسُ ١٫٥ م.ب.
MIN_BYTES = 700_000
NEEDED = {"cmap", "glyf", "head", "hmtx"}


def tables_of(data: bytes) -> set[str]:
    """أسماءُ جداول الخطّ — بها يُعرف الملفُّ خطّاً لا بايتاتٍ عشوائية."""
    if len(data) < 12:
        return set()
    n = struct.unpack(">H", data[4:6])[0]
    if 12 + 16 * n > len(data):
        return set()
    return {data[12 + 16 * i: 16 + 16 * i].decode("latin1", "replace") for i in range(n)}


def main() -> int:
    fonts = json.loads(LAYOUT.read_text(encoding="utf-8"))["fonts"]
    print(f"الخطوط المطلوبة: {len(fonts)} · المجلَّد: assets/mushaf/")

    bad, total = [], 0
    for name in fonts:
        f = FONTS / f"{name}.ttf"
        if not f.exists():
            bad.append((name, "غيرُ موجود"))
            continue
        data = f.read_bytes()
        total += len(data)
        if len(data) < MIN_BYTES:
            bad.append((name, f"حجمٌ مريب: {len(data)} بايت"))
            continue
        #  و`cmap` بالذات: بها تُترجم رموزُ الاستعمال الخاصّ إلى كلمات،
        #  وبدونها يُقرأ الملفُّ خطّاً ويرسم فراغاً.
        missing = NEEDED - tables_of(data)
        if missing:
            bad.append((name, f"جداولُ ناقصة: {sorted(missing)}"))

    #  ملفّاتٌ زائدةٌ في المجلَّد تُشحن بلا أن تُستعمل.
    known = {f"{n}.ttf" for n in fonts}
    extra = sorted(p.name for p in FONTS.glob("*.ttf") if p.name not in known)

    print(f"المجموع: {total / 1_048_576:.1f} ميغابايت · مشاكل {len(bad) + len(extra)}")
    if bad:
        print("\nخطوطٌ ناقصةٌ أو تالفة — صفحاتُها تخرج فارغة:")
        for name, why in bad:
            print(f"  ✗ {name}: {why}")
    if extra:
        print("\nملفّاتٌ لا يطلبها التخطيط — تُشحن بلا فائدة:")
        for name in extra:
            print(f"  ✗ {name}")
    if bad or extra:
        return 1

    print("الثمانيةُ والأربعون كلُّها حاضرةٌ وسليمة.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
