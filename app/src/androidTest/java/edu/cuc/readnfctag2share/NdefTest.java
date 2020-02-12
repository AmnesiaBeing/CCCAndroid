package edu.cuc.readnfctag2share;

import android.content.Context;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NdefTest {

    private static String TAG = NdefTest.class.getSimpleName();

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        NdefRecord record = NdefRecord.createApplicationRecord(appContext.getPackageName());

//        Log.i(TAG, record.toMimeType());
        Log.i(TAG, record.toString());
        Log.i(TAG, record.toUri().toString());

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
        } catch (NoSuchAlgorithmException e) {

        }
    }
}
