package dynoapps.exchange_rates;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseServiceActivity extends BaseActivity {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TimeIntervalManager.setAlarmMode(false);
        TimeIntervalManager.updateIntervalsToUIMode();// Intervals should be updated on ui mode.
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}
