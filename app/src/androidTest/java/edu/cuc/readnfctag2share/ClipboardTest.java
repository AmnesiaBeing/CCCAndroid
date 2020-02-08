package edu.cuc.readnfctag2share;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.cuc.readnfctag2share.helpers.ClipboardHelper;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ClipboardTest {
    private static String TAG = "ClipboardTest";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("edu.cuc.readnfctag2share", appContext.getPackageName());

        Log.i(TAG, ClipboardHelper.getClipboardContent(appContext));
    }
}
