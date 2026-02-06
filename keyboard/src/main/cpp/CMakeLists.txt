cmake_minimum_required(VERSION 3.22.1)
project(latinime_keyboard)

# Build shared native library from the moved JNI sources.
set(NATIVE_JNI_DIR "${CMAKE_CURRENT_LIST_DIR}/latinime")
add_subdirectory("${NATIVE_JNI_DIR}" native_jni_build)
