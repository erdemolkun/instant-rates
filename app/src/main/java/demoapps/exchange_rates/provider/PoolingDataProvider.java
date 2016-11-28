package demoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erdemmac on 25/11/2016.
 */

abstract class PoolingDataProvider<T> implements IRateProvider, Runnable {

    private static final int INTERVAL = 4000;
    private static final int INTERVAL_ON_ERROR = 6000;

    private Callback callback;

    PoolingDataProvider(Callback<T> callback) {
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
        getHandler().postDelayed(this, wasError ? INTERVAL_ON_ERROR : INTERVAL);
    }

    @Override
    public void stop() {
        getHandler().removeCallbacks(this);
        cancel();
        isWorking.set(false);
    }

    void notifyValue(T value) {
        if (!isWorking.get()) return;
        if (callback != null) {
            callback.onResult(value);
        }
    }

    void notifyError() {
        if (callback != null) {
            callback.onError();
        }
    }
}
