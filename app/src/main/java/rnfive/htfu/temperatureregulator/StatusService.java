package rnfive.htfu.temperatureregulator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import rnfive.htfu.temperatureregulator.define.UrlRunnable;
import rnfive.htfu.temperatureregulator.define.enums.Action;

import static androidx.core.app.NotificationCompat.FLAG_ONLY_ALERT_ONCE;
import static rnfive.htfu.temperatureregulator.define.enums.Action.*;

public class StatusService extends Service implements UrlListener{
    public static final String TAG = StatusService.class.getSimpleName();

    public static final String START_SERVICE = "rnfive.htfu.temperatureregulator.StatusService.START_SERVICE";
    public static final String STOP_SERVICE = "rnfive.htfu.temperatureregulator.StatusService.STOP_SERVICE";
    public static final String CHANNEL_ID = "STATUS_SERVICE_CHANNEL";

    public static final String BUNDLE_ID = "rnfive.htfu.temperatureregulator.StatusService.BUNDLE";
    public static final String INTENT_RESPONSE = "rnfive.djs.cyclingcomputer.StatusService.INTENT_RESPONSE";
    public static final String INTENT_ENDPOINT = "rnfive.djs.cyclingcomputer.StatusService.INTENT_ENDPOINT";

    private static boolean refreshRunning;
    private final Handler refreshHandler = new Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            getStatus();
            refreshHandler.postDelayed(this, 10000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        setNotificationMessage();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = null;
        if (intent != null)
            action = intent.getAction();
        if (action == null)
            action = "NONE";

        switch (action) {
            case START_SERVICE:
                broadcast("Service Started", null);
                refresh(START);
                break;
            case STOP_SERVICE:
                refresh(STOP);
                stopSelf();
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    private void refresh(Action action) {
        if (action == START) {
            if (!refreshRunning)
                refreshHandler.post(refreshRunnable);
            refreshRunning = true;
        } else if (action == STOP) {
            if (refreshRunning)
                refreshHandler.removeCallbacks(refreshRunnable);
            refreshRunning = false;
        }
    }

    private void getStatus() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new UrlRunnable(this, "get/status"));
    }

    private void broadcast(String response, @Nullable  String endpoint) {
        Intent intent = new Intent();
        intent.setAction(ServiceBroadcastReceiver.ACTION_STATUS);
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_RESPONSE, response);
        bundle.putString(INTENT_ENDPOINT, endpoint);
        intent.putExtra(BUNDLE_ID, bundle);
        sendBroadcast(intent);
    }

    void setNotificationMessage() {
        Intent notificationIntent = new Intent(this, StatusService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Title"); //detail mode is the "expanded" notification
        bigText.setBigContentTitle("Text");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("Text")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setStyle(bigText)
                .build();
        notification.flags = FLAG_ONLY_ALERT_ONCE;

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Recording Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        for (NotificationChannel n : manager.getNotificationChannels()) {
            Log.d(TAG, "Notification: " + n.getId());
        }
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onGetComplete(String response, String endpoint) {
        broadcast(response, endpoint);
    }

    @Override
    public void onDestroy() {
        refresh(STOP);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
