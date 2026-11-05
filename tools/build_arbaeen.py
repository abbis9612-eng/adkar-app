#!/usr/bin/env python3
"""
جالبُ الأربعين النوويّة.

═══ وكيف عُرف **أيُّ** اثنين وأربعين ═══

هذا هو السؤالُ الذي أوقف البناءَ دورتين. والجوابُ لم يُكتب من ذاكرة:

أرسل صاحبُ التطبيق نسخةَ **«متن الأربعين النووية» (دار السلام)** من
`islamhouse.com`. والـPDF مصوَّرٌ لا نصَّ فيه، ومعه على المسار نفسِه
ملفُّ Word نصّيٌّ كامل. فاستُخرج بجدول القطع (CLX/PlcPcd) لا بتخمين،
فجاء ٢٨١٤٩ محرفاً **بلا محرفِ بدلٍ واحد**.

وكشف أمراً: النسخةُ فيها **خمسون** حديثاً لا اثنان وأربعون — الأربعون
النوويّة ثمّ تتمّةُ الخمسين. وآخرُ الأربعين هو الحديثُ الثاني والأربعون
«يا ابن آدم إنك ما دعوتني ورجوتني»، وما بعده تتمّة. وهذا يفسّر ما حيّرني
قبلاً: الموسوعةُ تذكر في التخريج «شرح الأربعين وتتمة الخمسين»، فظننتُ
الوسمَ عضويّةً فأعطاني خمسين لا اثنين وأربعين.

**والحديثُ الخمسون «لا يزال لسانك رطبا من ذكر الله»** — وهو الذي شككتُ
في عضويّته فأوقفتُ البناءَ من أجله. وكان الشكُّ صحيحاً.

═══ وما يُشحن، وما لا يُشحن ═══

نسخةُ دار السلام فيها **تشكيلٌ وترقيمُ تخريجٍ** من عمل محقّقيها، ولا
إذنَ معلَناً بإعادة نشرها. والمتنُ نفسُه في الملك العامّ (النوويّ ت ٦٧٦هـ)،
أمّا تلك الزيادةُ فلا.

فتُستعمل النسخةُ **لتعيين الاثنين والأربعين وترتيبِهم** — وذلك حقيقةٌ عن
كتابٍ لا نشرٌ لنسخة — ويُشحن المتنُ والدرجةُ والتخريجُ والشرحُ من
**الموسوعة الحديثيّة** التي تُجيز إعادةَ النشر بشروطها.

═══ وكيف رُبط كلُّ حديثٍ برقمه عند الموسوعة ═══

بثلاث قواعدَ مرتَّبةِ القوّة، وكلُّها على النصّ المطبَّع:

  ١. تطابقُ العنوان حرفاً بعد التطبيع — أقوى دليل.
  ٢. عنوانُ الموسوعة مقطعٌ من متن النسخة (الأطولُ أولى).
  ٣. أطولُ مقطعٍ مشترك ≥ ٢٥ محرفاً وبفارقٍ ≥ ٦ عن الثاني.

وحيث تطابق عنوانا مرشَّحَين فهما **مدخلان لحديثٍ واحد** في الموسوعة،
فالاختيارُ بينهما اختيارُ مدخلٍ لا اختيارُ حديث.

وأُسقطت قاعدةٌ رابعةٌ كانت ستُخطئ: المرشَّحُ الأعلى تطابقاً للحديث
الرابع عشر كان **حديثاً آخرَ يشبهه لفظاً** (٥٨١٩٤)، والصحيحُ ٤٧١٤
بتطابقٍ أقلّ. فلولا تقديمُ قاعدة العنوان على قاعدة الطول لدخل الخطأ.

═══ وما يُسقط هذه الأداة ═══

نصٌّ بلا درجةٍ أو بلا تخريج، أو رقمٌ لا يرجع بحديث، أو عددٌ غيرُ ٤٢.
ولا يُحشى ناقصٌ بتقدير.
"""
import json
import pathlib
import sys
import time
import urllib.error
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "androidApp/src/main/assets/arbaeen.json"
CACHE = ROOT / "tools/.athar_cache"

BASE = "https://hadeethenc.com/api/v1"
PUBLISHER = "HadeethEnc.com"

#  الترتيبُ في «متن الأربعين النووية» ← رقمُ الحديث عند الموسوعة.
#  مستخرَجٌ بالقواعد الموصوفة أعلاه، ٤٢ رقماً فريداً بلا تكرار.
MAP = {
    1: "4560", 2: "4563", 3: "65000", 4: "66513", 5: "66514", 6: "4314",
    7: "4309", 8: "4211", 9: "4725", 10: "66518", 11: "66519", 12: "65255",
    13: "4717", 14: "4714", 15: "5437", 16: "4709", 17: "4319", 18: "4302",
    19: "66522", 20: "4559", 21: "65018", 22: "66525", 23: "66526",
    24: "4810", 25: "4558", 26: "4568", 27: "4308", 28: "66529",
    29: "66530", 30: "66510", 31: "4307", 32: "66531", 33: "66532",
    34: "65001", 35: "4706", 36: "4801", 37: "66533", 38: "66534",
    39: "4216", 40: "4704", 41: "66535", 42: "5456",
}

ALLOWED = {"صحيح", "حسن", "حسن صحيح", "قرآن", "من كلام أهل العلم",
           "صحيح لغيره", "صحيح بشواهده", "حسن بشواهده", "إسناده صحيح",
           "سنده صحيح", "حسن غريب", "صحيحان", "صحيح وحديث جابر حسن",
           "قال النووي: حديث حسن", "قال النووي: حديث صحيح", "قال الترمذي: حديث حسن"}


def fail(msg: str):
    print(f"  ✗ {msg}")
    sys.exit(1)


def fetch(path: str, optional: bool = False):
    url = f"{BASE}/{path}"
    for i in range(4):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "rafiq-aldhikr/1.0"})
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read())
        except urllib.error.HTTPError as e:
            if e.code == 404 and optional:
                return None
            time.sleep(2 ** i)
        except Exception:  # noqa: BLE001
            time.sleep(2 ** i)
    if optional:
        return None
    fail(f"تعذّر الجلب: {url}")


def cached(key: str, path: str, optional: bool = False) -> dict:
    CACHE.mkdir(exist_ok=True)
    f = CACHE / f"{key}.json"
    if f.exists():
        return json.loads(f.read_text(encoding="utf-8"))
    d = fetch(path, optional=optional)
    if d is None:
        return {}
    if not isinstance(d, dict):
        fail(f"جوابٌ غيرُ متوقَّع لـ{key}")
    f.write_text(json.dumps(d, ensure_ascii=False), encoding="utf-8")
    time.sleep(0.12)
    return d


def main() -> int:
    if len(MAP) != 42 or sorted(MAP) != list(range(1, 43)):
        fail(f"الخريطةُ ليست ١..٤٢: {len(MAP)}")
    if len(set(MAP.values())) != 42:
        fail("أرقامٌ مكرَّرةٌ في الخريطة")

    print(f"المصدر: {PUBLISHER} · {len(MAP)} حديثاً")
    rows, grades, no_en = [], {}, []

    for n in sorted(MAP):
        hid = MAP[n]
        ar = cached(f"ar_{hid}", f"hadeeths/one/?language=ar&id={hid}")
        if not ar.get("hadeeth"):
            fail(f"الحديث {n} (رقم {hid}): لا متنَ في الجواب")
        has_en = "en" in (ar.get("translations") or [])
        en = cached(f"en_{hid}", f"hadeeths/one/?language=en&id={hid}",
                    optional=True) if has_en else {}
        if not (en.get("hadeeth") or "").strip():
            no_en.append(n)

        grade = (ar.get("grade") or "").strip()
        attribution = (ar.get("attribution") or "").strip()
        reference = (ar.get("reference") or "").strip()
        if not grade:
            fail(f"الحديث {n} (رقم {hid}): بلا درجة")
        if not (attribution or reference):
            fail(f"الحديث {n} (رقم {hid}): بلا تخريج")
        if grade not in ALLOWED:
            fail(f"الحديث {n}: درجةٌ لا يعرفها الحارس «{grade}»")
        grades[grade] = grades.get(grade, 0) + 1

        rows.append({
            "n": n,
            "category": "arbaeen",
            "title": (ar.get("title") or "").strip(),
            "title_en": (en.get("title") or "").strip(),
            "text_ar": ar["hadeeth"].strip(),
            "text_en": (en.get("hadeeth") or "").strip(),
            "source": "\n".join(p for p in (attribution, reference) if p),
            "source_grade": grade,
            "virtue": (ar.get("explanation") or "").strip(),
            "virtue_en": (en.get("explanation") or "").strip(),
            "hadeeth_id": hid,
            "sort_order": n,
        })

    doc = {
        "publisher": PUBLISHER,
        "publisher_url": "https://hadeethenc.com",
        "enumeration": "متن الأربعين النووية — نسخة دار السلام (islamhouse.com)",
        "fetched_on": time.strftime("%Y-%m-%d"),
        "terms": (
            "يتاح تنزيل المحتوى وإعادة نشره بشرط: عدم التعديل أو الإضافة أو "
            "الحذف، والإشارة الواضحة للناشر والمصدر، وذكر رقم الإصدار."
        ),
        "items": rows,
    }
    OUT.write_text(json.dumps(doc, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")

    print("\nالدرجات:")
    for g, c in sorted(grades.items(), key=lambda kv: -kv[1]):
        print(f"  {c:3d}  {g}")
    print(f"\nكُتب {len(rows)} حديثاً في {OUT.relative_to(ROOT)} "
          f"({OUT.stat().st_size / 1024:.0f} ك.ب)")
    if no_en:
        print(f"  · {len(no_en)} بلا ترجمةٍ إنجليزيّةٍ منشورة — تُعرَض عربيّةً")
    return 0


if __name__ == "__main__":
    sys.exit(main())
