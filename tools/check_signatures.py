#!/usr/bin/env python3
"""
حارسُ وجوه الشاشات.

═══ العطبُ الذي يحرسه ═══

شاشةٌ مركَّبةٌ من بطاقةٍ وشريطٍ وزرّ — قطعٍ موجودةٍ في عشر شاشاتٍ غيرها
— **لا تُسقط بناءً ولا تُلقي استثناءً**. تعمل تماماً. وعطبُها أنّها بلا
وجه: لا شيءَ فيها يقول إنّها هذه الشاشةُ لا غيرها.

ولا يراه إلّا صاحبُ التطبيق، فينظر ويتعب ويقول «بدائيّ». فتصير بوّابةُ
الجودة انتباهَه هو — وما عُلّق على انتباه إنسانٍ يسقط يوم يغفل.

═══ ما يفحصه ═══

كلُّ ملفِّ شاشةٍ يجب أن يُصرّح بسطرٍ في تعليق رأسه:

    التوقيع: [العنصرُ الواحدُ الذي لا نظيرَ له في التطبيق]

وله شرطان في `AGENTS.md`: أن يكون مشتقّاً من موضوع الشاشة نفسِه، وأن
يُشفِّر شيئاً صحيحاً في المحتوى لا أن يزيّنه. وهذان لا يُفحصان آليّاً —
يفحصهما القارئ. **والمُصرَّحُ به وحدَه يمكن أن يُقرأ ويُنقَض.**

ويُفحص شيئان يمكن فحصُهما:

١) **العدد لا يرتفع.** السقفُ هو عددُ الشاشات بلا توقيعٍ اليوم، ولا
   يُرفَع أبداً — يُخفَض. فشاشةٌ جديدةٌ بلا وجهٍ تُسقط البناء، والقديمةُ
   تُعالَج على مهل. وهي قاعدةُ `check_hardcoded_ui.py` نفسُها.

٢) **لا توقيعَ مكرَّر.** شاشتان بالوجه نفسِه ليستا وجهين — إحداهما بلا
   وجه. فيُسقط البناء.

═══ ولماذا ليس «صفراً» من اليوم ═══

حارسٌ يفشل أوّلَ يومٍ على أربعٍ وأربعين شاشةً يُعطَّل في الأسبوع الأوّل،
فلا يحرس شيئاً. والسقفُ الذي ينزل ولا يصعد يحرس من اليوم الأوّل.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
UI = ROOT / "androidApp/src/main/kotlin/app/rafiqaldhikr/ui"

#  السقف: عددُ الشاشات بلا توقيعٍ يوم كُتب هذا الحارس.
#  يُخفَض ولا يُرفَع — ومن رفعه فقد عطّل الحارس.
CEILING = 38

#  ما ليس شاشةً وإن انتهى اسمُه بـScreen: شاشاتُ النظام والحالات.
SKIP = {"CrashReportScreen.kt"}


def screens() -> list[pathlib.Path]:
    out = [f for f in UI.rglob("*Screen.kt") if f.name not in SKIP]
    return sorted(out)


def signature_of(text: str) -> str | None:
    """
    يقرأ سطرَ «التوقيع:» من تعليقٍ في الملفّ.

    ولا يُقصَر البحثُ على أوّله: تعليقُ رأس الشاشة يأتي بعد الاستيرادات،
    وهي في بعض الملفّات أطولُ من ألفَي محرف — فكان السطرُ يُكتَب ولا
    يُرى، والحارسُ يقول «بلا توقيع» وهو مكتوب.

    ويُشترط أن يكون في سطر تعليق (`*` أو `//`) لا في نصٍّ معروض.
    """
    m = re.search(r"^[ \t]*(?:\*|//)[ \t]*التوقيع\s*:\s*(.+)$", text, re.M)
    if not m:
        return None
    return re.sub(r"\s+", " ", m.group(1).strip(" *./")).strip()


def main() -> int:
    files = screens()
    if not files:
        print("  ✗ لم تُعثر شاشةٌ واحدة — تغيّر مسارُ الواجهة؟")
        return 1

    missing, signed = [], {}
    for f in files:
        sig = signature_of(f.read_text(encoding="utf-8"))
        if sig:
            signed.setdefault(sig, []).append(f.name)
        else:
            missing.append(f.name)

    fail = False

    dupes = {s: n for s, n in signed.items() if len(n) > 1}
    if dupes:
        fail = True
        print("  ✗ توقيعٌ مكرَّر — شاشتان بالوجه نفسِه ليستا وجهين:")
        for s, names in dupes.items():
            print(f"      «{s[:60]}»")
            for n in names:
                print(f"         {n}")

    if len(missing) > CEILING:
        fail = True
        print(f"  ✗ {len(missing)} شاشةً بلا توقيع، والسقفُ {CEILING}.")
        print("    شاشةٌ جديدةٌ بلا وجه. أضِف إلى تعليق رأسها سطراً:")
        print("        التوقيع: [العنصرُ الواحدُ الذي لا نظيرَ له]")
        print("    وشرطاه في AGENTS.md. والسقفُ يُخفَض ولا يُرفَع.")
        for n in missing[:6]:
            print(f"      · {n}")

    print(f"  {len(files)} شاشة · {len(signed)} توقيعاً · {len(missing)} بلا توقيع (السقف {CEILING})")
    if len(missing) < CEILING and not fail:
        print(f"    ↓ نزل العددُ — اخفِض CEILING إلى {len(missing)} ليُثبَّت المكسب.")
    if not fail:
        print("وجوهُ الشاشات سليمة.")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(main())
