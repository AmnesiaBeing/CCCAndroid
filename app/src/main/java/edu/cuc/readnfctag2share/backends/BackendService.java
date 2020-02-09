package edu.cuc.readnfctag2share.backends;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import edu.cuc.readnfctag2share.helpers.NotificationHelper;
import edu.cuc.readnfctag2share.ui.MainActivity;

// 后（前）台服务，感觉这个数据不好走啊，流程贼复杂
// Activity-->onCreate||onStart-->Helper-->bind||setCallback
// Activity-->Helper-->commands-->onStartCommand-->doSth-->Handler-->Callback-->Activity
public class BackendService extends Service {

    private static final String TAG = BackendService.class.getSimpleName();

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private NotificationHelper notificationHelper = new NotificationHelper(this);

    private BackendServiceCallbackInterface mCallback;

    // 感觉有点不太对，程序运行的模型暂时没有思考好
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage");
            if (mCallback != null) {
                mCallback.BackendServiceCallback();
            }
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String cmd = bundle.getString("command");
            Log.i(TAG, "onStartCommand: " + cmd);
            if (cmd.equals("destroy")) {

            } else {
                Message msg = serviceHandler.obtainMessage();
                msg.arg1 = startId;
                serviceHandler.sendMessage(msg);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        notificationHelper.onCreate();

        HandlerThread thread = new HandlerThread("Service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        notificationHelper.onDestroy();
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
        context.startForegroundService(intent);
    }

    public static void bindBackendService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, BackendService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
}