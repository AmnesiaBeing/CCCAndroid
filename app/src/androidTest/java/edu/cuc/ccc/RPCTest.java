package edu.cuc.ccc;

import android.content.Context;
import android.nfc.NdefRecord;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.RPCHandler;
import edu.cuc.ccc.plugins.PluginFactory;

@RunWith(AndroidJUnit4.class)
public class RPCTest {

    private static String TAG = RPCTest.class.getSimpleName();

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Device targetDevice = new Device();
        try {
            targetDevice.setDeviceIPAddress(new ArrayList<Device.IPAddr>() {{
                add(new Device.IPAddr(InetAddress.getByName("192.168.1.4"), 8888));
            }});
        } catch (Exception e) {
            e.printStackTrace();
        }
        BackendService.
                getInstance().
                getRpcHandler().
                sendPairRequest(targetDevice, new RPCHandler.PairRequestCallback() {
                    @Override
                    public void onPairRequestError() {
                        Log.i(TAG, "onPairRequestError");
                    }

                    @Override
                    public void onPairRequestComplete() {
                        Log.i(TAG, "onPairRequestComplete");
                    }
                });
        try {
            Thread.currentThread().join(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
