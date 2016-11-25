package demoapps.exchangegraphics.provider;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erdemmac on 25/11/2016.
 */

abstract class PoolingDataProvider<T> implements IRateProvider {

    static final int INTERVAL = 2000;
    static final int INTERVAL_ON_ERROR = 5000;

    private Callback callback;

    PoolingDataProvider(Callback<T> callback) {
        this.callback = callback;
    }

    private Handler handler;

    protected Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    private AtomicBoolean isWorking = new AtomicBoolean(false);
    private AtomicBoolean isStopped = new AtomicBoolean(false);

    @Override
    public void start() {
        if (isStopped.get()) return;
        if (isWorking.get()) return;
        getHandler().post(getWork());
    }

    @Override
    public void stop() {
        Runnable runnable = getWork();
        if (runnable != null)
            getHandler().removeCallbacks(runnable);

        isStopped.set(true);
    }

    void notifyValue(T rates) {
        if (isStopped.get()) return;
        if (callback != null) {
            callback.onResult(rates);
        }
    }

    void notifyError() {
        if (callback != null) {
            callback.onError();
        }
    }

    abstract Runnable getWork();
}
