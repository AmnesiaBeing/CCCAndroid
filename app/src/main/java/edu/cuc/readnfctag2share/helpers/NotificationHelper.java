package edu.cuc.readnfctag2share.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import edu.cuc.readnfctag2share.R;
import edu.cuc.readnfctag2share.ui.MainActivity;

public class NotificationHelper {
    private static final String TAG = NotificationHelper.class.getSimpleName();

    private final Context mContext;
    private NotificationManager manager;

    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    private Notification notification;

    public NotificationHelper(Context context) {
        mContext = context;
    }

    public void onCreate() {
        manager = mContext.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(serviceChannel);
        }
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentText("任务等待中")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        ((Service) mContext).startForeground(1, notification);
    }

    public void onDestroy() {

    }
}
