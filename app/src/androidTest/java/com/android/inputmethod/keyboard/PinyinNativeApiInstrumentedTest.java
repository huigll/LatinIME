package com.android.inputmethod.keyboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.carbit.inappkeyboard.keyboard.PinyinDecoder;

@RunWith(AndroidJUnit4.class)
public class PinyinNativeApiInstrumentedTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Force native library load.
        new PinyinDecoder(context);
    }

    @After
    public void tearDown() {
        try {
            PinyinDecoder.nativeImCloseDecoder();
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void open_search_getChoice_choose_close_flow() throws Exception {
        assertTrue(openDecoder(context));

        PinyinDecoder.nativeImSetMaxLens(64, 64);
        PinyinDecoder.nativeImResetSearch();

        byte[] buf = buildPinyinBuffer("ni");
        int count = PinyinDecoder.nativeImSearch(buf, buf.length - 1);
        assertTrue(count >= 0);

        String choice0 = PinyinDecoder.nativeImGetChoice(0);
        assertNotNull(choice0);
        if (count > 0) {
            assertFalse(choice0.trim().isEmpty());
            int chooseRet = PinyinDecoder.nativeImChoose(0);
            assertTrue(chooseRet >= 0);
        }

        assertTrue(PinyinDecoder.nativeImCloseDecoder());
    }

    @Test
    public void reset_allows_multiple_searches() throws Exception {
        assertTrue(openDecoder(context));

        byte[] buf1 = buildPinyinBuffer("ni");
        int count1 = PinyinDecoder.nativeImSearch(buf1, buf1.length - 1);
        assertTrue(count1 >= 0);

        PinyinDecoder.nativeImResetSearch();

        byte[] buf2 = buildPinyinBuffer("hao");
        int count2 = PinyinDecoder.nativeImSearch(buf2, buf2.length - 1);
        assertTrue(count2 >= 0);
    }

    @Test
    public void setMaxLens_with_small_values_still_searches() throws Exception {
        assertTrue(openDecoder(context));

        PinyinDecoder.nativeImSetMaxLens(4, 4);
        byte[] buf = buildPinyinBuffer("ni");
        int count = PinyinDecoder.nativeImSearch(buf, buf.length - 1);
        assertTrue(count >= 0);
    }

    @Test
    public void search_empty_buffer_does_not_crash() throws Exception {
        assertTrue(openDecoder(context));

        byte[] buf = buildPinyinBuffer("");
        int count = PinyinDecoder.nativeImSearch(buf, 0);
        assertTrue(count >= 0);
    }

    @Test
    public void getChoice_out_of_range_returns_empty() throws Exception {
        assertTrue(openDecoder(context));

        byte[] buf = buildPinyinBuffer("ni");
        PinyinDecoder.nativeImSearch(buf, buf.length - 1);
        String choice = PinyinDecoder.nativeImGetChoice(9999);
        assertNotNull(choice);
        assertTrue(choice.trim().isEmpty());
    }

    @Test
    public void choose_within_range_returns_without_crash() throws Exception {
        assertTrue(openDecoder(context));

        byte[] buf = buildPinyinBuffer("ni");
        int count = PinyinDecoder.nativeImSearch(buf, buf.length - 1);
        if (count > 0) {
            // Return value may vary; just verify it does not throw.
            PinyinDecoder.nativeImChoose(0);
        }
    }

    @Test
    public void close_can_be_called_multiple_times() throws Exception {
        assertTrue(openDecoder(context));
        assertTrue(PinyinDecoder.nativeImCloseDecoder());
        assertTrue(PinyinDecoder.nativeImCloseDecoder());
    }

    /**
     * Regression test: after choose() the decoder state is "fixed" to that candidate.
     * Without reset, the next search for the same pinyin returns the fixed result (e.g. all "说").
     * InputLogic must call decoder.reset() on commit so that the next typing gets fresh candidates.
     */
    @Test
    public void search_after_choose_without_reset_returns_fixed_result_reset_restores_variety() throws Exception {
        assertTrue(openDecoder(context));
        PinyinDecoder.nativeImSetMaxLens(64, 64);
        PinyinDecoder.nativeImResetSearch();

        byte[] bufS = buildPinyinBuffer("s");
        int count1 = PinyinDecoder.nativeImSearch(bufS, bufS.length - 1);
        Assume.assumeTrue("need at least one candidate for 's'", count1 > 0);

        String firstChoice = PinyinDecoder.nativeImGetChoice(0);
        assertNotNull(firstChoice);
        assertFalse(firstChoice.trim().isEmpty());

        PinyinDecoder.nativeImChoose(0);

        // Search same key again WITHOUT reset: engine may return only the fixed candidate.
        int count2 = PinyinDecoder.nativeImSearch(bufS, bufS.length - 1);
        Set<String> withoutReset = new HashSet<>();
        for (int i = 0; i < count2; i++) {
            String c = PinyinDecoder.nativeImGetChoice(i);
            if (c != null && !c.trim().isEmpty()) {
                withoutReset.add(c.trim());
            }
        }

        PinyinDecoder.nativeImResetSearch();
        int count3 = PinyinDecoder.nativeImSearch(bufS, bufS.length - 1);
        Set<String> afterReset = new HashSet<>();
        for (int i = 0; i < count3; i++) {
            String c = PinyinDecoder.nativeImGetChoice(i);
            if (c != null && !c.trim().isEmpty()) {
                afterReset.add(c.trim());
            }
        }

        assertTrue("After reset, same pinyin must yield multiple distinct candidates (was: " + afterReset + ")",
                afterReset.size() >= 2);
    }




    private boolean openDecoder(Context ctx) throws IOException {
        File usr = new File(ctx.getFilesDir(), "usr_dict.dat");
        if (!usr.exists()) {
            try {
                usr.createNewFile();
            } catch (IOException ignored) {
            }
        }

        final int resId = PinyinDecoder.resolvePinyinDictResId(ctx);
        Assume.assumeTrue("dict_pinyin resource not found", resId != 0);

        try {
            AssetFileDescriptor afd = ctx.getResources().openRawResourceFd(resId);
            boolean ok = PinyinDecoder.nativeImOpenDecoderFd(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength(),
                    (usr.getAbsolutePath() + "\u0000").getBytes(StandardCharsets.UTF_8)
            );
            afd.close();
            return ok;
        } catch (RuntimeException e) {
            // Fallback when resource is compressed (openRawResourceFd throws).
            File dictFile = new File(ctx.getFilesDir(), "dict_pinyin.dat");
            if (!dictFile.exists() || dictFile.length() == 0) {
                try (java.io.InputStream input = ctx.getResources().openRawResource(resId);
                     java.io.OutputStream output = new java.io.FileOutputStream(dictFile)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = input.read(buf)) >= 0) {
                        output.write(buf, 0, n);
                    }
                }
            }
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(dictFile, ParcelFileDescriptor.MODE_READ_ONLY);
            boolean ok = PinyinDecoder.nativeImOpenDecoderFd(
                    pfd.getFileDescriptor(),
                    0L,
                    dictFile.length(),
                    (usr.getAbsolutePath() + "\u0000").getBytes(StandardCharsets.UTF_8)
            );
            pfd.close();
            return ok;
        }
    }

    private static byte[] buildPinyinBuffer(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] buf = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        buf[bytes.length] = 0;
        return buf;
    }
}
