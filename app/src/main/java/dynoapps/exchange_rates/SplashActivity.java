package dynoapps.exchange_rates;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import dynoapps.exchange_rates.event.IntervalUpdate;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.event.UpdateTriggerEvent;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 03/12/2016.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int MIN_DURATION = 450;

    Handler handler;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            gotoNextIntent();
        }
    };

    private long startMilis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        startMilis = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());
        EventBus.getDefault().register(this);
        TimeIntervalManager.setAlarmMode(false);
        if (!isMyServiceRunning(RatePollingService.class)) {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, RatePollingService.class));
        } else {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            SourcesManager.update();
            EventBus.getDefault().post(new IntervalUpdate(true)); // Intervals should be updated on ui mode.
        }
    }


    private void onConnectionDone() {
        long currentMilis = System.currentTimeMillis();
        if (currentMilis - startMilis < MIN_DURATION) {
            handler.postDelayed(runnable, Math.abs(MIN_DURATION - (currentMilis - startMilis)));
        } else {
            gotoNextIntent();
        }
    }

    RatePollingService ratePollingService;
    private ServiceConnection rateServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            L.i(LandingActivity.class.getSimpleName(), "onServiceConnected");
            TimeIntervalManager.setAlarmMode(false);
            ratePollingService = ((RatePollingService.SimpleBinder) binder).getService();
            onConnectionDone();
        }

        public void onServiceDisconnected(ComponentName className) {
            ratePollingService = null;
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RatesEvent ratesEvent) {

    }

    @Override
    public void onBackPressed() {
        clearEvents();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (ratePollingService != null) {
            unbindService(rateServiceConnection);
        }
        clearEvents();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void clearEvents() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    private void gotoNextIntent() {
        Intent i = new Intent(this, LandingActivity.class);
        startActivity(i);
        finish();
    }
}
