package dynoapps.exchange_rates;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by erdemmac on 28/11/2016.
 */

public class Prefs {
    private static final String SOURCES = "SOURCES";

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
}
