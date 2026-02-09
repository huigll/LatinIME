package com.carbit.inappkeyboard.keyboard;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PinyinImeSessionTest {

    private static class FakeDecoder implements IPinyinDecoder {
        int resetCalls = 0;
        String lastPinyin = null;

        @Override
        public void reset() {
            resetCalls++;
        }

        @Override
        public List<String> candidates(String pinyin, int max) {
            lastPinyin = pinyin;
            List<String> base = Arrays.asList("你", "拟", "尼", "呢", "泥");
            return base.subList(0, Math.min(max, base.size()));
        }

        @Override
        public String choose(int index) {
            List<String> c = candidates(lastPinyin != null ? lastPinyin : "", 10);
            return index >= 0 && index < c.size() ? c.get(index) : "";
        }
    }

    private static class FakeCandidateBar implements ICandidateBar {
        List<String> lastCandidates = Arrays.asList();
        boolean cleared = false;
        OnCandidateClickListener lastOnClick = null;

        @Override
        public void setCandidates(List<String> candidates, OnCandidateClickListener onClick) {
            cleared = false;
            lastCandidates = candidates != null ? candidates : Arrays.<String>asList();
            lastOnClick = onClick;
        }

        @Override
        public void clear() {
            cleared = true;
            lastCandidates = Arrays.asList();
            lastOnClick = null;
        }
    }

    private static class BufferTarget implements ITextCommitTarget {
        final StringBuilder sb = new StringBuilder();

        @Override
        public void insert(String text) {
            sb.append(text);
        }

        @Override
        public void deleteLastChar(int count) {
            if (count <= 0) return;
            int len = sb.length();
            if (len == 0) return;
            sb.delete(Math.max(0, len - count), len);
        }
    }

}
