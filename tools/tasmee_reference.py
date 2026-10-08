#!/usr/bin/env python3
"""
مرجعُ التسميع — المسارُ مُثبَتاً في بايثون، لتُنقل عنه كوتلن.

═══ لماذا يبقى في المستودع ═══

ليس أداةَ بناءٍ ولا حارساً: **هو المواصفةُ التنفيذيّة**. كلُّ ما تفعله
`TasmeeRecognizer` في كوتلن مكتوبٌ هنا بصيغةٍ تُشغَّل وتُقاس — فمن شكّ في
رقمٍ شغّل هذا وقابل. ولذلك لا يُدرج في `verify.sh`: يحتاج `numpy`
و`onnxruntime`، ولا يُطلب ذاك لبناء تطبيق.

    pip install numpy onnxruntime
    cd androidApp/src/main/assets/tasmee && python3 ../../../../../tools/tasmee_reference.py

═══ ومصيدتان أُسقطتا هنا قبل أن تُسقطا يوماً في كوتلن ═══

**١ · بُعدٌ رمزيٌّ واحدٌ يُربط بقيمتين.** ذاكرةُ المرمِّز في المفكِّك
المدموج تحمل البُعدَ `encoder_sequence_length` نفسَه الذي يحمله التمثيلُ
المخفيّ (١٥٠٠). فمن مرّرها فارغةً بطول صفرٍ رُبط البُعدُ بقيمتين فسقطت
`MatMul`. فتُمرَّر في الممرّ الأوّل أصفاراً **بطول ١٥٠٠** — لا تُقرأ،
ولكنّ شكلَها يجب أن يوافق.

**٢ · ذاكرةُ المرمِّز تُؤخذ مرّةً وتبقى.** في ممرّ الذاكرة لا يُعيد
المفكِّكُ حسابَها، ويُخرج مكانها موتوراً مشوَّهاً `(0, 6, 1, 64)`: دفعةٌ
صفرٌ وطولٌ واحد. فمن أعاده إليه سقط في الخطوة الثانية — ورسالةُ الخطأ
تتكلّم عن `MatMul` في طبقةٍ داخليّةٍ فلا تدلّ على السبب أبداً.

وكلتاهما تسقط **في الخطوة الثانية** لا الأولى، أي بعد أن يبدو كلُّ شيءٍ
سليماً.

═══ وما لا يُثبته هذا الملفّ ═══

يُثبت أنّ المسارَ يعمل، **لا أنّ النموذج يُحسن السمع**. ومدخلُه ضجيجٌ
أبيضُ محدَّدُ البذرة — لا تلاوةٌ مسجَّلة، فقاعدةُ المشروع أنّ صوتَ
التلاوة موقوفٌ حتى يصل إذنٌ مكتوب. ونتيجتُه الصحيحةُ أن يُخرج كلاماً لا
معنى له **بثقةٍ دون الحدّ** — وقد كان: ٠٫٣٤٧ دون ٠٫٥٥. فبوّابةُ «لم
أتبيّن» أمسكته.

وتبقى دقّةُ النموذج على التلاوة غيرَ مقيسةٍ حتى يُختبر على ١٠٠ تسجيلٍ
حقيقيّ — وذاك شرطٌ قبل العرض على الناس، لا بعده.
"""
import json
import pathlib
import sys

import numpy as np
import onnxruntime as ort

ASSETS = (pathlib.Path(__file__).resolve().parent.parent
          / "androidApp/src/main/assets/tasmee")

SR = 16000
N_FFT = 400
HOP = 160
N_MELS = 80
N_FRAMES = 3000
N_SAMPLES = 480000

_T = json.loads((ASSETS / "MODEL.json").read_text(encoding="utf-8"))["tokens"]
SOT, EOT = _T["startoftranscript"], _T["endoftext"]
AR, TRANSCRIBE, NOTIMESTAMPS = _T["ar"], _T["transcribe"], _T["notimestamps"]
LAYERS, HEADS, HEAD_DIM, D_MODEL = 4, 6, 64, 384


def hz_to_mel(f):
    """سلّمُ Slaney — وهو الذي يستعمله whisper، لا سلّمُ HTK."""
    f = np.atleast_1d(np.asarray(f, dtype=np.float64)).copy()
    mel = 3.0 * f / 200.0
    log_t = f >= 1000.0
    mel[log_t] = 15.0 + np.log(f[log_t] / 1000.0) / (np.log(6.4) / 27.0)
    return mel


def mel_to_hz(m):
    m = np.atleast_1d(np.asarray(m, dtype=np.float64)).copy()
    f = 200.0 * m / 3.0
    log_t = m >= 15.0
    f[log_t] = 1000.0 * np.exp((m[log_t] - 15.0) * (np.log(6.4) / 27.0))
    return f


def mel_filters():
    """مصفوفةُ المرشّحات 80×201 — تُولَّد مرّةً وتُحزَم أصلاً."""
    n_freq = N_FFT // 2 + 1
    fft_freqs = np.linspace(0.0, SR / 2.0, n_freq)
    mel_pts = np.linspace(hz_to_mel(0.0)[0], hz_to_mel(SR / 2.0)[0], N_MELS + 2)
    hz_pts = mel_to_hz(mel_pts)

    fb = np.zeros((N_MELS, n_freq))
    diff = np.diff(hz_pts)
    ramps = hz_pts[:, None] - fft_freqs[None, :]
    for i in range(N_MELS):
        lower = -ramps[i] / diff[i]
        upper = ramps[i + 2] / diff[i + 1]
        fb[i] = np.maximum(0.0, np.minimum(lower, upper))
    #  تطبيعُ Slaney: كلُّ مرشّحٍ يُقسم على عرضه
    fb *= (2.0 / (hz_pts[2:N_MELS + 2] - hz_pts[:N_MELS]))[:, None]
    return fb


def log_mel(audio, fb):
    a = np.zeros(N_SAMPLES, dtype=np.float32)
    n = min(len(audio), N_SAMPLES)
    a[:n] = audio[:n]

    #  حشوٌ انعكاسيٌّ بنصف النافذة — كما يفعل whisper قبل STFT
    pad = N_FFT // 2
    padded = np.pad(a, (pad, pad), mode="reflect")
    window = np.hanning(N_FFT + 1)[:N_FFT]

    frames = np.lib.stride_tricks.sliding_window_view(padded, N_FFT)[::HOP]
    frames = frames[:N_FRAMES] * window
    spec = np.abs(np.fft.rfft(frames, n=N_FFT, axis=1)) ** 2

    mel = fb @ spec.T
    log_spec = np.log10(np.maximum(mel, 1e-10))
    log_spec = np.maximum(log_spec, log_spec.max() - 8.0)
    return ((log_spec + 4.0) / 4.0).astype(np.float32)


def decode_tokens(ids, vocab_inv, added_inv):
    """BPE على مستوى البايت — عكسُ ترميز whisper."""
    byte_decoder = {}
    bs = list(range(33, 127)) + list(range(161, 173)) + list(range(174, 256))
    cs = bs[:]
    n = 0
    for b in range(256):
        if b not in bs:
            bs.append(b)
            cs.append(256 + n)
            n += 1
    for b, c in zip(bs, cs):
        byte_decoder[chr(c)] = b

    out = bytearray()
    for i in ids:
        if i in added_inv:
            continue
        piece = vocab_inv.get(i)
        if piece is None:
            continue
        for ch in piece:
            if ch in byte_decoder:
                out.append(byte_decoder[ch])
    return out.decode("utf-8", errors="replace")


def main():
    #  المرشّحاتُ تُقرأ من الأصل المشحون لا تُحسب — فيُفحَص ما يُشحن.
    #  وتُقابَل بالحساب هنا: فرقُهما يكشف عطباً في المولّد.
    fb = np.fromfile(f"{ASSETS}/mel_filters.bin",
                     dtype="<f4").reshape(N_MELS, N_FFT // 2 + 1)
    fb_calc = mel_filters()
    drift = float(np.abs(fb - fb_calc).max())
    print(f"المرشّحات: {fb.shape} · المجموع {fb.sum():.6f} · "
          f"الفرقُ عن الحساب {drift:.3e}")
    assert drift < 1e-6, f"المرشّحاتُ المشحونةُ تخالف الحساب: {drift}"

    #  مدخلٌ صناعيٌّ محدَّدٌ تماماً — لا تلاوةَ مسجَّلة. والغرضُ إثباتُ
    #  أنّ المسارَ يعمل من أوّله إلى آخره، لا قياسُ دقّةٍ على تلاوة.
    rng = np.random.default_rng(1434)
    audio = (0.05 * rng.standard_normal(SR * 4)).astype(np.float32)

    feats = log_mel(audio, fb)
    print(f"اللوغاريتم-مل: {feats.shape} · "
          f"[{feats.min():.4f} .. {feats.max():.4f}]")
    assert feats.shape == (N_MELS, N_FRAMES), feats.shape

    enc = ort.InferenceSession(f"{ASSETS}/encoder_int8.onnx",
                               providers=["CPUExecutionProvider"])
    hidden = enc.run(["last_hidden_state"],
                     {"input_features": feats[None, :, :]})[0]
    print(f"المرمِّز: {hidden.shape}")
    assert hidden.shape == (1, 1500, D_MODEL), hidden.shape

    dec = ort.InferenceSession(f"{ASSETS}/decoder_int8.onnx",
                               providers=["CPUExecutionProvider"])
    out_names = [o.name for o in dec.get_outputs()]

    prompt = [SOT, AR, TRANSCRIBE, NOTIMESTAMPS]

    #  ممرُّ الذاكرة في المفكِّك المدموج: البُعدُ الرمزيُّ
    #  `encoder_sequence_length` واحدٌ يُربط من مدخلين — من المخفيّ
    #  (١٥٠٠) ومن ذاكرة المرمِّز. فلو مُرّرت الذاكرةُ بطول صفرٍ رُبط
    #  البُعدُ بقيمتين فتسقط `MatMul`. فذاكرةُ المرمِّز تُمرَّر بطول
    #  ١٥٠٠ أصفاراً — لا تُقرأ في الممرّ الأوّل، ولكنّ شكلَها يجب أن
    #  يوافق. وذاكرةُ المفكِّك وحدَها هي التي تبدأ فارغة.
    enc_len = hidden.shape[1]
    empty_dec = np.zeros((1, HEADS, 0, HEAD_DIM), dtype=np.float32)
    zero_enc = np.zeros((1, HEADS, enc_len, HEAD_DIM), dtype=np.float32)
    feed = {"encoder_hidden_states": hidden,
            "use_cache_branch": np.array([False])}
    for l in range(LAYERS):
        for kv in ("key", "value"):
            feed[f"past_key_values.{l}.decoder.{kv}"] = empty_dec
            feed[f"past_key_values.{l}.encoder.{kv}"] = zero_enc
    feed["input_ids"] = np.array([prompt], dtype=np.int64)

    tokens, logprobs = [], []
    for step in range(48):
        res = dict(zip(out_names, dec.run(out_names, feed)))
        logits = res["logits"][0, -1]
        logits = logits - logits.max()
        probs = np.exp(logits)
        probs /= probs.sum()
        nxt = int(np.argmax(probs))
        if nxt == EOT:
            break
        tokens.append(nxt)
        logprobs.append(float(np.log(probs[nxt] + 1e-12)))

        #  ذاكرةُ المرمِّز تُؤخذ من الممرّ الأوّل **مرّةً وتبقى**.
        #
        #  فالمفكِّكُ المدموج في ممرّ الذاكرة لا يُعيد حسابها — ويُخرج
        #  مكانها موتوراً مشوَّهاً `(0, 6, 1, 64)`: دفعةٌ صفرٌ وطولٌ واحد.
        #  فمن أعادها إليه سقط في الخطوة الثانية، ورسالتُه تتكلّم عن
        #  `MatMul` في طبقةٍ داخليّة فلا تدلّ على السبب.
        if step == 0:
            enc_past = {f"past_key_values.{l}.encoder.{kv}":
                        res[f"present.{l}.encoder.{kv}"]
                        for l in range(LAYERS) for kv in ("key", "value")}

        feed = {"encoder_hidden_states": hidden,
                "use_cache_branch": np.array([True]),
                "input_ids": np.array([[nxt]], dtype=np.int64)}
        feed.update(enc_past)
        for l in range(LAYERS):
            for kv in ("key", "value"):
                feed[f"past_key_values.{l}.decoder.{kv}"] = \
                    res[f"present.{l}.decoder.{kv}"]

    #  المعجمُ من `tokens.txt` المشحون: رمزٌ لكلّ سطرٍ بترتيب رقمه،
    #  والخاصُّ سطرٌ فارغٌ يُسقَط. فهذا بعينه ما تقرأه كوتلن.
    rows = pathlib.Path(f"{ASSETS}/tokens.txt").read_text(
        encoding="utf-8").split("\n")
    vocab_inv = {i: r for i, r in enumerate(rows) if r}
    added_inv = {}

    text = decode_tokens(tokens, vocab_inv, added_inv)
    conf = float(np.exp(np.mean(logprobs))) if logprobs else 0.0

    print(f"المفكِّك: {len(tokens)} رمزاً · ثقة {conf:.3f}")
    print(f"النصّ: «{text.strip()}»")
    print()
    print("═══ والنتيجةُ ما يُنتظَر: ضجيجٌ أبيضُ يُخرج كلاماً لا معنى له،")
    print("    وهذه بعينها الحالةُ التي وُضعت لها بوّابةُ «لم أتبيّن».")

    #  متّجهاتٌ ذهبيّةٌ تُثبَّت في اختبار كوتلن
    golden = {
        "mel_filters_sum": round(float(fb.sum()), 6),
        "mel_filters_max": round(float(fb.max()), 8),
        "feats_mean": round(float(feats.mean()), 6),
        "feats_min": round(float(feats.min()), 6),
        "feats_max": round(float(feats.max()), 6),
        "hidden_mean": round(float(hidden.mean()), 6),
        "tokens": tokens[:12],
    }
    print()
    print("golden =", json.dumps(golden, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
