#!/usr/bin/env python3
"""
حارسُ خطوط المصحف — الملفّات الثمانيةُ والأربعون تُنزَّل وتُقرأ خطوطاً صالحة.

الصفحةُ المصحفية لا تُرسم بنصٍّ عاديّ: كلُّ كلمةٍ رمزٌ في منطقة الاستعمال
الخاصّ من خطِّ صفحتها. فإن غاب خطٌّ واحدٌ خرجت ثلاثَ عشرةَ صفحةً مربّعاتٍ
فارغة — لا رسالةَ خطأ ولا تحذير، صفحةُ مصحفٍ بيضاء.

والخطوطُ ليست في الحزمة: تسعون ميغابايت تُنزَّل من مستودعٍ **على الشبكة
لا نملكه**. فإن حُذف أو أُعيد ترتيبُه، مات المصحفُ عند كلّ من لم يُنزّل
بعد — ولا يظهر ذلك في بناءٍ ولا في اختبارٍ يعمل بلا شبكة.

هذا الحارسُ يفتح الشبكةَ عمداً ليقول: أما زالت الثمانيةُ والأربعون هناك؟

    python3 tools/check_mushaf_fonts.py          # الرؤوس فقط (سريع)
    python3 tools/check_mushaf_fonts.py --deep   # يُنزّل ويتحقّق من الجداول

وهو **خارج `verify.sh`** عمداً: لا يُشترط إنترنتٌ لدفع تغييرٍ في الكود.
يُدار قبل الإصدار، وفي أيّ وقتٍ يُشكّ فيه بالمصدر.
"""
import json
import pathlib
import struct
import sys
import urllib.error
import urllib.request

ROOT   = pathlib.Path(__file__).resolve().parent.parent
LAYOUT = ROOT / "androidApp/src/main/assets/mushaf_layout.json"

#  المصدرُ نفسُه الذي في `MushafDownloader.urlFor` — يُقرأ من الكود لا
#  يُكتب هنا ثانيةً، كي لا يفترقا حين يُبدَّل المستودع.
DOWNLOADER = ROOT / "androidApp/src/main/kotlin/app/rafiqaldhikr/ui/mushaf/MushafDownloader.kt"


def base_url() -> str:
    """يستخرج قاعدةَ العنوان من الكود — مصدرٌ واحدٌ للحقيقة."""
    src = DOWNLOADER.read_text(encoding="utf-8")
    # القيمةُ قد تكون على السطر التالي للإعلان — يُقرأ أوّلُ نصٍّ بعده.
    i = src.find("FONT_BASE")
    if i < 0:
        sys.exit("✗ لم أجد FONT_BASE في MushafDownloader.kt")
    j = src.find('"', i)
    return src[j + 1: src.find('"', j + 1)]


def url_for(base: str, name: str) -> str:
    # خطوطُ المتن وحدَها تحمل لاحقةَ `_W`؛ ولوحُ السور `QCF4_QBSML.ttf` مجرّداً.
    f = f"{name}_W.ttf" if name.startswith("QCF4_Hafs") else f"{name}.ttf"
    return f"{base}{f}"


def tables_of(data: bytes) -> set[str]:
    """أسماءُ جداول الخطّ — بها يُعرف الملفُّ خطّاً لا صفحةَ خطأ بـ200."""
    if len(data) < 12:
        return set()
    n = struct.unpack(">H", data[4:6])[0]
    if 12 + 16 * n > len(data):
        return set()
    return {data[12 + 16 * i: 16 + 16 * i].decode("latin1") for i in range(n)}


def main() -> int:
    deep  = "--deep" in sys.argv
    base  = base_url()
    fonts = json.loads(LAYOUT.read_text(encoding="utf-8"))["fonts"]

    print(f"المصدر: {base}")
    print(f"الخطوط المطلوبة: {len(fonts)}" + ("  ·  فحصٌ عميق" if deep else ""))

    bad, total = [], 0
    for name in fonts:
        url = url_for(base, name)
        try:
            req = urllib.request.Request(url, method="GET" if deep else "HEAD")
            with urllib.request.urlopen(req, timeout=45) as r:
                if r.status != 200:
                    bad.append((name, f"HTTP {r.status}"))
                    continue
                if deep:
                    body = r.read()
                    total += len(body)
                    #  الجداولُ الأربعةُ التي لا يقوم خطٌّ بدونها. و`cmap`
                    #  بالذات: بها تُترجم رموزُ الاستعمال الخاصّ إلى كلمات،
                    #  وبدونها يُقرأ الملفُّ خطّاً ويرسم فراغاً.
                    t = tables_of(body)
                    missing = {"cmap", "glyf", "head", "hmtx"} - t
                    if missing:
                        bad.append((name, f"جداولُ ناقصة: {sorted(missing)}"))
                    elif len(body) < 100_000:
                        bad.append((name, f"حجمٌ مريب: {len(body)} بايت"))
                else:
                    n = int(r.headers.get("Content-Length") or 0)
                    total += n
                    if n < 100_000:
                        bad.append((name, f"حجمٌ مريب: {n} بايت"))
        except urllib.error.HTTPError as e:
            bad.append((name, f"HTTP {e.code}"))
        except Exception as e:                       # noqa: BLE001
            bad.append((name, type(e).__name__))

    print(f"المجموع: {total / 1_048_576:.1f} ميغابايت · مشاكل {len(bad)}")
    if bad:
        print("\nخطوطٌ لا تُجلَب — الصفحةُ التي تحتاجها تخرج فارغة:")
        for name, why in bad:
            print(f"  ✗ {name}: {why}")
        print("\nإن سقط المصدرُ كلُّه: انسخ الملفّات إلى مستودعك وبدّل")
        print("FONT_BASE في MushafDownloader.kt — سطرٌ واحد.")
        return 1

    print("الثمانيةُ والأربعون كلُّها تُجلَب.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
