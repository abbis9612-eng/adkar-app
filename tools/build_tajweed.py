#!/usr/bin/env python3
"""
يبني أصلَ التجويد مُحاذًى على نصِّ المصحف عندنا.

لماذا أداةٌ لا نسخ
──────────────────
مصدرُ البيانات `tarekeldeeb/quran-tajweed-embedded` (CC-BY 4.0) يعطي
مواضعَ الأحكام بالمحارف — لكنّها مواضعُ في **نصّه هو**. ونصُّنا يحمل
علاماتِ الوقف (ۖ ۛ ۗ) وعلاماتٍ أخرى ليست عنده، فتنزاح المواضعُ.

وقِيس الانزياح: من ١٣٢٥٢ موضعَ همزةِ وصل **٦٢٨٥ يقع على حرفٍ آخر**،
و١٠٨ آيةً تتجاوز فيها المواضعُ طولَ نصّنا. أي أنّ النقلَ المباشر يلوّن
**حروفاً خاطئةً في كتاب الله** — وذاك أسوأُ من ألّا يكون تلوينٌ أصلاً.

فتُعاد المحاذاةُ هنا، مرّةً واحدةً عند البناء لا في كلّ تشغيل، ويُفحص
الناتجُ بمعيارٍ لا يحتمل التأويل: **كلُّ موضعِ همزةِ وصلٍ يجب أن يقع على
حرف «ٱ» في نصّنا بالضبط.** فإن نقص واحدٌ سقطت الأداةُ ولم تكتب شيئاً.

الاستعمال
─────────
    python3 tools/build_tajweed.py <tajweed.json> <embeded.txt>
"""
import difflib
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
QURAN = ROOT / "androidApp/src/main/assets/quran_uthmani.json"
OUT = ROOT / "androidApp/src/main/assets/tajweed.txt"

WASL = "ٱ"

#  ترتيبٌ ثابتٌ — الفهرسُ هو ما يُكتب في الأصل، فلا يُعاد ترتيبُه.
RULES = [
    "ghunnah", "idghaam_ghunnah", "idghaam_no_ghunnah", "idghaam_mutajanisayn",
    "idghaam_mutaqaribayn", "idghaam_shafawi", "ikhfa", "ikhfa_shafawi", "iqlab",
    "madd_2", "madd_246", "madd_6", "madd_muttasil", "madd_munfasil",
    "qalqalah", "hamzat_wasl", "lam_shamsiyyah", "silent",
]
IDX = {r: i for i, r in enumerate(RULES)}

#  حروفٌ يجب أن يقع عليها الحكم — بها تُفحَص المحاذاة.
#
#  وثلاثةٌ تكفي: همزةُ الوصل حرفٌ واحدٌ لا يلتبس، واللامُ الشمسيّةُ لامٌ،
#  والقلقلةُ حروفُها خمسةٌ معدودة. وما عداها (المدودُ والإخفاء) يقع على
#  حروفٍ كثيرةٍ فلا يُميّز خطأَ موضعٍ بإزاحةِ حرف.
EXPECT = {
    "hamzat_wasl": WASL,
    "lam_shamsiyyah": "ل",
    "qalqalah": "قطبجد",
}

#  نصُّ المصدر للآية الأولى من كلّ سورةٍ **يبدأ بالبسملة**.
#
#  وقِيس: مواضعُ «الٓمٓ» في البقرة تقع عند ٤٠ و٤٢، وطولُ الآية خمسةُ
#  محارف — فالمرجعُ «بسملةٌ + مسافةٌ + الآية» (٤٤ محرفاً). وبلا هذا تسقط
#  آياتُ الفواتح كلُّها أو تُلوَّن حروفاً خاطئة.
#
#  والفاتحةُ مستثناةٌ لأنّ بسملتها آيةٌ مستقلّةٌ معدودة، وبراءةُ لا بسملةَ
#  لها.
BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"


def source_text(surah: int, ayah: int, body: str) -> tuple:
    """نصُّ المصدر، وإزاحةُ بداية الآية فيه."""
    if ayah == 1 and surah not in (1, 9):
        return BASMALA + " " + body, len(BASMALA) + 1
    return body, 0


def remap(src: str, ours: str, shift: int) -> list:
    """
    خريطةُ موضعٍ من نصّ المصدر إلى نصّنا — أو None لما لا مقابلَ له.

    و[shift] طولُ البسملة المُقحَمة: ما قبله يُهمَل، إذ ليس من الآية.
    """
    body = src[shift:]
    m = [None] * (len(src) + 1)
    sm = difflib.SequenceMatcher(None, body, ours, autojunk=False)
    for a, b, n in sm.get_matching_blocks():
        for k in range(n):
            m[shift + a + k] = b + k
    #  نهايةُ النصّ تقابل نهايتَه — فمدًى ينتهي بآخر حرفٍ لا يضيع
    m[len(src)] = len(ours)
    return m


def main() -> int:
    if len(sys.argv) != 3:
        print(__doc__)
        return 2
    taj = json.loads(pathlib.Path(sys.argv[1]).read_text(encoding="utf-8"))
    theirs = {}
    for line in pathlib.Path(sys.argv[2]).read_text(encoding="utf-8").splitlines():
        if not line:
            continue
        s, a, t = line.split("|", 2)
        #  أحرفُ الأحكام مدسوسةٌ في النصّ — نزعُها يُعيد نصَّهم الصِّرف
        theirs[(int(s), int(a))] = re.sub(r"[a-z]", "", t)

    q = json.loads(QURAN.read_text(encoding="utf-8"))
    items = q if isinstance(q, list) else list(q.values())[0]
    ours = {(a["surah"], a["ayah"]): a["text_uthmani"] for a in items}

    lines, kept, dropped, upstream = [], 0, 0, 0
    wasl_ok = wasl_bad = 0

    for e in taj:
        key = (e["surah"], e["ayah"])
        t, o = theirs.get(key), ours.get(key)
        if t is None or o is None:
            continue
        src, shift = source_text(e["surah"], e["ayah"], t)
        #  المحاذاةُ على نصّ المصدر كلِّه، ثمّ تُسقَط البسملةُ: ما وقع
        #  فيها حكمٌ لآيةٍ أخرى (البسملةُ تُرسم بنفسها في رأس السورة).
        m = remap(src, o, shift)
        spans = []
        for an in e["annotations"]:
            rule = an["rule"]
            if rule not in IDX:
                continue
            if an["start"] >= len(m) or an["end"] >= len(m):
                dropped += 1
                continue
            s, en = m[an["start"]], m[an["end"]]
            #  ما لا مقابلَ له يُسقَط ولا يُخمَّن: حكمٌ بلا موضعٍ مؤكَّدٍ
            #  لا يُلوَّن — والتخمينُ هنا تلوينُ حرفٍ لم يُقصد.
            if s is None or en is None or en <= s:
                dropped += 1
                continue
            #  يُفصَل عطبُ المصدر عن عطب المحاذاة.
            #
            #  ففي المصدر نفسِه مواضعُ تقع على مسافةٍ لا على حرفِ الحكم
            #  (٣٤٥ من ١٣٢٥٢ في همزة الوصل). وتلك تُسقَط ولا تُحسب علينا:
            #  لا نملك تصحيحَها، ولا نلوّن حرفاً بحكمٍ لم يُقصد.
            #
            #  أمّا ما صحّ في المصدر ثمّ ضلّ عندنا فذاك **عطبُ محاذاةٍ**
            #  يُسقط الأداة: معناه أنّ الخريطةَ كاذبة، والباقي كلُّه مشكوكٌ
            #  فيه.
            expect = EXPECT.get(rule)
            if expect is not None:
                in_src = any(c in expect for c in src[an["start"]:an["end"]])
                in_ours = any(c in expect for c in o[s:en])
                if not in_src:
                    upstream += 1
                    continue
                if not in_ours:
                    wasl_bad += 1
                    if wasl_bad <= 3:
                        print(f"  ✗ محاذاة {e['surah']}:{e['ayah']} {rule} "
                              f"{src[an['start']:an['end']]!r} ← {o[s:en]!r}")
                    continue
                wasl_ok += 1
            spans.append(f"{s},{en},{IDX[rule]}")
            kept += 1
        if spans:
            lines.append(f"{e['surah']}|{e['ayah']}|{';'.join(spans)}")

    print(f"أحكامٌ محاذاة: {kept} · بلا مقابلٍ في نصّنا: {dropped} · "
          f"عطبٌ في المصدر فأُسقط: {upstream}")
    print(f"فحصُ الحروف — مطابق: {wasl_ok} · عطبُ محاذاة: {wasl_bad}")

    if wasl_bad:
        print("  ✗ حكمٌ صحَّ في المصدر وضلَّ موضعُه عندنا — الخريطةُ كاذبة،")
        print("     ولا يُكتب أصلٌ قد يلوّن حرفاً خاطئاً في كتاب الله.")
        return 1

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"كُتب {OUT.relative_to(ROOT)} · {OUT.stat().st_size // 1024} كيلوبايت · {len(lines)} آية")
    return 0


if __name__ == "__main__":
    sys.exit(main())
