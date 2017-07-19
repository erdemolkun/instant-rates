package dynoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.atomic.AtomicBoolean;

import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmManager;
import dynoapps.exchange_rates.alarm.AlarmRepository;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.interfaces.PoolingRunnable;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by erdemmac on 25/11/2016.
 */

public abstract class BasePoolingProvider<T> implements IPollingSource, PoolingRunnable, Runnable {

    private static final int NEXT_FETCH_ON_ERROR = 4000;

    private static final int MESSAGE_WHAT_FETCH = 1;

    private SourceCallback<T> callback;

    protected CompositeDisposable compositeDisposable;

    private int error_count = 0;
    private int success_count = 0;

    private AtomicBoolean is_working = new AtomicBoolean(false); // Indicates if a job currently running
    private AtomicBoolean is_started = new AtomicBoolean(false); // Indicates if a job currently running

    private CurrencySource currencySource;

    BasePoolingProvider(SourceCallback<T> callback) {
        this.callback = callback;
        this.currencySource = SourcesManager.getSource(getSourceType());
        compositeDisposable = new CompositeDisposable();
    }

    private Handler handler;

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == MESSAGE_WHAT_FETCH) {
                        run();
                    }
                }
            };
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
    public void cancel() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
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
        postWork(0);
        is_started.set(true);
    }

    @Override
    public void stop() {
        L.i(BasePoolingProvider.class.getSimpleName(), this.getClass().getSimpleName() + " Stopped");
        cancelWorks();
        is_started.set(false);
    }

    void fetchAgain(boolean wasError) {
        if (!isEnabled() && !is_started.get()) return;
        long interval_value = TimeIntervalManager.getPollingInterval();
        if (wasError) {
            /**
             * Calculate error interval in logarithmic.
             * */
            float ratio = (error_count / (float) (success_count <= 0 ? 1 : success_count));
            interval_value = (int) (NEXT_FETCH_ON_ERROR + Math.log(ratio) * NEXT_FETCH_ON_ERROR);
        }
        postWork(interval_value);
    }

    public void refreshIntervals(boolean immediate_shot) {
        cancelWorks();
        if (!isEnabled()) {
            return;
        }
        postWork(immediate_shot ? 0 : TimeIntervalManager.getPollingInterval());
    }

    private void cancelWorks() {
        getHandler().removeMessages(MESSAGE_WHAT_FETCH);
        cancel();
        is_working.set(false);
    }

    private void postWork(long delayed) {
        is_working.set(true);
        getHandler().sendEmptyMessageDelayed(1, delayed);
        L.e(BasePoolingProvider.class.getSimpleName(), "- postWork : " + delayed + " ms - " + this.getClass().getSimpleName());
    }


    private long last_call_start_millis = -1;
    private int average_duration = 0;

    private void logDurationSuccess() {
        if (last_call_start_millis < 0) return;
        long current_milis = System.currentTimeMillis();
        average_duration = (int)
                (average_duration * success_count + (current_milis - last_call_start_millis)) / (success_count + 1);

    }

    private void logDurationStart() {
        last_call_start_millis = System.currentTimeMillis();
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
        last_call_start_millis = -1;
        error_count++;
        if (callback != null) {
            callback.onError();
        }
    }

    public boolean stopIfHasAlarm() {
        boolean contains = false;
        for (Alarm alarm : AlarmRepository.getInstance().getCachedAlarms()) {
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
