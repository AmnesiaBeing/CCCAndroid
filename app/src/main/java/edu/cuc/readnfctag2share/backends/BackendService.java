package edu.cuc.readnfctag2share.backends;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
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

public class BackendService extends Service {

    private static final String TAG = BackendService.class.getSimpleName();

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private NotificationHelper notificationHelper = new NotificationHelper(this);

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        notificationHelper.onCreate();

        HandlerThread thread = new HandlerThread("Service");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy() {
        notificationHelper.onDestroy();
    }

    private final IBinder binder = new BackendServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public class BackendServiceBinder extends Binder {
        public BackendService getService() {
            return BackendService.this;
        }
    }

    public void test() {
        Log.i(TAG, "test: aaaa");
    }


    public static void startBackendService(Context context) {
        Intent intent = new Intent(context, BackendService.class);
        context.startService(intent);
    }
}