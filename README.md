# LatinIME

## Generate a Chinese dictionary (offline)

This project supports the AOSP dicttool to convert a text lexicon into a binary
dictionary for `java/res/raw/`.

### 1) Prepare a combined-format input

The combined format looks like:

```
dictionary=main,locale=zh_CN,version=4
 word=你好,f=128
 word=中国,f=120
 word=中文,f=110
```

- `f` is the frequency (1-255). If your source file only has words, use a
  fixed default frequency.

Example: convert a word list where each line is
`<word> [freq] [pos]` into `zh_cn.combined`:

```
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

### 2) Build the binary dictionary with dicttool

You need `dicttool_aosp.jar` (built from AOSP or provided separately).

```
java -jar tools/dicttool/dicttool_aosp.jar \
  makedict -s tools/dicttool/zh_cn.combined \
  -d tools/dicttool/main_zh_cn.dict -2
```

Notes:
- `-2` outputs a single `.dict` file (v2). This is compatible with `res/raw`.
- `-4` outputs a directory with `.header` and `.body` files instead of a single
  `.dict` file.

### 3) Copy into resources

```
copy tools/dicttool/main_zh_cn.dict java/res/raw/main_zh_cn.dict
```

