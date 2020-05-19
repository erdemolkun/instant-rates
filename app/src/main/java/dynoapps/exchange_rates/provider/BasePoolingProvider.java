package dynoapps.exchange_rates.provider;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.SourcesManager;
import dynoapps.exchange_rates.alarm.Alarm;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by erdemmac on 25/11/2016.
 */

public abstract class BasePoolingProvider<T> implements IPollingSource, Runnable {

    private static final int NEXT_FETCH_ON_ERROR = 4000;

    private static final int MESSAGE_WHAT_FETCH = 1;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final SourceCallback<T> callback;
    private final AlarmsRepository alarmsRepository;

    private int errorCount = 0;
    private int successCount = 0;

    private final AtomicBoolean isWorking = new AtomicBoolean(false); // Indicates if a job currently running
    private final AtomicBoolean isStarted = new AtomicBoolean(false); // Indicates if a job currently started

    private final CurrencySource currencySource;
    private Handler handler;
    private long last_call_start_millis = -1;
    private int average_duration = 0;

    BasePoolingProvider(SourceCallback<T> callback) {
        this.callback = callback;
        this.currencySource = SourcesManager.getSource(getSourceType());
        alarmsRepository = App.getInstance().provideAlarmsRepository();
    }

    protected abstract Observable<T> getObservable();

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
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
    public void oneShot() {
        job(true);
    }

    private boolean isEnabled() {
        return currencySource != null && currencySource.isEnabled();
    }

    public abstract int getSourceType();

    @Override
    public void run() {
        logDurationStart();
        job(false);
    }

    @Override
    public void cancel() {
        disposables.clear();
    }

    @Override
    public void start() {
        if (!isEnabled()) {
            /*
             * Double check :)
             * */
            return;
        }
        if (isWorking.get()) {
            /*
             Working already. Has a handler callback.
             */
            return;
        }
        if (isStarted.get()) {
            /*
             * No need to start again.
             * */
            return;
        }
        L.i(BasePoolingProvider.class.getSimpleName(), this.getClass().getSimpleName() + " Started");
        postWork(0);
        isStarted.set(true);
    }

    @Override
    public void stop() {
        L.i(BasePoolingProvider.class.getSimpleName(), this.getClass().getSimpleName() + " Stopped");
        cancelWorks();
        isStarted.set(false);
    }

    private void fetchAgain(boolean wasError) {
        if (!isEnabled() && !isStarted.get()) return;
        long interval_value = TimeIntervalManager.getPollingInterval();
        if (wasError) {
            /*
             * Calculate error interval in logarithmic.
             **/
            float ratio = (errorCount / (float) (successCount <= 0 ? 1 : successCount));
            interval_value = (int) (NEXT_FETCH_ON_ERROR + Math.log(ratio) * NEXT_FETCH_ON_ERROR);
        }
        postWork(interval_value);
    }

    @Override
    public void refreshIntervals(boolean immediateShort) {
        cancelWorks();
        if (!isEnabled()) {
            return;
        }
        postWork(immediateShort ? 0 : TimeIntervalManager.getPollingInterval());
    }

    private void cancelWorks() {
        getHandler().removeMessages(MESSAGE_WHAT_FETCH);
        cancel();
        isWorking.set(false);
    }

    private void postWork(long delayed) {
        isWorking.set(true);
        getHandler().sendEmptyMessageDelayed(1, delayed);
        L.i(BasePoolingProvider.class.getSimpleName(), "- postWork : " + delayed + " ms - " + this.getClass().getSimpleName());
    }

    private void logDurationSuccess() {
        if (last_call_start_millis < 0) return;
        average_duration = (int)
                (average_duration * successCount + (System.currentTimeMillis() - last_call_start_millis)) / (successCount + 1);

    }

    private void logDurationStart() {
        last_call_start_millis = System.currentTimeMillis();
    }

    private void notifyValue(T value) {
        logDurationSuccess();
        successCount++;
        if (!isWorking.get()) return;
        if (callback != null) {
            callback.onResult(value, getSourceType());
        }
    }

    private void notifyError() {
        last_call_start_millis = -1;
        errorCount++;
        if (callback != null) {
            callback.onError();
        }
    }

    @Override
    public void stopNonAlarmSources() {
        alarmsRepository.getAlarms(alarms -> {
            boolean contains = false;
            for (Alarm alarm : alarms) {
                if (alarm.sourceType == getSourceType() && alarm.isEnabled) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                stop();
            }
        });

    }

    private void job(final boolean singleRun) {

        disposables.add(Observable.defer(this::getObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rates -> {
                            notifyValue(rates);
                            if (!singleRun)
                                fetchAgain(false);
                        }
                        , th -> {
                            notifyError();
                            if (!singleRun)
                                fetchAgain(true);
                        }));

    }

}
