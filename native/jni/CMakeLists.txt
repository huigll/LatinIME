cmake_minimum_required(VERSION 3.10.2)
project(jni_latinime)

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
