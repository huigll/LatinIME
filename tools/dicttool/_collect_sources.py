from pathlib import Path

root = Path(r"J:/com.google.android.inputmethod.latin/LatinIME")
paths = []

patterns = [
    "tools/dicttool/src/**/*.java",
    "tools/dicttool/compat/**/*.java",
    "java/src/com/android/inputmethod/latin/makedict/**/*.java",
    "tests/src/com/android/inputmethod/latin/makedict/**/*.java",
]

for pattern in patterns:
    paths.extend(root.glob(pattern))

# Add CombinedFormatUtils explicitly
paths.append(root / "java/src/com/android/inputmethod/latin/utils/CombinedFormatUtils.java")

# Add annotation stubs
paths.extend((root / "tools/dicttool/tmp-annotations").glob("**/*.java"))

# Write sources list
out = root / "tools/dicttool/sources.txt"
with out.open("w", encoding="utf-8") as f:
    for p in paths:
        f.write(str(p) + "\n")

print(f"Total sources: {len(paths)}")
print(f"Wrote: {out}")
