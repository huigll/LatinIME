cmake_minimum_required(VERSION 3.10.2)
project(jni_latinime)

# 16 KB page alignment for compatibility with 16 KB page size devices (Android 15+)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

file(GLOB_RECURSE LATIN_IME_CORE_SRC
    "src/*.cpp"
)

set(LATIN_IME_JNI_SRC
    com_android_inputmethod_keyboard_ProximityInfo.cpp
    com_android_inputmethod_latin_BinaryDictionary.cpp
    com_android_inputmethod_latin_BinaryDictionaryUtils.cpp
    com_android_inputmethod_latin_DicTraverseSession.cpp
    jni_common.cpp
)

add_library(jni_latinime SHARED
    ${LATIN_IME_JNI_SRC}
    ${LATIN_IME_CORE_SRC}
)

target_include_directories(jni_latinime PRIVATE
    .
    src
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(jni_latinime
    ${log-lib}
    ${android-lib}
)

set(PINYINIME_SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../keyboard/src/main/cpp/pinyinime")

add_library(jni_pinyinime SHARED
    ${PINYINIME_SRC_DIR}/pinyin_jni.cpp
    ${PINYINIME_SRC_DIR}/share/dictbuilder.cpp
    ${PINYINIME_SRC_DIR}/share/dictlist.cpp
    ${PINYINIME_SRC_DIR}/share/dicttrie.cpp
    ${PINYINIME_SRC_DIR}/share/lpicache.cpp
    ${PINYINIME_SRC_DIR}/share/matrixsearch.cpp
    ${PINYINIME_SRC_DIR}/share/mystdlib.cpp
    ${PINYINIME_SRC_DIR}/share/ngram.cpp
    ${PINYINIME_SRC_DIR}/share/pinyinime.cpp
    ${PINYINIME_SRC_DIR}/share/searchutility.cpp
    ${PINYINIME_SRC_DIR}/share/spellingtable.cpp
    ${PINYINIME_SRC_DIR}/share/spellingtrie.cpp
    ${PINYINIME_SRC_DIR}/share/splparser.cpp
    ${PINYINIME_SRC_DIR}/share/userdict.cpp
    ${PINYINIME_SRC_DIR}/share/utf16char.cpp
    ${PINYINIME_SRC_DIR}/share/utf16reader.cpp
    ${PINYINIME_SRC_DIR}/share/sync.cpp
)

target_include_directories(jni_pinyinime PRIVATE
    ${PINYINIME_SRC_DIR}
    ${PINYINIME_SRC_DIR}/include
)

target_link_libraries(jni_pinyinime
    ${log-lib}
)
