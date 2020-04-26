package dynoapps.exchange_rates;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import dynoapps.exchange_rates.util.ServiceUtils;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseServiceActivity extends BaseActivity {

    RatePollingService ratePollingService;

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

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

    abstract void onConnectionDone();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TimeIntervalManager.setAlarmMode(false);
        if (!ServiceUtils.isMyServiceRunning(this, RatePollingService.class)) {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            ContextCompat.startForegroundService(this, intent);
        } else {
            Intent intent = new Intent(this, RatePollingService.class);
            bindService(intent, rateServiceConnection, Context.BIND_AUTO_CREATE);
            SourcesManager.update();
            TimeIntervalManager.updateIntervalsToUIMode();// Intervals should be updated on ui mode.
        }
    }

    @Override
    protected void onDestroy() {

        if (ratePollingService != null) {
            unbindService(rateServiceConnection);
        }
        EventBus.getDefault().unregister(this);
        compositeDisposable.dispose();
        super.onDestroy();
    }

}
