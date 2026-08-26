#!/usr/bin/env python3
"""
حارس النصّ الديني المكتوب في الكود.

check_religious_sources.py يفحص ملفات JSON — وهي المسار الصحيح، إذ يحمل
كل نصّ فيها مصدره ودرجته ويُعرضان للمستخدم. لكنه لا يرى نصّاً دينياً
مكتوباً داخل ملف Kotlin، وهناك بالضبط اختبأت ستّ صيغ دعاء بلا إسناد في
EmotionalDuaScreen، تُعرض بنفس هيئة الأدعية الموثَّقة.

هذا الحارس يكشف الصيغ الدعائية في الكود. ليس كل ما يمسكه خطأ — البسملة
عنوانٌ، وآيةٌ في بطاقة ترحيب استشهاد. لذلك: قائمة سماح صريحة، كل بند
فيها قرارٌ مكتوب بسببه، لا استثناء صامت.
"""
import re, sys, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC  = ROOT / "androidApp/src/main/kotlin"

# بدايات دعائية مميّزة — لا تظهر في نصّ واجهة عادي
MARKERS = [
    "اللهم", "اللَّهُمَّ", "أعوذ بالله", "أَعُوذُ بِاللَّهِ", "لا إله إلا",
    "لَا إِلَهَ إِلَّا", "حسبنا الله", "سبحان الله", "سُبْحَانَ اللَّهِ",
    "يا حي يا قيوم", "أذهب البأس", "بسم الله الرحمن", "الحمد لله",
    "الْحَمْدُ لِلَّهِ", "الله أكبر", "اللَّهُ أَكْبَرُ",
]

# قائمة السماح — كل بند قرارٌ مكتوب بسببه، لا استثناء صامت.
# على مستوى الملف حين يكون السبب واحداً لكل ما فيه.
ALLOWED_FILES = {
    "DayCompanionViewModel.kt":
        "كل محطّة تحمل حقل virtue بتخريجها (البخاري · مسلم · الترمذي · "
        "اختيار ابن القيم في الوابل الصيّب)، ويُعرض في ورقة اليوم. "
        "تُحقّق يدوياً: ١٠ محطّات، ١٠ إسنادات.",
    "TasbeehScreen.kt":
        "ألفاظ الباقيات الصالحات — هي موضوع الشاشة نفسها ومتواترة.",
    "TasbeehViewModel.kt": "كسابقه",
    # مؤجَّلة بوسم @HiddenInV1: لا تُعرض في V1. يجب حسم إسنادها قبل إعادتها.
    "BreathingScreen.kt":   "@HiddenInV1 — غير معروضة. تُحسم قبل إعادتها.",
    "ShareCardScreen.kt":   "@HiddenInV1 — غير معروضة. تُحسم قبل إعادتها.",
    "EmotionalDuaScreen.kt":"@HiddenInV1 — أُخفيت لهذا السبب بالذات: ستّ صيغ بلا مصدر.",
}

# على مستوى النصّ حين يكون الملف عاديّاً وفيه استثناء واحد
# ملحوظة: ذكرُ شاشة الترحيب («أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ…») يسكن في
# strings.xml لا في الكود، فلا يمرّ من هنا أصلاً — وتخريجُه «رواه مسلم ·
# صحيح» معروضٌ تحته على الشاشة نفسها، وهو الشرط.
ALLOWED_STRINGS = {
    ("QuranReadingScreen.kt",   "بِسْمِ اللَّهِ الرَّحْمَٰنِ"): "بسملة رأس السورة",
    ("AdhkarCategoriesScreen.kt","أَلَا بِذِكْرِ اللَّهِ"):      "آية الرعد ٢٨ باستشهادها الظاهر تحتها",
}

LIT = re.compile(r'"((?:[^"\\]|\\.)*)"')
problems = 0

for f in sorted(SRC.rglob("*.kt")):
    for i, line in enumerate(f.read_text().split("\n"), 1):
        s = line.strip()
        if s.startswith(("//", "*", "/*")):
            continue
        for m in LIT.finditer(line):
            v = m.group(1)
            hit = next((k for k in MARKERS if k in v), None)
            if not hit:
                continue
            if f.name in ALLOWED_FILES:
                continue
            if any(f.name == an and frag in v for (an, frag) in ALLOWED_STRINGS):
                continue
            print(f"  ✗ {f.relative_to(ROOT)}:{i}")
            print(f"      {v[:70]}")
            print(f"      نصّ دعائي في الكود بلا مصدر. انقله إلى duas.json أو")
            print(f"      adhkar_*.json بمصدر ودرجة، أو أضِفه إلى ALLOWED بسببه.")
            problems += 1

print(f"مشاكل: {problems}")
sys.exit(1 if problems else 0)
