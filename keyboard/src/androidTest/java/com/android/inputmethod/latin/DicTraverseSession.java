package com.android.inputmethod.latin;

/**
 * Test-only stub to satisfy JNI registration when loading libjni_latinime.
 */
public final class DicTraverseSession {
    private DicTraverseSession() {
    }

    private static native long setDicTraverseSessionNative(String localeStr, long dictSize);
    private static native void initDicTraverseSessionNative(long traverseSession, long dictionary,
            int[] previousWord, int previousWordLength);
    private static native void releaseDicTraverseSessionNative(long traverseSession);
}
