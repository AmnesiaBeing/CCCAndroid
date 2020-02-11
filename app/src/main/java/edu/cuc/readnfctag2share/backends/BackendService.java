package edu.cuc.readnfctag2share.backends;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import edu.cuc.readnfctag2share.R;
import edu.cuc.readnfctag2share.helpers.BTHelper;
import edu.cuc.readnfctag2share.helpers.NSDHelper;
import edu.cuc.readnfctag2share.helpers.NotificationHelper;
import edu.cuc.readnfctag2share.ui.MainActivity;

// 后（前）台服务，感觉这个数据不好走啊，流程贼复杂
// Activity-->onCreate||onStart-->Helper-->bind||setCallback
// Activity-->Helper-->commands-->onStartCommand-->doSth-->Handler-->Callback-->Activity
public class BackendService extends Service {

    private static final String TAG = BackendService.class.getSimpleName();

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private BackendServiceCallbackInterface mCallback;

    private NotificationHelper notificationHelper;
    private BTHelper btHelper;
    private NSDHelper nsdHelper;

    // 感觉有点不太对，程序运行的模型暂时没有思考好
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Command cmd = (Command) msg.obj;
            Log.i(TAG, "handleMessage" + cmd.name());
            switch (cmd) {
                case CMD_NONE:
                    break;
                case CMD_START:

                    break;
                case CMD_DESTROY:
                    break;
            }
            if (mCallback != null) {
                mCallback.BackendServiceCallback();
            }
        }
    }

    // startService(intent)-->here-->handleMessage
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Command cmd = (Command) intent.getSerializableExtra("command");
        Log.i(TAG, "onStartCommand:" + cmd.name());
        Message msg = new Message();
        msg.obj = cmd;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        notificationHelper = new NotificationHelper(this);
        notificationHelper.onCreate();

        btHelper = new BTHelper(this);
        btHelper.onCreate();

        nsdHelper = new NSDHelper(this);
        nsdHelper.initializeResolveListener();
        nsdHelper.initializeDiscoveryListener();
        nsdHelper.discoveryServices();

        HandlerThread thread = new HandlerThread("Service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        notificationHelper.onDestroy();
        btHelper.onDestroy();
        nsdHelper.onDestroy();
    }

    private final IBinder binder = new BackendServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return false;
    }

    public class BackendServiceBinder extends Binder {
        public BackendService getService() {
            return BackendService.this;
        }
    }


    public void setBackendServiceCallback(BackendServiceCallbackInterface backendServiceCallback) {
        mCallback = backendServiceCallback;
    }

    public interface BackendServiceCallbackInterface {
        void BackendServiceCallback();
    }

    public static void startBackendService(Context context) {
        Intent intent = new Intent(context, BackendService.class);
        intent.putExtra("command", Command.CMD_START);
        context.startForegroundService(intent);
    }

    public static void bindBackendService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, BackendService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public enum Command {
        CMD_NONE,   // 0:总感觉如果没有个0撑着会出问题
        CMD_START,
        CMD_DESTROY
    }
}