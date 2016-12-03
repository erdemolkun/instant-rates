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

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void saveSources(Context context, String sources) {
        SharedPreferences preferences = getPrefs(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SOURCES, sources);
        editor.apply();
    }

    public static String getSources(Context context) {
        return getPrefs(context).getString(SOURCES, null);
    }

    public static void saveInterval(Context context, long interval) {
        SharedPreferences preferences = getPrefs(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(INTERVAL, interval);
        editor.apply();
    }

    public static long getInterval(Context context) {
        return getPrefs(context).getLong(INTERVAL, -1);
    }
}
