# LatinIME

[中文](README.md)

A keyboard project based on AOSP LatinIME, built with Gradle. It keeps multi-language full keyboard and word prediction, and adds extensions such as **Chinese Pinyin input**.

## Overview

- **Multi-language keyboards**: Dozens of layouts (English, French, German, Spanish, etc.)
- **Chinese Pinyin input**: Integrated AOSP Pinyin engine with zh_CN subtype and offline dictionary
- **Prediction & correction**: Multi-language lexicons, auto-correction, personal dictionary
- **Setup wizard**: Dedicated Setup UI for enabling and configuring the IME

## Main changes after forking from AOSP

On top of AOSP LatinIME, this repo applies the following changes:

### Build and project structure

- **Gradle**: Soong/Android.bp removed; Gradle multi-module (`:app` + `:keyboard`)
- **JNI**: CMake for native build; Pinyin JNI merged into this LatinIME project
- **Modules**: Main app in `app`; keyboard and input logic in `keyboard` library; Setup wizard and related UI in app

### Chinese input

- AOSP Pinyin engine integrated; **zh_CN** subtype supported
- Chinese dictionary build flow (dicttool + combined format) to produce offline `main_zh_cn.dict`
- Pinyin-related AndroidTests moved into the app module

### Behavior and UI

- Scrollable suggestion strip and expanded suggestions panel
- Composing text shown in a bubble
- Fixes for prediction and backspace-after-composing when switching between Chinese and English

### Tests and stability

- **SubtypeLocaleUtils**: Normalize locale strings (e.g. `sr_zz` → `sr_ZZ`) so exceptional-locale resources are found
- **SubtypeLocaleUtilsTests / RichInputMethodSubtypeTests**: Tolerate Filipino/Tagalog display name differences
- **SpacingAndPunctuationsTests**: Assertions aligned with current resource config (sentence/word separators, language-has-spaces, etc.)
- Full `connectedDebugAndroidTest` passes (~981 tests)

## Build and install

```bash
# Build and install Debug on a connected device
./gradlew :app:installDebug
```

After install, enable the IME under **Settings → Languages & input → Keyboards**.

## Chinese input and dictionary

This project supports a **Chinese (Simplified) Pinyin** subtype and lets you build or update an offline dictionary.

### Using Chinese input on device

1. Install and enable this IME
2. Add “Chinese (Simplified)” or zh_CN in the keyboard language list
3. Type with the Pinyin keyboard; the suggestion bar shows predictions and candidates

### Building a Chinese offline dictionary (optional)

To use a custom word list or refresh `main_zh_cn.dict`, follow these steps.

#### 1) Prepare a combined-format word list

Example combined format:

```
dictionary=main,locale=zh_CN,version=4
 word=你好,f=128
 word=中国,f=120
 word=中文,f=110
```

`f` is frequency (1–255). If your source has only words, use a fixed default frequency.

Example: convert a “one word per line, optional frequency” file into `zh_cn.combined`:

```python
# Example: build zh_cn.combined from dict.txt.big
python - <<'PY'
from pathlib import Path

src = Path("C:/Users/larry/Downloads/dict.txt.big")
out = Path("tools/dicttool/zh_cn.combined")
out.parent.mkdir(parents=True, exist_ok=True)

word_freq = {}
for line in src.read_text(encoding="utf-8", errors="ignore").splitlines():
    parts = line.strip().split()
    if not parts:
        continue
    word = parts[0]
    freq = 128
    if len(parts) >= 2:
        try:
            freq = int(parts[1])
        except ValueError:
            freq = 128
    freq = max(1, min(255, freq))
    prev = word_freq.get(word)
    if prev is None or freq > prev:
        word_freq[word] = freq

with out.open("w", encoding="utf-8", newline="\n") as f:
    f.write("dictionary=main,locale=zh_CN,version=4\n")
    for word, freq in word_freq.items():
        f.write(f" word={word},f={freq}\n")
PY
```

#### 2) Build binary dictionary with dicttool

You need `dicttool_aosp.jar` (from AOSP or the one shipped in this repo).

```bash
java -jar tools/dicttool/dicttool_aosp.jar \
  makedict -s tools/dicttool/zh_cn.combined \
  -d tools/dicttool/main_zh_cn.dict -2
```

- `-2`: single-file v2 format, suitable for `res/raw`
- `-4`: directory output (.header + .body)

#### 3) Put the dictionary into project resources

```bash
# Copy the built dictionary into the keyboard module’s raw resources
cp tools/dicttool/main_zh_cn.dict keyboard/src/main/res/raw/main_zh_cn.dict
```

Rebuild and reinstall; the Pinyin IME will use the new offline dictionary.

## Testing

```bash
# Run all Android instrumentation tests (device/emulator required)
./gradlew :app:connectedDebugAndroidTest

# Run a single test class, e.g.:
./gradlew :app:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=com.android.inputmethod.latin.utils.SubtypeLocaleUtilsTests"
```

Reports: `app/build/reports/androidTests/connected/debug/`.

## License and credits

- This project is based on **AOSP LatinIME** and is under the Apache License 2.0.
- The Chinese Pinyin engine and dictionary pipeline come from AOSP; dictionary content can be your own or any compatible word list.

See `NOTICE` and related files in the repo.
