#!/usr/bin/env bash
# تثبيت Android SDK لبيئة بلا واجهة (حاوية، CI، جلسة بعيدة).
#
# سبب وجوده: افترضتُ جلسةً كاملةً أن الترجمة المحلية مستحيلة هنا، فكان
# CI مترجمي الوحيد ودورته ست دقائق. كسرتُ البناء ثلاث مرات في يوم، وكلّها
# أخطاء يمسكها المترجم في ثوانٍ. الافتراض كان خاطئاً: كل ما لزم ٤٦٢
# ميغابايت وخمس دقائق.
set -euo pipefail

SDK="${ANDROID_HOME:-/opt/android-sdk}"
CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

# النسخ مأخوذة من androidApp/build.gradle.kts — حدّثها معه
PLATFORM="platforms;android-36"
BUILDTOOLS="build-tools;36.0.0"

if [ -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
    echo "الـSDK مثبَّت في $SDK"
else
    echo "── تنزيل أدوات سطر الأوامر ──"
    mkdir -p "$SDK/cmdline-tools"
    tmp="$(mktemp -d)"
    curl -sSL --max-time 600 -o "$tmp/cmd.zip" "$CMDLINE_URL"
    unzip -q "$tmp/cmd.zip" -d "$tmp/x"
    mv "$tmp/x/cmdline-tools" "$SDK/cmdline-tools/latest"
    rm -rf "$tmp"
fi

export ANDROID_HOME="$SDK" ANDROID_SDK_ROOT="$SDK"
export PATH="$PATH:$SDK/cmdline-tools/latest/bin"

yes | sdkmanager --licenses > /dev/null 2>&1 || true
sdkmanager "platform-tools" "$PLATFORM" "$BUILDTOOLS" > /dev/null 2>&1

# يقرؤها Gradle لتحديد مكان الـSDK؛ مستثناة في .gitignore لأنها خاصة بالجهاز
echo "sdk.dir=$SDK" > "$(dirname "$0")/../local.properties"

echo "تمّ. ANDROID_HOME=$SDK"
echo "شغّل الآن: tools/verify.sh"
