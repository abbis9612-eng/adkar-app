#!/usr/bin/env python3
"""
جالبُ «الأثر» — أحاديثُ الذكر والدعاء بأدلّتها.

═══ ما هذا، وما ليس ═══

**ليس أذكاراً تُقرأ.** أذكارُ التطبيق (`adhkar_*.json`) نصوصٌ يقولها
القارئُ: «الحمد لله وحده…». وهذا **أحاديثُها**: «مَنْ قَرَأَ بِالْآيَتَيْنِ
مِنْ آخِرِ سُورَةِ الْبَقَرَةِ فِي لَيْلَةٍ كَفَتَاهُ» — دليلُ الذكر
وفضلُه، لا الذكرُ نفسُه.

ولو أُدرج هذا في «أذكار الصباح» لقرأه القارئُ على أنّه ذكرٌ يقال، وهو
خبرٌ عن فضل. فله بابُه.

═══ المصدرُ وشروطُه ═══

`hadeethenc.com` — الموسوعةُ الحديثيّة، بواجهةِ برمجةٍ رسميّة. وشروطُ
النشر معلنةٌ على الموقع، وأوّلُها **«عدم التعديل أو الإضافة أو الحذف على
المحتوى»** — وهو قانونُ `AGENTS.md` نفسُه. ومعها: الإشارةُ الواضحةُ
للناشر والمصدر، وذكرُ رقم الإصدار، وأن لا تُضمَّن إعلاناتٌ لا تليق.

فيُخزَّن مع كلّ نصٍّ **رقمُه عند المصدر** وتاريخُ الجلب، ويُعرَضان في
الواجهة. ولا يُعاد صوغُ حرف.

═══ وما يُسقط هذه الأداةَ ولا تُكمل ═══

١) **درجةٌ خارج ما يقبله `check_religious_sources.py`.** فلو جاءت درجةٌ
   لا يعرفها الحارسُ لم تُضَف إلى قائمته صامتاً: تُطبع ويُسقط الجلب،
   ويُعرَض على المستخدم قرارٌ بإضافتها. فالدرجةُ حكمٌ شرعيٌّ لا حقلُ
   تصنيف.

٢) **نصٌّ بلا درجةٍ أو بلا تخريج.** قاعدةُ المشروع: لا نصَّ دينيٌّ بلا
   مصدرٍ موثَّق. فالناقصُ لا يُحشى بفراغٍ ولا يُقدَّر.

٣) **فرقٌ في العدد** بين ما تقوله فهرسةُ المصدر وما وصل فعلاً — فنصٌّ
   سقط في الشبكة لا يمرّ بصمت.
"""
import json
import pathlib
import sys
import time
import urllib.error
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "androidApp/src/main/assets/athar.json"
CACHE = ROOT / "tools/.athar_cache"

BASE = "https://hadeethenc.com/api/v1"
PUBLISHER = "HadeethEnc.com"

#  الدرجاتُ التي يقبلها `check_religious_sources.py` اليوم
ALLOWED_AR = {"صحيح", "حسن", "حسن صحيح", "قرآن", "من كلام أهل العلم",
          "صحيح لغيره", "صحيح بشواهده", "حسن بشواهده", "إسناده صحيح",
          "سنده صحيح", "حسن غريب", "صحيحان", "صحيح وحديث جابر حسن",
           "قال النووي: حديث حسن", "قال النووي: حديث صحيح", "قال الترمذي: حديث حسن"}

#  (رقمُ البابِ عند المصدر، مفتاحُنا) — والمفتاحُ يُترجَم في الموارد
CATEGORIES = [
    ("278", "fadl_dhikr"),
    ("279", "fadl_dua"),
    ("299", "hady_dhikr"),
    ("300", "fawaid_dhikr"),
    ("301", "morning_evening"),
    ("302", "mutlaq"),
    ("303", "home"),
    ("305", "khala"),
    ("306", "masjid"),
    ("307", "shidda"),
    ("308", "arida"),
    ("309", "ahkam_dua"),
    ("310", "anwa_dua"),
    ("311", "adab_dua"),
    ("312", "ijaba"),
    ("313", "mathura"),
    ("465", "salah"),
]


def fail(msg: str):
    print(f"  ✗ {msg}")
    sys.exit(1)


def fetch(path: str, tries: int = 4) -> dict | list:
    """يجلب مع إعادةٍ متباطئة — وانقطاعُ شبكةٍ لا يُنتج ملفّاً ناقصاً."""
    url = f"{BASE}/{path}"
    last = None
    for i in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "rafiq-aldhikr/1.0"})
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read())
        except Exception as e:  # noqa: BLE001
            last = e
            time.sleep(2 ** i)
    fail(f"تعذّر الجلب بعد {tries} محاولات: {url}\n    {last}")


def cached(key: str, path: str, required: bool = True) -> dict:
    """
    يُخزَّن الجوابُ على القرص — فإعادةُ التشغيل لا تُثقل المصدر.

    و`required=False` تُرجع فارغاً عند ٤٠٤ ولا تُسقط الأداة: الترجمةُ
    الإنجليزيّةُ لا توجد لكلّ حديث، **ولا يُسقَط نصٌّ عربيٌّ موثَّقٌ لأنّ
    ترجمتَه لم تُنشر**.
    """
    CACHE.mkdir(exist_ok=True)
    f = CACHE / f"{key}.json"
    if f.exists():
        return json.loads(f.read_text(encoding="utf-8"))
    if required:
        d = fetch(path)
    else:
        d = fetch_optional(path)
        if d is None:
            return {}
    if not isinstance(d, dict):
        fail(f"جوابٌ غيرُ متوقَّع لـ{key}: {str(d)[:120]}")
    f.write_text(json.dumps(d, ensure_ascii=False), encoding="utf-8")
    time.sleep(0.15)
    return d


def fetch_optional(path: str) -> dict | None:
    """كـ[fetch] إلّا أنّ ٤٠٤ ليست عطباً — ترجمةٌ غيرُ موجودة."""
    url = f"{BASE}/{path}"
    for i in range(3):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "rafiq-aldhikr/1.0"})
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read())
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return None
            time.sleep(2 ** i)
        except Exception:  # noqa: BLE001
            time.sleep(2 ** i)
    return None


def ids_of(category: str) -> list[str]:
    out, page, expected = [], 1, None
    while True:
        d = fetch(f"hadeeths/list/?language=ar&category_id={category}&page={page}&per_page=50")
        if not isinstance(d, dict):
            fail(f"فهرسةُ الباب {category} جاءت بصورةٍ غيرِ متوقَّعة")
        expected = expected or int(d["meta"]["total_items"])
        out += [r["id"] for r in d.get("data", [])]
        if page >= int(d["meta"]["last_page"]):
            break
        page += 1
        time.sleep(0.15)
    if len(out) != expected:
        fail(f"الباب {category}: وصل {len(out)} والفهرسةُ تقول {expected}")
    return out


def main() -> int:
    print(f"المصدر: {PUBLISHER}  ({len(CATEGORIES)} باباً)")
    rows, grades, missing, no_english = [], {}, [], []

    for cat, key in CATEGORIES:
        ids = ids_of(cat)
        for hid in ids:
            ar = cached(f"ar_{hid}", f"hadeeths/one/?language=ar&id={hid}")
            #  الإنجليزيّةُ تُطلب إن أعلن المصدرُ أنّها منشورة، وتُحتمَل
            #  ٤٠٤ على كلّ حال: الفهرسةُ تسبق النشرَ أحياناً.
            has_en = "en" in (ar.get("translations") or [])
            en = cached(f"en_{hid}", f"hadeeths/one/?language=en&id={hid}",
                        required=False) if has_en else {}
            if not (en.get("hadeeth") or "").strip():
                no_english.append(hid)

            text = (ar.get("hadeeth") or "").strip()
            grade = (ar.get("grade") or "").strip()
            attribution = (ar.get("attribution") or "").strip()
            reference = (ar.get("reference") or "").strip()

            if not text or not grade or not (attribution or reference):
                missing.append((hid, bool(text), bool(grade), bool(attribution or reference)))
                continue

            grades[grade] = grades.get(grade, 0) + 1

            #  المصدرُ عندنا = نسبةُ الحديث ثمّ تخريجُه بالجزء والصفحة
            #  والرقم. يُجمعان بسطرٍ فاصلٍ ولا يُعاد صوغُ أيٍّ منهما.
            source = "\n".join(p for p in (attribution, reference) if p)

            rows.append({
                "category": key,
                "title": (ar.get("title") or "").strip(),
                "title_en": (en.get("title") or "").strip(),
                "text_ar": text,
                "text_en": (en.get("hadeeth") or "").strip(),
                "source": source,
                "source_grade": grade,
                "virtue": (ar.get("explanation") or "").strip(),
                "virtue_en": (en.get("explanation") or "").strip(),
                "hadeeth_id": hid,
                "sort_order": len(rows) + 1,
            })

    if missing:
        for m in missing[:10]:
            print(f"  ✗ {m[0]}: نصّ={m[1]} درجة={m[2]} تخريج={m[3]}")
        fail(f"{len(missing)} نصّاً ناقصَ الإسناد — ولا يُحشى الناقصُ بتقدير")

    unknown = sorted(set(grades) - ALLOWED_AR)
    print("\nالدرجاتُ الواردة:")
    for g, n in sorted(grades.items(), key=lambda kv: -kv[1]):
        mark = "✓" if g in ALLOWED_AR else "✗ غيرُ معروفة"
        print(f"  {n:4d}  {g:28s} {mark}")
    if unknown:
        fail(
            "درجاتٌ لا يعرفها الحارس: " + " · ".join(unknown)
            + "\n    ولا تُضاف إلى قائمته صامتاً — الدرجةُ حكمٌ شرعيٌّ لا حقلُ تصنيف."
            + "\n    فإن قُبلت، تُضاف إلى GRADES في tools/check_religious_sources.py"
            + "\n    وإلى ALLOWED_AR هنا، بقرارٍ معلَن."
        )

    doc = {
        "publisher": PUBLISHER,
        "publisher_url": "https://hadeethenc.com",
        "fetched_on": time.strftime("%Y-%m-%d"),
        "terms": (
            "يتاح تنزيل المحتوى وإعادة نشره بشرط: عدم التعديل أو الإضافة أو "
            "الحذف، والإشارة الواضحة للناشر والمصدر، وذكر رقم الإصدار."
        ),
        "items": rows,
    }
    OUT.write_text(json.dumps(doc, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"\nكُتب {len(rows)} نصّاً في {OUT.relative_to(ROOT)} "
          f"({OUT.stat().st_size / 1024:.0f} ك.ب)")
    if no_english:
        print(f"  · {len(no_english)} نصّاً بلا ترجمةٍ إنجليزيّةٍ منشورةٍ عند المصدر "
              f"— يُعرَض عربيّاً، ولا يُحشى بترجمةٍ من عندنا.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
