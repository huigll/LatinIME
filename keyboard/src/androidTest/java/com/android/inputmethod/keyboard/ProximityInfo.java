package com.android.inputmethod.keyboard;

/**
 * Test-only stub to satisfy JNI registration when loading libjni_latinime.
 */
public final class ProximityInfo {
    private ProximityInfo() {
    }

    private static native long setProximityInfoNative(int displayWidth, int displayHeight,
            int gridWidth, int gridHeight, int mostCommonkeyWidth, int mostCommonkeyHeight,
            int[] proximityChars, int keyCount, int[] keyXCoordinates, int[] keyYCoordinates,
            int[] keyWidths, int[] keyHeights, int[] keyCharCodes, float[] sweetSpotCenterXs,
            float[] sweetSpotCenterYs, float[] sweetSpotRadii);

    private static native void releaseProximityInfoNative(long proximityInfo);
}
