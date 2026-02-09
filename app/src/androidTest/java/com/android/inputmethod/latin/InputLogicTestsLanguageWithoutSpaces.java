/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin;

import android.view.inputmethod.BaseInputConnection;

import androidx.test.filters.LargeTest;

import com.android.inputmethod.latin.common.Constants;

@LargeTest
public class InputLogicTestsLanguageWithoutSpaces extends InputTestsBase {
    public void testAutoCorrectForLanguageWithoutSpaces() {
        final String STRING_TO_TYPE = "tgis is";
        final String EXPECTED_RESULT = "thisis";
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(STRING_TO_TYPE);
        commitComposingText();
        assertEquals("simple auto-correct for language without spaces", EXPECTED_RESULT,
                mEditText.getText().toString());
    }

    public void testRevertAutoCorrectForLanguageWithoutSpaces() {
        final String STRING_TO_TYPE = "tgis ";
        final String EXPECTED_INTERMEDIATE_RESULT = "this";
        final String EXPECTED_FINAL_RESULT = "tgis";
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(STRING_TO_TYPE);
        assertEquals("simple auto-correct for language without spaces",
                EXPECTED_INTERMEDIATE_RESULT, mEditText.getText().toString());
        type(Constants.CODE_DELETE);
        assertComposingText("simple auto-correct for language without spaces", EXPECTED_FINAL_RESULT);
        commitComposingText();
        assertEquals("simple auto-correct for language without spaces",
                EXPECTED_FINAL_RESULT, mEditText.getText().toString());
        // Check we are back to composing the word
        assertComposingText("don't resume suggestion on backspace", "");
    }

    public void testDontResumeSuggestionOnBackspace() {
        final String WORD_TO_TYPE = "and this ";
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(WORD_TO_TYPE);
        assertComposingText("don't resume suggestion on backspace", "");
        type(" ");
        type(Constants.CODE_DELETE);
        assertComposingText("don't resume suggestion on backspace", "");
    }

    public void testStartComposingInsideText() {
        final String WORD_TO_TYPE = "abcdefgh ";
        final int typedLength = WORD_TO_TYPE.length() - 1; // -1 because space gets eaten
        final int CURSOR_POS = 4;
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(WORD_TO_TYPE);
        commitComposingText();
        mLatinIME.onUpdateSelection(0, 0, typedLength, typedLength, -1, -1);
        mInputConnection.setSelection(CURSOR_POS, CURSOR_POS);
        mLatinIME.onUpdateSelection(typedLength, typedLength,
                CURSOR_POS, CURSOR_POS, -1, -1);
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();
        assertComposingText("start composing inside text", "");
        type("xxxx");
        assertComposingText("start composing inside text", "xxxx");
    }

    public void testMovingCursorInsideWordAndType() {
        final String WORD_TO_TYPE = "abcdefgh";
        final int typedLength = WORD_TO_TYPE.length();
        final int CURSOR_POS = 4;
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(WORD_TO_TYPE);
        commitComposingText();
        mLatinIME.onUpdateSelection(0, 0, typedLength, typedLength, 0, typedLength);
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();
        mInputConnection.setSelection(CURSOR_POS, CURSOR_POS);
        mLatinIME.onUpdateSelection(typedLength, typedLength,
                CURSOR_POS, CURSOR_POS, 0, typedLength);
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();
        assertComposingText("move cursor inside text", "");
        type("x");
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();
        assertComposingText("start typing while cursor inside composition", "x");
    }

    public void testPredictions() {
        final String WORD_TO_TYPE = "Barack ";
        changeKeyboardLocaleAndDictLocale("th", "en_US");
        type(WORD_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();
        // Make sure there is no space
        commitComposingText();
        assertEquals("predictions in lang without spaces", "Barack",
                mEditText.getText().toString());
        // Test the first prediction is displayed
        final SuggestedWords suggestedWords = mLatinIME.getSuggestedWordsForTest();
        if (suggestedWords == null) {
            return;
        }
        assertEquals("predictions in lang without spaces", "Obama",
                suggestedWords.size() > 0 ? suggestedWords.getWord(0) : null);
    }
}
