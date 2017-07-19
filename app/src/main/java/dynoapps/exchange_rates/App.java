package dynoapps.exchange_rates;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.HashMap;

import dynoapps.exchange_rates.alarm.AlarmsRepository;


/**
 * Created by erdemmac on 21/10/2016.
 */

public class App extends Application {
    private static final String PROPERTY_ID = "UA-58111264-6";
    private static App appInstance;

    public static App getInstance() {
        return appInstance;
    }

    public static Context context() {
        if (appInstance == null) return null;
        return appInstance.getApplicationContext();
    }

    public AlarmsRepository provideAlarmsRepository() {
        return AlarmsRepository.getInstance(getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
//        if (PublishSettings.isAlphaOrDeveloper()) {
//            Stetho.initializeWithDefaults(this);
//        }

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

    private HashMap<TrackerName, Tracker> trackers = new HashMap<>();

    private synchronized Tracker getTracker(TrackerName trackerId) {
        if (!trackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            GoogleAnalytics.getInstance(this).setDryRun(false);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) :
                    (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) : analytics.newTracker(R.xml.global_tracker);
            trackers.put(trackerId, t);
        }
        return trackers.get(trackerId);
    }

    public void sendAnalyticsScreenName(Activity activity) {
        // Get tracker.
        Tracker tracker = getTracker(TrackerName.APP_TRACKER);

        if (tracker == null) return;

        String screenName = activity.getClass().getSimpleName();
        // Set screen name.
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendAnalyticsError(Exception ex, @NonNull String description, boolean isFatal) {
        // Get tracker.
        Tracker tracker = getTracker(TrackerName.APP_TRACKER);

        if (tracker == null) return;
        String desc_final = description;
        if (ex != null) {
            String exMessage = ex.getLocalizedMessage();
            if (!TextUtils.isEmpty(exMessage)) {
                desc_final += " : " + exMessage;
            }
        }
        tracker.send(new HitBuilders.ExceptionBuilder().setDescription(desc_final).setFatal(isFatal).build());
    }

    public void sendAnalyticsError(String description, boolean isFatal) {
        sendAnalyticsError(null, description, isFatal);
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
