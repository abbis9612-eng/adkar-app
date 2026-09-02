#!/usr/bin/env bash
# التحقّق الكامل قبل الدفع — نفس ما يفعله CI، محلياً وفي ثوانٍ.
#
# الترتيب مقصود: الحرّاس أولاً لأنها تكشف في ثانية ما يكلّف المترجم
# دقائق، ثم الترجمة، ثم الاختبارات.
set -uo pipefail
cd "$(dirname "$0")/.."
export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

fail=0
step() {
    printf '\n\033[1m── %s\033[0m\n' "$1"; shift
    if "$@"; then :; else echo "  ✗ فشل"; fail=1; fi
}

step "المسارات اليتيمة"   python3 tools/check_orphan_routes.py
step "ملفات النصوص"       python3 tools/check_strings.py
step "المصادر الدينية"     python3 tools/check_religious_sources.py
step "نصّ ديني في الكود"   python3 tools/check_devotional_in_code.py
step "إحداثيات المدن"      python3 tools/check_cities.py
step "بيانات القرآن"       python3 tools/check_quran_data.py
step "مخطّط القاعدة"       python3 tools/check_schema.py
step "الاستيرادات"        python3 tools/check_imports.py
step "الترجمة"            ./gradlew :androidApp:compileDebugKotlin -q
step "الاختبارات"          ./gradlew :shared:testDebugUnitTest :androidApp:testDebugUnitTest -q

if [ "$fail" -eq 0 ]; then
    printf '\n\033[32mكل الفحوص خضراء — آمن للدفع.\033[0m\n'
else
    printf '\n\033[31mفشل فحص أو أكثر — لا تدفع.\033[0m\n'
fi
exit "$fail"
