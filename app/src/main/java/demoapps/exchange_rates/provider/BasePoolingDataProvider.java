package demoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erdemmac on 25/11/2016.
 */

abstract class BasePoolingDataProvider<T> implements IPollingSource, Runnable {

    private static final int INTERVAL = 4000;
    private static final int INTERVAL_ON_ERROR = 3000;

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
    public void start() {
        if (isWorking.get()) return;
        getHandler().post(this);
        isWorking.set(true);
    }

    void fetchAgain(boolean wasError) {
        int interval = INTERVAL;
        if (wasError) {
            /**
             * Calculate error interval in logarithmic.
             * */
            float ratio = (float) (error_count / (success_count <= 0 ? 1 : success_count) * 1.0);
            interval = (int) (INTERVAL_ON_ERROR + Math.log10(ratio) * INTERVAL_ON_ERROR);
        }
        getHandler().postDelayed(this, interval);
    }

    @Override
    public void stop() {
        getHandler().removeCallbacks(this);
        cancel();
        isWorking.set(false);
    }

    void notifyValue(T value) {
        success_count++;
        if (!isWorking.get()) return;
        if (callback != null) {
            callback.onResult(value);
        }
    }

    void notifyError() {
        error_count++;
        if (callback != null) {
            callback.onError();
        }
    }
}
