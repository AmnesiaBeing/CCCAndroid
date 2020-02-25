package edu.cuc.ccc;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static edu.cuc.ccc.backends.ConnectionManager.tryConnectDevice;

@RunWith(AndroidJUnit4.class)
public class RPCTest {

    private static String TAG = RPCTest.class.getSimpleName();

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        tryConnectDevice("123456");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
