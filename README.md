# LatinIME

[English](readme_en.md)

基于 AOSP LatinIME 的输入法项目，使用 Gradle 构建，在保留多语言全键盘与联想的基础上，增加了**中文拼音输入**等扩展。

## 功能概览

- **多语言键盘**：英语、法语、德语、西班牙语等数十种语言布局
- **中文拼音输入**：集成 AOSP 拼音引擎，支持 zh_CN 子类型与离线词典
- **联想与纠错**：多语言词库、自动纠错、个人词典
- **设置向导**：独立 Setup 界面，便于启用与配置输入法

## 从 AOSP 切回后的主要修改

在 AOSP 源码基础上，本仓库主要做了以下调整：

### 构建与工程结构

- **Gradle 化**：移除 Soong/Android.bp，改为 Gradle 多模块（`:app` + `:keyboard`）
- **JNI**：使用 CMake 构建 native，Pinyin JNI 已并入 LatinIME 工程
- **模块划分**：主工程为 `app`，键盘与输入逻辑在 `keyboard` 库中；Setup 向导等 UI 在 app 内

### 中文输入法

- 集成 AOSP 拼音引擎，支持 **zh_CN** 子类型
- 提供中文词典构建流程（dicttool + combined 格式），可生成离线 `main_zh_cn.dict`
- 拼音相关 AndroidTest 已迁入 app 模块

### 行为与 UI

- 候选区可滚动、更多建议面板
- 组合文本（composing）气泡显示
- 中英文切换后的联想与退格清除逻辑修正

### 测试与稳定性

- **SubtypeLocaleUtils**：对 locale 字符串做规范化（如 `sr_zz` → `sr_ZZ`），保证异常 locale 资源正确命中
- **SubtypeLocaleUtilsTests / RichInputMethodSubtypeTests**：兼容 Filipino/Tagalog 显示名差异
- **SpacingAndPunctuationsTests**：断言与当前资源配置一致（句号/空格/语言有无空格等）
- 全量 `connectedDebugAndroidTest` 通过（约 981 个测试）

## 构建与安装

```bash
# 编译并安装 Debug 到已连接设备
./gradlew :app:installDebug
```

安装后在系统 **设置 → 语言与输入法 → 键盘** 中启用本输入法即可。

## 中文输入法与词典

本工程支持**中文（简体）拼音**子类型，并可自行生成/更新离线词典。

### 在设备上使用中文输入

1. 安装并启用本输入法
2. 在键盘语言列表中添加「中文（简体）」或 zh_CN
3. 使用拼音键盘输入，候选栏会显示联想与词条

### 生成中文离线词典（可选）

若需使用自定义词库或更新 `main_zh_cn.dict`，可按以下步骤操作。

#### 1）准备 combined 格式词表

combined 格式示例：

```
dictionary=main,locale=zh_CN,version=4
 word=你好,f=128
 word=中国,f=120
 word=中文,f=110
```

其中 `f` 为频率（1–255）。若源文件只有词条，可统一使用一个默认频率。

示例：将「每行一个词，可选频率」的文本转为 `zh_cn.combined`：

```python
# 示例：从 dict.txt.big 生成 zh_cn.combined
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

#### 2）用 dicttool 生成二进制词典

需要 `dicttool_aosp.jar`（可从 AOSP 构建或使用仓库内提供的）。

```bash
java -jar tools/dicttool/dicttool_aosp.jar \
  makedict -s tools/dicttool/zh_cn.combined \
  -d tools/dicttool/main_zh_cn.dict -2
```

- `-2`：输出单文件 v2 格式，可直接放入 `res/raw`
- `-4`：输出目录形式（.header + .body）

#### 3）放入工程资源

```bash
# 将生成的主词典复制到 keyboard 模块 raw 资源目录
cp tools/dicttool/main_zh_cn.dict keyboard/src/main/res/raw/main_zh_cn.dict
```

重新编译安装后，中文拼音将使用新的离线词典。

## 测试

```bash
# 运行全部 Android 仪器测试（需已连接设备/模拟器）
./gradlew :app:connectedDebugAndroidTest

# 仅运行某一测试类，例如：
./gradlew :app:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=com.android.inputmethod.latin.utils.SubtypeLocaleUtilsTests"
```

报告输出目录：`app/build/reports/androidTests/connected/debug/`。

## 许可证与致谢

- 本项目基于 **AOSP LatinIME**，遵循 Apache License 2.0。
- 中文拼音引擎与词典流程来自 AOSP 相关组件；词典内容可自备或使用兼容格式词表。

详见仓库内 `NOTICE` 等文件。
