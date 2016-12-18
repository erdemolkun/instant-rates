package dynoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmManager;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.interfaces.PoolingRunnable;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 25/11/2016.
 */

public abstract class BasePoolingProvider<T> implements IPollingSource, PoolingRunnable, Runnable {

    private static final int NEXT_FETCH_ON_ERROR = 4000;

    private SourceCallback<T> callback;

    private int error_count = 0;
    private int success_count = 0;

    private AtomicBoolean is_working = new AtomicBoolean(false); // Indicates if a job currently running
    private AtomicBoolean is_started = new AtomicBoolean(false); // Indicates if a job currently running

    private CurrencySource currencySource;

    BasePoolingProvider(SourceCallback<T> callback) {
        this.callback = callback;
        this.currencySource = SourcesManager.getSource(getSourceType());
    }

    private Handler handler;

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    @Override
    public void one_shot() {
        run(true);
    }

    public boolean isEnabled() {
        return currencySource != null && currencySource.isEnabled();
    }

    public abstract int getSourceType();

    @Override
    public void run() {
        logDurationStart();
    }

    @Override
    public void start() {
        if (!isEnabled()) {
            /**
             * Double check :)
             * */
            return;
        }
        if (is_working.get()) {
            /**
             Working already. Has a handler callback.
             */
            return;
        }
        if (is_started.get()) {
            /**
             * No need to start again.
             * */
            return;
        }
        L.i(BasePoolingProvider.class.getSimpleName(), this.getClass().getSimpleName() + " Started");
        postWork(this, 0);
        is_started.set(true);
    }

    @Override
    public void stop() {
        L.i(BasePoolingProvider.class.getSimpleName(), this.getClass().getSimpleName() + " Stopped");
        cancelWorks();
        is_started.set(false);
    }

    void fetchAgain(boolean wasError) {
        if (!isEnabled()) return;
        long interval_value = TimeIntervalManager.getPollingInterval();
        if (wasError) {
            /**
             * Calculate error interval in logarithmic.
             * */
            float ratio = (error_count / (float) (success_count <= 0 ? 1 : success_count));
            interval_value = (int) (NEXT_FETCH_ON_ERROR + Math.log(ratio) * NEXT_FETCH_ON_ERROR);
        }
        postWork(this, interval_value);
    }

    public void refreshIntervals(boolean immediate_shot) {
        cancelWorks();
        if (!isEnabled()) {
            return;
        }
        postWork(this, immediate_shot ? 0 : TimeIntervalManager.getPollingInterval());
    }

    private void cancelWorks() {
        getHandler().removeCallbacks(this);
        cancel();
        is_working.set(false);
    }

    private void postWork(Runnable runnable, long delayed) {
        is_working.set(true);
        getHandler().postDelayed(runnable, delayed);
        L.e(BasePoolingProvider.class.getSimpleName(), "- postWork : " + delayed + " ms - " + this.getClass().getSimpleName());
    }


    private long last_call_start_milis = -1;
    private int average_duration = 0;

    private void logDurationSuccess() {
        if (last_call_start_milis < 0) return;
        long current_milis = System.currentTimeMillis();
        average_duration = (int)
                (average_duration * success_count + (current_milis - last_call_start_milis)) / (success_count + 1);

    }

    private void logDurationStart() {
        last_call_start_milis = System.currentTimeMillis();
    }

    void notifyValue(T value) {
        logDurationSuccess();
        success_count++;
        if (!is_working.get()) return;
        if (callback != null) {
            callback.onResult(value);
        }
    }

    void notifyError() {
        last_call_start_milis = -1;
        error_count++;
        if (callback != null) {
            callback.onError();
        }
    }

    public boolean stopIfHasAlarm() {
        boolean contains = false;
        for (Alarm alarm : AlarmManager.getAlarmsHolder().alarms) {
            if (alarm.source_type == getSourceType() && alarm.is_enabled) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            stop();
            return true;
        }
        return false;
    }

}
