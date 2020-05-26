package dynoapps.exchange_rates.util;


import android.util.Log;


/**
 * Created by eolkun on 10/11/15.
 */
public class L {

    private static final String TAG = "ExchangeRates";

    private static final String LOG_PREFIX = "";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;
    private static final int MAX_CHAR_LENGTH = 320;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    public static void e(String tag, String msg) {
        Log.e(makeLogTag(tag), msg);
    }

    public static void e(String tag, Throwable th) {
        Log.e(tag, "", th);
    }

    public static void i(String tag, String msg) {
        Log.i(makeLogTag(tag), msg);
    }

    public static void ex(Exception ex) {
        Log.e(TAG, "", ex);
    }

    public static void ex(Exception ex, String message) {
        Log.e(TAG, message, ex);
    }
}
