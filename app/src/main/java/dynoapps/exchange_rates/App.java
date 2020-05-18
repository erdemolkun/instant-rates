package dynoapps.exchange_rates;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.alarm.AlarmsRepository;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.L;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * Created by erdemmac on 21/10/2016.
 */

public class App extends Application {
    private static App appInstance;

    public static App getInstance() {
        return appInstance;
    }

    @NonNull
    public static Context context() {
        return appInstance.getApplicationContext();
    }

    public AlarmsRepository provideAlarmsRepository() {
        return AlarmsRepository.getInstance(getApplicationContext());
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;


        RxJavaPlugins.setErrorHandler(throwable -> L.e("App", throwable.getLocalizedMessage()));

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(2)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("ExchangeRates")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();


        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return PublishSettings.isAlphaOrDeveloper();
            }
        });

    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }


}
