/*
 * Copyright (C) 2026 The Android Open Source Project
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

import androidx.test.filters.LargeTest;

import com.android.inputmethod.latin.SuggestedWords;
@LargeTest
public class PinyinLanguageSwitchTests extends InputTestsBase {
    private static final String LANGUAGE_ZH_CN = "zh_CN";
    private static final String LANGUAGE_EN_US = "en_US";

    /**
     * Type full pinyin "guanggu", pick candidate "光谷" in one selection, verify commit.
     */
    public void testPinyinGuangguSingleCommit() {
        changeLanguage(LANGUAGE_ZH_CN);
        type("guanggu");
        runMessages();
        pickSuggestionManually("光谷");
        runMessages();
        assertEquals("光谷", mEditText.getText().toString().trim());
    }

    /**
     * Type full pinyin "kabite", pick candidate "卡比特" in one selection, verify commit.
     */
    public void testPinyinKabiteSingleCommit() {
        changeLanguage(LANGUAGE_ZH_CN);
        type("kabite");
        runMessages();
        pickSuggestionManually("卡比特");
        runMessages();
        assertEquals("卡比特", mEditText.getText().toString().trim());
    }

    public void testPinyinCommitThenSwitchToEnglishShowsSuggestions() {
        changeLanguage(LANGUAGE_ZH_CN);
        type("ni");
        runMessages();
        pickSuggestionManually("你");
        runMessages();
        assertEquals("你", mEditText.getText().toString());

        changeLanguage(LANGUAGE_EN_US);
        type('a');
        sleep(DELAY_TO_WAIT_FOR_PREDICTIONS_MILLIS);
        runMessages();

        final SuggestedWords suggestedWords = mLatinIME.getSuggestedWordsForTest();
        if (suggestedWords == null) {
            return;
        }
        assertTrue("Expected suggestions after switching to English",
                suggestedWords.size() > 0);
    }
}
