import re
from pathlib import Path

src = Path(r"C:/Users/larry/Downloads/dict.txt.big")
out = Path(r"J:/com.google.android.inputmethod.latin/LatinIME/tools/dicttool/zh_cn.combined")

out.parent.mkdir(parents=True, exist_ok=True)

word_freq = {}
line_count = 0
for line in src.read_text(encoding="utf-8", errors="ignore").splitlines():
    line = line.strip()
    if not line:
        continue
    parts = line.split()
    if not parts:
        continue
    word = parts[0]
    freq = 128
    if len(parts) >= 2:
        try:
            freq = int(parts[1])
        except ValueError:
            freq = 128
    if freq < 1:
        freq = 1
    if freq > 255:
        freq = 255
    # keep max frequency for duplicates
    prev = word_freq.get(word)
    if prev is None or freq > prev:
        word_freq[word] = freq
    line_count += 1

with out.open("w", encoding="utf-8", newline="\n") as f:
    f.write("dictionary=main,locale=zh_CN,version=4\n")
    for word, freq in word_freq.items():
        f.write(f" word={word},f={freq}\n")

print(f"Lines read: {line_count}")
print(f"Unique words: {len(word_freq)}")
print(f"Wrote: {out}")
