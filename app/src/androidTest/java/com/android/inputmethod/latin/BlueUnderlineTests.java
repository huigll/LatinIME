/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.text.style.SuggestionSpan;
import android.text.style.UnderlineSpan;

import androidx.test.filters.LargeTest;

import com.android.inputmethod.latin.common.Constants;

@LargeTest
public class BlueUnderlineTests extends InputTestsBase {

    public void testBlueUnderline() {
        final String STRING_TO_TYPE = "tgis";
        type(STRING_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        runMessages();
        assertComposingText("composing bubble shows typed word", STRING_TO_TYPE);
        final SpanGetter span = new SpanGetter(mEditText.getText(), SuggestionSpan.class);
        assertNull("no suggestion span in editor while composing", span.mSpan);
    }

    public void testBlueUnderlineDisappears() {
        final String STRING_1_TO_TYPE = "tqis";
        final String STRING_2_TO_TYPE = "g";
        type(STRING_1_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        runMessages();
        type(STRING_2_TO_TYPE);
        assertComposingText("composing bubble extends typed word", STRING_1_TO_TYPE + STRING_2_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        runMessages();
        final SpanGetter spanAfter = new SpanGetter(mEditText.getText(), SuggestionSpan.class);
        assertNull("no suggestion span in editor while composing", spanAfter.mSpan);
    }

    public void testBlueUnderlineOnBackspace() {
        final String STRING_TO_TYPE = "tgis";
        type(STRING_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        runMessages();
        type(Constants.CODE_DELETE);
        type(Constants.CODE_DELETE);
        assertComposingText("composing bubble updates on backspace", "tg");
    }

    public void testBlueUnderlineDisappearsWhenCursorMoved() {
        final String STRING_TO_TYPE = "tgis";
        final int NEW_CURSOR_POSITION = 0;
        type(STRING_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        // Simulate the onUpdateSelection() event
        mLatinIME.onUpdateSelection(0, 0, STRING_TO_TYPE.length(), STRING_TO_TYPE.length(), -1, -1);
        runMessages();
        // Here the blue underline has been set. testBlueUnderline() is testing for this already,
        // so let's not test it here again.
        // Now simulate the user moving the cursor.
        mInputConnection.setSelection(NEW_CURSOR_POSITION, NEW_CURSOR_POSITION);
        mLatinIME.onUpdateSelection(STRING_TO_TYPE.length(), STRING_TO_TYPE.length(),
                NEW_CURSOR_POSITION, NEW_CURSOR_POSITION, -1, -1);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        runMessages();
        assertComposingText("composing bubble clears when cursor moves", "");
    }

    public void testComposingStopsOnSpace() {
        final String STRING_TO_TYPE = "this ";
        type(STRING_TO_TYPE);
        sleep(DELAY_TO_WAIT_FOR_UNDERLINE_MILLIS);
        // Simulate the onUpdateSelection() event
        mLatinIME.onUpdateSelection(0, 0, STRING_TO_TYPE.length(), STRING_TO_TYPE.length(), -1, -1);
        runMessages();
        // Here the blue underline has been set. testBlueUnderline() is testing for this already,
        // so let's not test it here again.
        // Now simulate the user moving the cursor.
        assertComposingText("should not be composing after space", "");
    }
}
