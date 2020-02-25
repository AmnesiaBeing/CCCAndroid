package edu.cuc.ccc.backends;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.rpc.CCCGrpc;
import edu.cuc.ccc.rpc.Content;
import edu.cuc.ccc.utils.SSLUtil;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getSimpleName();

    //----------------------------------------------------------------------------------------------

    public static void tryConnectDevice(String uuid, String pin, ManagedChannel channel) {
        NSDHandler.getInstance().startNSDHandler();
        Device targetDevice;
        while ((targetDevice = DeviceManager.getInstance().searchDevice(uuid)) == null) ;
        Log.i(TAG, "tryConnectDevice: find Device");
        try {
            channel = OkHttpChannelBuilder
                    .forAddress(targetDevice.getIpAddrFromNetwork().getHostAddress(), targetDevice.getIpPortFromNetwork())
                    .sslSocketFactory(SSLUtil.getSslContext(targetDevice).getSocketFactory())
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        if (pin != null) {
            CCCGrpc.CCCBlockingStub stub = CCCGrpc.newBlockingStub(channel);
            Content request = Content.newBuilder().setContent(pin).build();
            Content reply = stub.sayHello(request);
        }
    }


}
