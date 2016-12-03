package dynoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;

/**
 * Created by erdemmac on 25/11/2016.
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


    private AtomicBoolean isWorking = new AtomicBoolean(false);

    @Override
    public void run() {
        logDurationStart();
    }

    @Override
    public void start() {
        if (isWorking.get()) return;
        getHandler().post(this);
        isWorking.set(true);
    }

    void fetchAgain(boolean wasError) {
        int interval_value = TimeIntervalManager.getIntervalInMiliseconds();
        if (wasError) {
            /**
             * Calculate error interval in logarithmic.
             * */
            float ratio = (error_count / (float) (success_count <= 0 ? 1 : success_count));
            interval_value = (int) (NEXT_FETCH_ON_ERROR + Math.log(ratio) * NEXT_FETCH_ON_ERROR);
        }
        getHandler().postDelayed(this, interval_value);

    }

    public void refreshForIntervals() {
        getHandler().removeCallbacks(this);
        getHandler().postDelayed(this, TimeIntervalManager.getIntervalInMiliseconds());
    }

    @Override
    public void stop() {
        getHandler().removeCallbacks(this);
        cancel();
        isWorking.set(false);
    }


    private long last_call_start_milis = -1;
    private int average_duration = 0;

    private void logDurationSuccess() {
        if (last_call_start_milis < 0) return;
        long current_milis = System.currentTimeMillis();
        average_duration = (int)
                (average_duration * success_count + (current_milis - last_call_start_milis)) / (success_count + 1);
        if (this instanceof EnparaRateProvider) {
            L.d(this.getClass().getSimpleName() + "", "Current Diff : " + (current_milis - last_call_start_milis));
            L.e(this.getClass().getSimpleName() + "", "Average Duration :" + average_duration);
        }
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
