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

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;

import com.carbit.inappkeyboard.keyboard.PinyinDecoder;
import com.android.inputmethod.latin.common.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.inputmethod.latin.tests.R;

@LargeTest
public class PinyinCommonWordsTests extends InputTestsBase {
    private static final String LANGUAGE_ZH_CN = "zh_CN";
    public void testPinyinCommonWordsFromResource() throws Exception {
        changeLanguage(LANGUAGE_ZH_CN);
        final PinyinDecoder decoder = new PinyinDecoder(getContext());
        forEachPinyinWordPair(new LineConsumer() {
            @Override
            public void accept(final int lineNumber, final String word, final String pinyinRaw) {
                final List<String> tokens = splitTokens(pinyinRaw);
                final PinyinInput pinyinInput = resolvePinyinInput(tokens, word, decoder);
                if (pinyinInput != null) {
                    mEditText.setText("");
                    type(pinyinInput.mTextToType);
                    runMessages();

                    final List<String> candidates = pinyinInput.mCandidates;
                    pickSuggestionManually(word);
                    runMessages();
                    final String actual = stripTrailingWhitespace(mEditText.getText().toString());
                    assertEquals("pinyin commit failed for line " + lineNumber + ": " + word,
                            word, actual);
                    return;
                }

                typeBySyllables(tokens, word, decoder, lineNumber);
            }
        });
    }

    private interface LineConsumer {
        void accept(int lineNumber, String word, String pinyinRaw);
    }

    private void forEachPinyinWordPair(final LineConsumer consumer) throws IOException {
        final Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        final InputStream input = testContext.getResources().openRawResource(
                R.raw.ime_common_words_1000_pinyin_spaced);
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8));
        try {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                final ParsedLine parsed = parseLine(line, lineNumber);
                consumer.accept(lineNumber, parsed.mWord, parsed.mPinyin);
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // Ignore close failures in tests.
            }
        }
    }

    private static final class ParsedLine {
        final String mWord;
        final String mPinyin;
        ParsedLine(final String word, final String pinyin) {
            mWord = word;
            mPinyin = pinyin;
        }
    }

    private ParsedLine parseLine(final String line, final int lineNumber) {
        final int openIndex = line.lastIndexOf('(');
        final int closeIndex = line.lastIndexOf(')');
        assertTrue("invalid format at line " + lineNumber + ": " + line,
                openIndex > 0 && closeIndex > openIndex);
        final String word = line.substring(0, openIndex).trim();
        final String pinyin = line.substring(openIndex + 1, closeIndex).trim();
        assertTrue("missing word at line " + lineNumber + ": " + line, !word.isEmpty());
        assertTrue("missing pinyin at line " + lineNumber + ": " + line, !pinyin.isEmpty());
        return new ParsedLine(word, pinyin);
    }

    private static String normalizePinyinForTyping(final String pinyinRaw) {
        return pinyinRaw.replace("\u00FC", "v");
    }

    private static String stripTrailingWhitespace(final String value) {
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end);
    }

    private static final class PinyinInput {
        final String mTextToType;
        final List<String> mCandidates;

        PinyinInput(final String textToType, final List<String> candidates) {
            mTextToType = textToType;
            mCandidates = candidates;
        }
    }

    private static PinyinInput resolvePinyinInput(final List<String> tokens, final String word,
            final PinyinDecoder decoder) {
        final String joined = joinTokens(tokens, "");
        final List<String> candidates = decoder.candidates(joined, 100);
        if (candidates.contains(word)) {
            return new PinyinInput(joined, candidates);
        }
        final String withApostrophes = joinTokens(tokens, "'");
        final List<String> candidatesWithApostrophes =
                decoder.candidates(withApostrophes, 100);
        if (candidatesWithApostrophes.contains(word)) {
            return new PinyinInput(withApostrophes, candidatesWithApostrophes);
        }
        return null;
    }

    private static List<String> splitTokens(final String pinyinRaw) {
        final String trimmed = pinyinRaw.trim();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(trimmed.split("\\s+"));
    }

    private static String joinTokens(final List<String> tokens, final String separator) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(normalizePinyinForTyping(tokens.get(i)));
        }
        return builder.toString();
    }

    private void typeBySyllables(final List<String> tokens, final String word,
            final PinyinDecoder decoder, final int lineNumber) {
        final int[] codePoints = StringUtils.toCodePointArray(word);
        assertEquals("pinyin syllable count mismatch at line " + lineNumber + ": " + word,
                tokens.size(), codePoints.length);

        mEditText.setText("");
        for (int i = 0; i < tokens.size(); i++) {
            final String token = normalizePinyinForTyping(tokens.get(i));
            final String expectedChar = new String(codePoints, i, 1);
            type(token);
            runMessages();

            final List<String> candidates = decoder.candidates(token, 100);
            assertTrue("missing syllable candidate at line " + lineNumber + ": "
                    + expectedChar + " (" + tokens.get(i) + ")",
                    candidates.contains(expectedChar));
            pickSuggestionManually(expectedChar);
            runMessages();
        }

        final String actual = stripTrailingWhitespace(mEditText.getText().toString());
        assertEquals("pinyin commit failed for line " + lineNumber + ": " + word,
                word, actual);
    }

}
