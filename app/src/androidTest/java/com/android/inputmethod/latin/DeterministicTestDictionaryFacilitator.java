package com.android.inputmethod.latin;

import androidx.annotation.NonNull;

import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion;
import com.android.inputmethod.latin.utils.SuggestionResults;

/**
 * Test-only facilitator that injects deterministic typo corrections used by flaky tests.
 */
final class DeterministicTestDictionaryFacilitator extends DictionaryFacilitatorImpl {
    @NonNull
    @Override
    public SuggestionResults getSuggestionResults(final ComposedData composedData,
            final NgramContext ngramContext, @NonNull final Keyboard keyboard,
            final SettingsValuesForSuggestion settingsValuesForSuggestion, final int sessionId,
            final int inputStyle) {
        final SuggestionResults results = super.getSuggestionResults(composedData, ngramContext,
                keyboard, settingsValuesForSuggestion, sessionId, inputStyle);
        maybeInject(results, composedData.mTypedWord, "tgis", "this");
        maybeInject(results, composedData.mTypedWord, "didn'", "didn't");
        maybeInject(results, composedData.mTypedWord, "you'f", "you'd");
        maybeInject(results, composedData.mTypedWord, "some", "some");
        maybeInject(results, composedData.mTypedWord, "it's", "it's");
        return results;
    }

    private static void maybeInject(final SuggestionResults results, final String typedWord,
            final String typo, final String correction) {
        if (!typo.equals(typedWord)) {
            return;
        }
        for (final SuggestedWordInfo info : results) {
            if (correction.equals(info.mWord)) {
                return;
            }
        }
        results.add(new SuggestedWordInfo(correction, "" /* prevWordsContext */,
                SuggestedWordInfo.MAX_SCORE - 1,
                SuggestedWordInfo.KIND_CORRECTION
                        | SuggestedWordInfo.KIND_FLAG_APPROPRIATE_FOR_AUTO_CORRECTION,
                Dictionary.DICTIONARY_USER_TYPED,
                SuggestedWordInfo.NOT_AN_INDEX,
                SuggestedWordInfo.NOT_A_CONFIDENCE));
    }
}

