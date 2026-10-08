#!/usr/bin/env python3
"""
حارسُ تطبيع التسميع — نسختان من قواعدَ واحدة.

`ArabicSearch.normalize` يحذف المسافات، فلا يصلح للتسميع: من حذف
المسافاتَ لم تبقَ له كلماتٌ يقابلها فلا يعرف أيَّ كلمةٍ أخطأ القارئ.
فنُسخت قواعدُه في `Tasmee.tasmeeNormalize` بلا حذف المسافات.

والنسخُ يفترق. ولو غُيّر حرفٌ في جدول `ArabicSearch` وحدَه لصار البحثُ
يطبّع كلمةً والتسميعُ يطبّعها غيرَها — فيُتَّهم قارئٌ بخطأٍ في كتاب الله
سببُه حرفٌ في جدول. فهذا الحارس يُفشل البناءَ إن افترق الجدولان.

ولا يُطلب هنا تشابهُ الدالّتين — إحداهما تحذف المسافات والأخرى تحفظها —
بل تشابهُ **المادّة**: التشكيل، والحروف، وكلماتُ الرسم العثمانيّ.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BASE = ROOT / "shared/src/commonMain/kotlin/app/rafiq/domain/model"
SEARCH = BASE / "ArabicSearch.kt"
TASMEE = BASE / "Tasmee.kt"

PAIR = re.compile(r'"([^"]*)"\s+to\s+"([^"]*)"')


def fail(msg):
    print(f"  ✗ {msg}")
    sys.exit(1)


def diacritics(text, name):
    m = re.search(r'Regex\("(\[[^"]+\])"\)', text)
    if not m:
        fail(f"{name}: لم يُعثَر على تعبير التشكيل")
    return m.group(1)


def pairs(text, start, name):
    i = text.find(start)
    if i < 0:
        fail(f"{name}: لم يُعثَر على {start}")
    j = text.find(")", i)
    return dict(PAIR.findall(text[i:j]))


def main():
    for p in (SEARCH, TASMEE):
        if not p.exists():
            fail(f"ملفٌّ ناقص: {p.relative_to(ROOT)}")

    s = SEARCH.read_text(encoding="utf-8")
    t = TASMEE.read_text(encoding="utf-8")

    if diacritics(s, "ArabicSearch") != diacritics(t, "Tasmee"):
        fail("تعبيرُ التشكيل افترق بين ArabicSearch وTasmee")

    s_letters = pairs(s, "private val LETTERS", "ArabicSearch")
    t_letters = pairs(t, "private val LETTERS", "Tasmee")
    if s_letters != t_letters:
        only_s = set(s_letters.items()) - set(t_letters.items())
        only_t = set(t_letters.items()) - set(s_letters.items())
        fail(f"جدولُ الحروف افترق — في البحث وحدَه {sorted(only_s)} · "
             f"في التسميع وحدَه {sorted(only_t)}")

    s_words = pairs(s, "private val UTHMANI_TO_IMLAEI", "ArabicSearch")
    t_words = pairs(t, "private val UTHMANI_WORDS", "Tasmee")
    if s_words != t_words:
        only_s = set(s_words.items()) - set(t_words.items())
        only_t = set(t_words.items()) - set(s_words.items())
        fail(f"جدولُ الرسم العثمانيّ افترق — في البحث وحدَه {sorted(only_s)} · "
             f"في التسميع وحدَه {sorted(only_t)}")

    #  والحدودُ الثلاثة: وجودُها شرطٌ، فحذفُ أحدها يُسقط بوّابةَ «لم أتبيّن»
    #  كلَّها ولا يُظهر عطباً في أيّ اختبار.
    for gate in ("CONFIDENCE_FLOOR", "LENGTH_FLOOR", "SUSPECT_CEILING"):
        if f"const val {gate}" not in t:
            fail(f"Tasmee.kt: حُذف الحدُّ {gate} — بوّابةُ «لم أتبيّن» ناقصة")

    print(f"طُوبق جدولا التطبيع: {len(s_letters)} حرفاً · "
          f"{len(s_words)} كلمةً · والحدود الثلاثة قائمة · مشاكل 0")


if __name__ == "__main__":
    main()
