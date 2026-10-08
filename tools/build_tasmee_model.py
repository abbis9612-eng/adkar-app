#!/usr/bin/env python3
"""
مولّدُ أصول التسميع — النموذجُ والمرشّحاتُ والمعجم.

═══ ما يفعله ═══

يجلب نموذجَ `tarteel-ai/whisper-tiny-ar-quran` مصدَّراً إلى ONNX
ومكمَّماً، ويضع في `assets/tasmee/`:

  encoder_int8.onnx   ١٠ م.ب   صوتٌ ← تمثيلٌ مخفيّ
  decoder_int8.onnx   ٣٠ م.ب   تمثيلٌ ← رموزٌ عربيّة
  mel_filters.bin     ٦٤ ك.ب   مصفوفةُ ٨٠×٢٠١ — تُولَّد هنا لا على الجهاز
  tokens.txt          ٨٠٠ ك.ب  رمزٌ لكلّ سطر بترتيب رقمه
  MODEL.json                   السندُ والترخيصُ والبصماتُ وأرقامُ الرموز

═══ ولماذا مستودعُ التحويل لا الأصل ═══

الأصلُ (`tarteel-ai`) فيه `pytorch_model.bin` ولا ONNX، وتحويلُه يحتاج
torch وoptimum — جيجاباتٌ من الأدوات لا تُطلب لبناء تطبيق. و
`Sharjeelbaig/whisper-tiny-ar-quran-onnx` تصديرٌ منشورٌ له:

  · `license: apache-2.0` وملفُّ `LICENSE` كامل
  · `base_model: tarteel-ai/whisper-tiny-ar-quran` معلَنٌ في بطاقته
  · و**سكربتُ التحويل منشورٌ** (`convert-model.py`) — قُرئ، وهو تصديرُ
    optimum قياسيٌّ زاد مخارجَ الانتباه لتوقيت الكلمات، لا أكثر

ورُفض `omartariq612/...` مع أنّه أكثرُ تنزيلاً: **بلا ترخيصٍ معلوم**.

═══ والبصماتُ تُثبَّت ═══

كلُّ ملفٍّ يُجلَب تُحسب بصمتُه وتُكتب في `MODEL.json`، و
`check_tasmee_model.py` يفشل إن تغيّر ملفٌّ بعد ذلك. فنموذجٌ يُستبدل
بصمتاً في مستودعٍ بعيدٍ لا يدخل تطبيقاً يُسمَّع فيه كتابُ الله.
"""
import hashlib
import json
import math
import pathlib
import struct
import sys
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "androidApp/src/main/assets/tasmee"
LICENSES = ROOT / "tools/licenses"

REPO = "Sharjeelbaig/whisper-tiny-ar-quran-onnx"
BASE = f"https://huggingface.co/{REPO}/resolve/main"
UPSTREAM = "tarteel-ai/whisper-tiny-ar-quran"

#  (الملفُّ البعيد، الاسمُ عندنا)
MODELS = [
    ("onnx/encoder_model_quantized.onnx", "encoder_int8.onnx"),
    ("onnx/decoder_model_merged_quantized.onnx", "decoder_int8.onnx"),
]
META = ["vocab.json", "added_tokens.json", "generation_config.json", "LICENSE"]

#  مواصفاتُ whisper الصوتيّة — من `preprocessor_config.json`، لا من ذاكرة
SR, N_FFT, HOP, N_MELS, N_FRAMES = 16000, 400, 160, 80, 3000

#  أرقامُ الرموز الخاصّة — تُقرأ من `added_tokens.json` ولا تُكتب هنا
SPECIALS = ("<|startoftranscript|>", "<|ar|>", "<|transcribe|>",
            "<|notimestamps|>", "<|endoftext|>")


def fetch(remote: str) -> bytes:
    req = urllib.request.Request(f"{BASE}/{remote}",
                                 headers={"User-Agent": "rafiq-aldhikr/1.0"})
    with urllib.request.urlopen(req, timeout=300) as r:
        return r.read()


def hz_to_mel(f: float) -> float:
    """سلّمُ Slaney — وهو سلّمُ whisper، لا سلّمُ HTK."""
    if f < 1000.0:
        return 3.0 * f / 200.0
    return 15.0 + math.log(f / 1000.0) / (math.log(6.4) / 27.0)


def mel_to_hz(m: float) -> float:
    if m < 15.0:
        return 200.0 * m / 3.0
    return 1000.0 * math.exp((m - 15.0) * (math.log(6.4) / 27.0))


def mel_filters() -> list[list[float]]:
    """مصفوفةُ ٨٠×٢٠١ بتطبيع Slaney — بحسابٍ قياسيٍّ بلا numpy."""
    n_freq = N_FFT // 2 + 1
    fft_freqs = [i * (SR / 2.0) / (n_freq - 1) for i in range(n_freq)]
    lo, hi = hz_to_mel(0.0), hz_to_mel(SR / 2.0)
    hz_pts = [mel_to_hz(lo + (hi - lo) * i / (N_MELS + 1))
              for i in range(N_MELS + 2)]

    fb = []
    for i in range(N_MELS):
        left, center, right = hz_pts[i], hz_pts[i + 1], hz_pts[i + 2]
        #  تطبيعُ Slaney: المرشّحُ يُقسم على عرضه فتتساوى الطاقةُ
        norm = 2.0 / (right - left)
        row = []
        for f in fft_freqs:
            lower = (f - left) / (center - left)
            upper = (right - f) / (right - center)
            row.append(max(0.0, min(lower, upper)) * norm)
        fb.append(row)
    return fb


def tokens_file(vocab: dict, added: dict) -> str:
    """
    رمزٌ لكلّ سطرٍ بترتيب رقمه — والرموزُ الخاصّة سطورٌ فارغة.

    وسطرٌ فارغٌ قرارٌ لا نقص: `<|ar|>` و`<|notimestamps|>` ليست كلاماً
    يُعرض، فلو كُتبت لظهرت في نصّ القارئ. والفراغُ يُسقطها في كوتلن بلا
    قائمةِ استثناءاتٍ تُصان.

    والسطرُ آمنٌ فاصلاً: ترميزُ whisper على مستوى البايت يكتب السطرَ
    الجديد `Ċ`، فلا محرفَ سطرٍ داخل رمز.
    """
    size = max(max(vocab.values()), max(added.values())) + 1
    rows = [""] * size
    for piece, i in vocab.items():
        rows[i] = piece
    for piece, i in added.items():
        rows[i] = ""  # خاصٌّ: لا يُعرض
        if "\n" in piece:
            raise SystemExit(f"رمزٌ فيه سطرٌ جديد: {piece!r}")
    for piece in vocab:
        if "\n" in piece:
            raise SystemExit(f"رمزٌ فيه سطرٌ جديد: {piece!r}")
    return "\n".join(rows) + "\n"


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    sums = {}

    print(f"المصدر: {REPO}  (أصلُه {UPSTREAM})")

    blobs = {}
    for name in META:
        blobs[name] = fetch(name)
        print(f"  ← {name}  {len(blobs[name]) / 1024:.0f} ك.ب")

    if b"Apache License" not in blobs["LICENSE"]:
        raise SystemExit("ملفُّ الترخيص ليس Apache — أُوقف البناء")
    (LICENSES / "TASMEE-MODEL.txt").write_bytes(blobs["LICENSE"])

    for remote, local in MODELS:
        data = fetch(remote)
        (OUT / local).write_bytes(data)
        sums[local] = hashlib.sha256(data).hexdigest()
        print(f"  ← {local}  {len(data) / 1e6:.2f} م.ب")

    #  المرشّحات: ٨٠×٢٠١ عوّاماً صغيرةَ الطرف — يقرأها كوتلن كما هي
    fb = mel_filters()
    raw = b"".join(struct.pack("<f", v) for row in fb for v in row)
    (OUT / "mel_filters.bin").write_bytes(raw)
    sums["mel_filters.bin"] = hashlib.sha256(raw).hexdigest()
    print(f"  ⊹ mel_filters.bin  {len(raw) / 1024:.0f} ك.ب  "
          f"(المجموع {sum(sum(r) for r in fb):.6f})")

    vocab = json.loads(blobs["vocab.json"])
    added = json.loads(blobs["added_tokens.json"])
    toks = tokens_file(vocab, added).encode("utf-8")
    (OUT / "tokens.txt").write_bytes(toks)
    sums["tokens.txt"] = hashlib.sha256(toks).hexdigest()
    print(f"  ⊹ tokens.txt  {len(toks) / 1024:.0f} ك.ب  "
          f"({len(vocab)} رمزاً + {len(added)} خاصّاً)")

    missing = [s for s in SPECIALS if s not in added]
    if missing:
        raise SystemExit(f"رموزٌ خاصّةٌ ناقصةٌ في النموذج: {missing}")

    meta = {
        "source_repo": REPO,
        "upstream_model": UPSTREAM,
        "license": "Apache-2.0",
        "audio": {"sample_rate": SR, "n_fft": N_FFT, "hop": HOP,
                  "n_mels": N_MELS, "n_frames": N_FRAMES},
        "tokens": {name.strip("<|>"): added[name] for name in SPECIALS},
        "sha256": sums,
    }
    (OUT / "MODEL.json").write_text(
        json.dumps(meta, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    total = sum((OUT / f).stat().st_size for f in sums) / 1e6
    print(f"المجموع: {total:.2f} م.ب · وبصماتُه في MODEL.json")
    return 0


if __name__ == "__main__":
    sys.exit(main())
