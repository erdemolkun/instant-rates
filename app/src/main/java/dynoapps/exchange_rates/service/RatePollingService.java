package dynoapps.exchange_rates.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import dynoapps.exchange_rates.LandingActivity;
import dynoapps.exchange_rates.ProvidersManager;
import dynoapps.exchange_rates.R;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.notification.NotificationHelper;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Erdem OLKUN , 05/12/2016
 */

public class RatePollingService extends IntentService {

    private final IBinder mBinder = new SimpleBinder();

    private static final int FOREGROUND_ID = 1001;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public RatePollingService() {
        super("RatePollingService");
    }

    public RatePollingService(String name) {
        super(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.i(RatePollingService.class.getSimpleName(), "Service onCreate");
        EventBus.getDefault().register(this);
        ProvidersManager.getInstance().initOrRefresh();
        Disposable disposable = ProvidersManager.getInstance().registerIntervalUpdates();
        compositeDisposable.add(disposable);
        compositeDisposable.add(SourcesManager.getSourceUpdates().observeOn(AndroidSchedulers.mainThread()).subscribe(__ -> {
            TimeIntervalManager.setAlarmMode(false);
            ProvidersManager.getInstance().refreshSources();
        }));
    }

    private boolean isOreoAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }


    @Override
    public void onDestroy() {
        if (isOreoAndAbove()) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        L.i(RatePollingService.class.getSimpleName(), "Service Stopped");
        ProvidersManager.getInstance().stopAll();
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ServiceStopActionReceiver.StopAction stopAction) {
        if (isOreoAndAbove()) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showForegroundNotification();
        return START_STICKY;
    }

    private void showForegroundNotification() {
        if (isOreoAndAbove()) {
            NotificationHelper.createConnectionChannelIfNotExists(this);

            Intent pushIntent = new Intent(this, LandingActivity.class);
            pushIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    pushIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            Intent stopActionIntent = new Intent(this, ServiceStopActionReceiver.class);
            PendingIntent stopActionPendingIntent = PendingIntent.getBroadcast(this, FOREGROUND_ID, stopActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(0, getString(R.string.notification_stop_action), stopActionPendingIntent)
                    .build();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.CHANNEL_ID_CONNECTION);
            builder.setContentIntent(pendingIntent);
            builder.setContentText(getApplicationContext().getString(R.string.background_working));
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
            builder.setWhen(0);
            builder.addAction(stopAction);
            builder.setSmallIcon(R.drawable.ic_store_icon_24dp);
            startForeground(FOREGROUND_ID, builder.build());
        }
    }

    public class SimpleBinder extends Binder {
        public RatePollingService getService() {
            return RatePollingService.this;
        }
    }
}
