package com.android.inputmethod.latin.utils;

/**
 * Test-only stub to satisfy JNI registration when loading libjni_latinime.
 */
public final class BinaryDictionaryUtils {
    private BinaryDictionaryUtils() {
    }

    private static native boolean createEmptyDictFileNative(String filePath, long dictVersion,
            String locale, String[] attributeKeyStringArray, String[] attributeValueStringArray);
    private static native float calcNormalizedScoreNative(int[] before, int[] after, int score);
    private static native int setCurrentTimeForTestNative(int currentTime);
}
