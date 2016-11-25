package demoapps.exchangegraphics.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erdemmac on 25/11/2016.
 */

abstract class PoolingDataProvider<T> implements IRateProvider {

    private static final int INTERVAL = 2000;
    private static final int INTERVAL_ON_ERROR = 5000;

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
        getHandler().post(getWork());
        isWorking.set(true);
    }

    void fetchAgain(boolean wasError) {
        getHandler().postDelayed(getWork(), wasError ? INTERVAL_ON_ERROR : INTERVAL);
    }

    @Override
    public void stop() {
        Runnable runnable = getWork();
        if (runnable != null)
            getHandler().removeCallbacks(runnable);
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

    abstract Runnable getWork();
}
