package dynoapps.exchange_rates.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import dynoapps.exchange_rates.LandingActivity;
import dynoapps.exchange_rates.ProvidersManager;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.notification.NotificationHelper;
import dynoapps.exchange_rates.util.L;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Foreground service that keeps the rate providers polling while the UI is closed so that
 * price alarms can still fire.
 * <p>
 * Created by Erdem OLKUN , 05/12/2016
 */
public class RatePollingService extends Service {

    private static final String TAG = "RatePollingService";

    private static final int FOREGROUND_ID = 1001;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.i(TAG, "Service onCreate");
        compositeDisposable.add(ServiceStopActionReceiver.getStopSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ignored -> stopPolling()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startForeground must be called promptly after startForegroundService on every start.
        showForegroundNotification();
        ProvidersManager.getInstance().startSources();
        return START_STICKY;
    }

    /**
     * Called on Android 15+ once the daily time limit for dataSync foreground services is used up.
     */
    @Override
    public void onTimeout(int startId, int fgsType) {
        L.i(TAG, "Foreground service timed out");
        stopPolling();
    }

    @Override
    public void onDestroy() {
        L.i(TAG, "onDestroy");
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void stopPolling() {
        ProvidersManager.getInstance().stopAll();
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void showForegroundNotification() {
        NotificationHelper.createConnectionChannelIfNotExists(this);

        Intent pushIntent = new Intent(this, LandingActivity.class);
        pushIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                pushIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopActionIntent = new Intent(this, ServiceStopActionReceiver.class);
        PendingIntent stopActionPendingIntent = PendingIntent.getBroadcast(this, FOREGROUND_ID,
                stopActionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(0,
                getString(R.string.notification_stop_action), stopActionPendingIntent)
                .build();

        Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                NotificationHelper.CHANNEL_ID_CONNECTION)
                .setContentIntent(pendingIntent)
                .setContentText(getString(R.string.background_working))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setWhen(0)
                .setOngoing(true)
                .addAction(stopAction)
                .setSmallIcon(R.drawable.ic_store_icon_24dp)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC : 0;
        ServiceCompat.startForeground(this, FOREGROUND_ID, notification, type);
    }
}
