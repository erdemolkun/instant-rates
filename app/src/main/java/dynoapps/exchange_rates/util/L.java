package dynoapps.exchange_rates.util;


import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import dynoapps.exchange_rates.App;


/**
 * Created by eolkun on 10/11/15.
 */
public class L {


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

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void d(String tag, String msg) {
        Logger.t(makeLogTag(tag)).d(msg);
    }

    public static void v(String tag, String msg) {
        Logger.t(makeLogTag(tag)).v(msg);
    }

    public static void e(String tag, String msg) {
        Logger.t(makeLogTag(tag)).e(msg);
    }

    public static void i(String tag, String msg) {
        Logger.t(makeLogTag(tag)).i(msg);
    }

    public static void wtf(String tag, String msg) {
        Logger.t(makeLogTag(tag)).wtf(msg);
    }

    public static void json(String tag, String json) {
        if (TextUtils.isEmpty(json)) return;
        if (json.length() < MAX_CHAR_LENGTH) {
            Logger.t(tag).json(json);
        } else {
            v(tag, json.substring(0, MAX_CHAR_LENGTH));
        }
    }

    public static void ex(Exception ex) {
        Logger.t("ExchangeRates").e(ex, "");
    }
    public static void ex(Exception ex,String message) {
        Logger.t("ExchangeRates").e(ex, message);
    }
}
