package edu.cuc.ccc.backends;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.InetAddress;

import edu.cuc.ccc.helpers.BTHelper;
import edu.cuc.ccc.helpers.NetworkHelper;
import edu.cuc.ccc.helpers.NotificationHelper;

// 后（前）台服务，感觉这个数据不好走啊，流程贼复杂
// Activity-->onCreate||onStart-->Helper-->bind||setCallback
// Activity-->Helper-->commands-->onStartCommand-->doSth-->Handler-->Callback-->Activity
// 先判断网络是否处于wifi已连接状态，如果是，则通过arp协议或者NSD获取地址？
// 如果不是，判断是否可以通过WLAN直连连接
// 如果不是，判断是否可以通过蓝牙连接
public class BackendService extends Service implements NsdManager.ResolveListener {

    private static final String TAG = BackendService.class.getSimpleName();

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private static BackendService instance;

    public static BackendService getInstance() {
        return instance;
    }

//    private BackendServiceCallbackInterface mCallback;

    private NotificationHelper notificationHelper;
    private BTHelper btHelper;
    private NSDHandler nsdHandler;
    private NetworkHelper networkHelper;

    //    private ManagedChannel channel;

    // 负责控制流程的handler，负责达成连接关系，不负责数据传输
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Command cmd = Command.values()[msg.arg1];
            Log.i(TAG, "handleMessage: " + cmd.name());
            switch (cmd) {
//                case CMD_NONE:
//                    break;
//                case CMD_DESTROY:
//                    break;
//                case CMD_START:
//                    // 读取上一次使用的设备Info，设置为目标连接设备
//                    DeviceInfo lastDevice = DeviceInfo.parseJSONStr(MySharedPreferences.getApplicationSharedPreferences().getString("LastDevice", null));
//                    nsdHandler.setTargetDevice(lastDevice);
//                    break;
//                case CMD_CONNECT_DEVICE:
//                    // 尝试连接设备，设备的MAC地址存放在msg的obj中，类型为DeviceInfo
//                    DeviceInfo device = (DeviceInfo) msg.obj;
//                    if (device == null) return;
//                    // TODO:根据连接优先级重新调整结构体中，mac顺序
//                    nsdHandler.setTargetDevice(device);
//                    break;
            }
//            if (mCallback != null) {
//                mCallback.BackendServiceCallback();
//            }
        }
    }

    // startService(intent)-->here-->handleMessage
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
//        Command cmd = (Command) intent.getSerializableExtra("command");
//        if (cmd == null) cmd = Command.CMD_NONE;
//        Log.i(TAG, "onStartCommand: " + cmd.name());
        Message msg = new Message();
//        msg.arg1 = cmd.ordinal();
        msg.obj = intent.getSerializableExtra("extra");
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        BackendService.instance = this;

        notificationHelper = new NotificationHelper(this);
        notificationHelper.onCreate();

        btHelper = new BTHelper(this);
        btHelper.onCreate();

        nsdHandler = new NSDHandler(this);
        nsdHandler.initializeResolveListener();
        nsdHandler.initializeDiscoveryListener();
        nsdHandler.discoveryServices();

        HandlerThread thread = new HandlerThread("Service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
//
//        channel = ManagedChannelBuilder.forAddress("192.168.1.4", 50051).usePlaintext().build();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        notificationHelper.onDestroy();
        btHelper.onDestroy();
        nsdHandler.onDestroy();
    }

//    private final IBinder binder = new BackendServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
//        return binder;
        return new Binder();
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//        Log.i(TAG, "onUnbind");
//        return false;
//    }

//    public class BackendServiceBinder extends Binder {
//        public BackendService getService() {
//            return BackendService.this;
//        }
//    }

//    public void setBackendServiceCallback(BackendServiceCallbackInterface backendServiceCallback) {
//        mCallback = backendServiceCallback;
//    }
//
//    public interface BackendServiceCallbackInterface {
//        void BackendServiceCallback();
//    }

    public static void startBackendService(Context context) {
        Intent intent = new Intent(context, BackendService.class);
//        intent.putExtra("command", Command.CMD_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }

//    public static void bindBackendService(Context context, ServiceConnection connection) {
//        Intent intent = new Intent(context, BackendService.class);
//        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
//    }

    public enum Command {
//        CMD_NONE,   // 0:总感觉如果没有个0撑着会出问题
//        CMD_START,
//        CMD_DESTROY,
//        CMD_CONNECT_DEVICE
    }

    // 暂时没有测试过，什么时候会解析失败
    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "Resolve failed: " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        // 在这获取目标IP与端口
        Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

        int port = serviceInfo.getPort();
        InetAddress host = serviceInfo.getHost();
    }

    public void test() {
        Log.i(TAG, "test");
    }


}