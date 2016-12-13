package dynoapps.exchange_rates;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by erdemmac on 28/11/2016.
 */

public class Prefs {
    private static final String SOURCES = "SOURCES";
    private static final String INTERVAL = "INTERVAL";
    private static final String LANDING_HINT_STATE = "LANDING_HINT_STATE";
    private static final String ALARMS = "ALARMS";

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void saveSources(String sources) {
        saveString(SOURCES, sources);
    }

    public static String getSources() {
        return getPrefs(App.context()).getString(SOURCES, null);
    }


    public static void saveAlarms(String alarms_json) {
        saveString(ALARMS, alarms_json);
    }

    public static String getAlarms() {
        return getPrefs(App.context()).getString(ALARMS, null);
    }

    public static void saveInterval(Context context, long interval) {
        SharedPreferences preferences = getPrefs(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(INTERVAL, interval);
        editor.apply();
    }

    public static void saveLandingHintState(boolean closed) {
        saveBoolean(LANDING_HINT_STATE, closed);
    }

    public static boolean isLandingHintClosed() {
        return getPrefs(App.context()).getBoolean(LANDING_HINT_STATE, false);
    }

    public static long getInterval(Context context) {
        return getPrefs(context).getLong(INTERVAL, -1);
    }

    private static void saveString(String key, String value) {
        SharedPreferences preferences = getPrefs(App.context());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences preferences = getPrefs(App.context());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

}
