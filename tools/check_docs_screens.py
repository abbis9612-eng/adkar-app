#!/usr/bin/env python3
"""
حارسُ الوثائق — كلُّ شاشةٍ تُذكَر لها ملفٌّ على القرص.

`check_orphan_routes.py` يمسك الاتّجاهَ الأوّل: شاشةٌ موجودةٌ لا مدخلَ
لها. وهذا يمسك الاتّجاهَ المعاكس: **وثيقةٌ تصف شاشةً لا وجودَ لها**.

وليس فرضاً نظرياً. حين كُتب هذا الحارس كان `PROJECT_DOCS.md` يسمّي عشرَ
شاشاتٍ محذوفة:

    HomeScreen · QuranReadingScreen · QuranAudioPlayer · KhatiraScreen
    CustomDhikrScreen · PrayerTrackingScreen · DeepLinkLandingScreen
    RamadanHomeScreen · WidgetSettingsScreen · EmotionalDuaScreen

**ثلاثٌ منها حُذفت في الجولة التي كشفت الأمر؛ والسبعُ الباقيةُ كانت
مفقودةً من قبلُ بجولات.** أي أنّ الوثيقةَ تنجرف منذ زمنٍ ولا شيءَ يمسكها،
وكلُّ من قرأها — إنساناً كان أو نموذجاً — يبني على تطبيقٍ لا وجودَ له.

والانجرافُ صامتٌ بطبعه: لا يُسقط بناءً ولا اختباراً، ولا يظهر إلّا حين
يبحث أحدٌ عن ملفٍّ فلا يجده. فمن هنا الحارس.

**ولا يُدار مثلُه على `FINISH_PLAN.md` و`REVIEW_REPORT.md`:** تلك سجلُّ
ما وُجد ومتى، وذكرُها شاشةً محذوفةً صوابٌ لا خطأ — هي تصف الماضي لا
الحاضر. أمّا `PROJECT_DOCS.md` و`APP_SPECIFICATION.md` فيدّعيان وصفَ ما
هو قائمٌ الآن، فيُحاسَبان عليه.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC  = ROOT / "androidApp/src/main/kotlin"

#  الوثائقُ التي تدّعي وصفَ الحاضر — وحدَها تُحاسَب.
DOCS = ["PROJECT_DOCS.md", "APP_SPECIFICATION.md"]

#  أيُّ اسمٍ داخل `backticks` يشبه اسمَ شاشةٍ أو نموذجِ عرض.
NAME = re.compile(r"`(\w+(?:Screen|ViewModel))`")

#  أسماءٌ تُذكر وصفاً لنمطٍ لا لملفّ — تُستثنى بسببها المكتوب.
ALLOWED = {
    "ViewModel": "الكلمةُ مجرّدةً اسمُ نمطٍ معماريّ لا ملفّ.",
}


def files_on_disk() -> set[str]:
    return {p.stem for p in SRC.rglob("*.kt")}


def main() -> int:
    have = files_on_disk()
    problems = 0
    checked  = 0

    for doc in DOCS:
        path = ROOT / doc
        if not path.exists():
            continue
        for i, line in enumerate(path.read_text(encoding="utf-8").split("\n"), 1):
            #  السطورُ المقتبسةُ بـ`>` تشرح الانجرافَ الماضيَ وتسمّي
            #  الشاشاتِ المحذوفةَ عمداً — وهي التوثيقُ نفسُه، لا ادّعاء.
            if line.lstrip().startswith(">"):
                continue
            for m in NAME.finditer(line):
                name = m.group(1)
                if name in ALLOWED:
                    continue
                checked += 1
                if name not in have:
                    print(f"  ✗ {doc}:{i}")
                    print(f"      `{name}` — لا ملفَّ له في androidApp/src/main/kotlin")
                    problems += 1

    print(f"فُحص {checked} اسمَ شاشةٍ في {len(DOCS)} وثيقة · مشاكل {problems}")
    if problems:
        print("\nالوثيقةُ تصف تطبيقاً غيرَ الموجود. احذف الاسمَ أو أعِد توليد")
        print("الجدول من RafiqRoute.kt + RafiqNavGraph.kt — لا تحرّره سطراً سطراً.")
        return 1

    print("كلُّ شاشةٍ مذكورةٍ لها ملفُّها.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
