package dynoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

import dynoapps.exchange_rates.time.TimeIntervalManager;

/**
 * Created by erdemmac on 25/11/2016. todo add is enabled state
 */

public abstract class BasePoolingDataProvider<T> implements IPollingSource, Runnable {

    private static final int NEXT_FETCH_ON_ERROR = 4000;

    private SourceCallback<T> callback;

    private int error_count = 0;
    private int success_count = 0;


    BasePoolingDataProvider(SourceCallback<T> callback) {
        this.callback = callback;
    }

    private Handler handler;

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    private AtomicBoolean is_enabled = new AtomicBoolean(false);
    private AtomicBoolean isWorking = new AtomicBoolean(false);

    @Override
    public void run() {
        logDurationStart();
    }

    @Override
    public void start() {
        is_enabled.set(true);
        if (isWorking.get())
        /**
         Working already. Has a handler callback.
         */
            return;
        postWork(this, 0);
    }

    void fetchAgain(boolean wasError) {
        long interval_value = TimeIntervalManager.getIntervalInMiliseconds();
        if (wasError) {
            /**
             * Calculate error interval in logarithmic.
             * */
            float ratio = (error_count / (float) (success_count <= 0 ? 1 : success_count));
            interval_value = (int) (NEXT_FETCH_ON_ERROR + Math.log(ratio) * NEXT_FETCH_ON_ERROR);
        }
        postWork(this, interval_value);

    }

    public void refreshForIntervals() {
        cancelWorks();
        if (!is_enabled.get()) {
            return;
        }
        postWork(this, TimeIntervalManager.getIntervalInMiliseconds());
    }

    @Override
    public void stop() {
        is_enabled.set(false);
        cancelWorks();
    }

    private void cancelWorks() {
        getHandler().removeCallbacks(this);
        cancel();
        isWorking.set(false);
    }

    private void postWork(Runnable runnable, long delayed) {
        isWorking.set(true);
        getHandler().postDelayed(runnable, delayed);
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
        if (!isWorking.get()) return;
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
}
