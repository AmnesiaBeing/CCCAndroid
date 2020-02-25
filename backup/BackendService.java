package edu.cuc.ccc.backends;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import edu.cuc.ccc.helpers.BTHelper;
//import edu.cuc.ccc.helpers.NetworkHelper;
import edu.cuc.ccc.helpers.NotificationHelper;

public class BackendService extends Service {

    private static final String TAG = BackendService.class.getSimpleName();

    private static BackendService instance;

    public static BackendService getInstance() {
        return instance;
    }

    private NotificationHelper notificationHelper;


    private RPCHandler rpcHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public BackendService() {
        instance = this;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        notificationHelper = new NotificationHelper(this);
        notificationHelper.onCreate();

        rpcHandler = new RPCHandler();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        notificationHelper.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return new Binder();
    }

    public static void startBackendService(Context context) {
        Intent intent = new Intent(context, BackendService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }

    public RPCHandler getRpcHandler() {
        return rpcHandler;
    }
}