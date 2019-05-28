package dynoapps.exchange_rates;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dynoapps.exchange_rates.event.IntervalUpdate;
import dynoapps.exchange_rates.service.RatePollingService;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import dynoapps.exchange_rates.util.ServiceUtils;

public abstract class BaseServiceActivity extends BaseActivity {

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

    abstract void onConnectionDone();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TimeIntervalManager.setAlarmMode(false);
        if (!ServiceUtils.isMyServiceRunning(this, RatePollingService.class)) {
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

    @Override
    protected void onDestroy() {
        if (ratePollingService != null) {
            unbindService(rateServiceConnection);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
