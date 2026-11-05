#!/usr/bin/env python3
"""
حارسُ أصول التسميع.

`build_tasmee_model.py` يجلب النموذجَ ويكتب بصمةَ كلِّ ملفٍّ في
`MODEL.json`. وهذا يتحقّق أنّ ما في `assets` هو ما جُلب:

١) الملفّاتُ الخمسةُ موجودةٌ وأحجامُها معقولة.
٢) **بصمةُ كلِّ ملفٍّ تطابق المثبَّتة.** فنموذجٌ يُبدَّل — في مستودعٍ
   بعيدٍ أو بيدٍ هنا — لا يدخل تطبيقاً يُسمَّع فيه كتابُ الله بصمتاً.
٣) الترخيصُ محفوظٌ في `tools/licenses/`.
٤) أرقامُ الرموز الخاصّة مثبَّتةٌ — ورقمٌ خاطئٌ منها يجعل النموذج يفكّ
   بلغةٍ أخرى أو يُدرج توقيتاتٍ في النصّ، ولا يظهر ذلك إلّا في التسميع.

ولا يفشل إن غابت الأصولُ كلُّها: التسميعُ ميزةٌ تُبنى على مراحل، ومن
استنسخ المستودعَ قبل أن يُشغّل المولّد لا يُمنع من البناء. والغيابُ
الكاملُ يُقال ولا يُسقط؛ والنقصُ الجزئيُّ يُسقط.
"""
import hashlib
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "androidApp/src/main/assets/tasmee"
LICENSE = ROOT / "tools/licenses/TASMEE-MODEL.txt"

#  (الاسم، أدنى حجمٍ بالبايت، أقصاه)
EXPECT = {
    "encoder_int8.onnx": (9_000_000, 12_000_000),
    "decoder_int8.onnx": (28_000_000, 34_000_000),
    "mel_filters.bin": (64_320, 64_320),
    "tokens.txt": (300_000, 1_200_000),
}

#  أرقامُ whisper الخاصّة — تُثبَّت هنا فلا تُبدَّل في `MODEL.json` بصمت
TOKENS = {
    "startoftranscript": 50258,
    "ar": 50272,
    "transcribe": 50359,
    "notimestamps": 50363,
    "endoftext": 50257,
}

problems = []


def main() -> int:
    if not OUT.exists() or not any(OUT.iterdir()):
        print("أصولُ التسميع لم تُجلب بعد — `python3 tools/build_tasmee_model.py`")
        return 0

    meta_path = OUT / "MODEL.json"
    if not meta_path.exists():
        print("  ✗ MODEL.json ناقص — الأصولُ موجودةٌ بلا سند")
        return 1
    meta = json.loads(meta_path.read_text(encoding="utf-8"))

    for name, (lo, hi) in EXPECT.items():
        p = OUT / name
        if not p.exists():
            problems.append(f"{name}: ناقص")
            continue
        n = p.stat().st_size
        if not lo <= n <= hi:
            problems.append(f"{name}: حجمُه {n} خارج [{lo}, {hi}]")
            continue
        want = meta.get("sha256", {}).get(name)
        if not want:
            problems.append(f"{name}: لا بصمةَ له في MODEL.json")
            continue
        got = hashlib.sha256(p.read_bytes()).hexdigest()
        if got != want:
            problems.append(f"{name}: بصمتُه {got[:16]} والمثبَّتُ {want[:16]}")

    if meta.get("license") != "Apache-2.0":
        problems.append(f"الترخيصُ المعلَن {meta.get('license')!r} لا Apache-2.0")
    if not LICENSE.exists():
        problems.append("tools/licenses/TASMEE-MODEL.txt ناقص")
    elif "Apache License" not in LICENSE.read_text(encoding="utf-8", errors="replace"):
        problems.append("ملفُّ الترخيص ليس نصَّ Apache")

    for name, want in TOKENS.items():
        got = meta.get("tokens", {}).get(name)
        if got != want:
            problems.append(f"رمزُ {name}: {got} والمثبَّتُ {want}")

    audio = meta.get("audio", {})
    for k, want in (("sample_rate", 16000), ("n_fft", 400), ("hop", 160),
                    ("n_mels", 80), ("n_frames", 3000)):
        if audio.get(k) != want:
            problems.append(f"مواصفةُ {k}: {audio.get(k)} والمثبَّتُ {want}")

    for p in problems:
        print(f"  ✗ {p}")
    total = sum((OUT / n).stat().st_size for n in EXPECT if (OUT / n).exists())
    print(f"أصولُ التسميع: {total / 1e6:.2f} ميغابايت · "
          f"{len(EXPECT)} ملفّاً ببصماته · مشاكل {len(problems)}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
