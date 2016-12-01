package dynoapps.exchange_rates.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dynoapps.exchange_rates.App;

/**
 * Created by erdemmac on 26/11/15.
 */
public class AppUtils {


    public static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // Should not happen.
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getDeviceVersion() {
        return Build.VERSION.SDK_INT + "";
    }

    public static String getAppVersion() {
        Context context = App.context();
        PackageInfo pInfo;
        pInfo = getPackageInfo(context);
        return pInfo.versionName;
    }

    /**
     * Converts to http://semver.org/ standards.
     */
    public static String getAppVersionForSemver() {
        String appVersion = getAppVersion();
        return versionSemverCompat(appVersion);
    }


    public static String versionSemverCompat(String version) {
        if (TextUtils.isEmpty(version)) return "";
        else {
            String firstPart = version.split(" ")[0];
            String[] splittedSemVer = firstPart.split("\\.");
            ArrayList<String> parts = new ArrayList<>();
            Collections.addAll(parts, splittedSemVer);
            for (int i = parts.size(); i < 3; i++) {
                parts.add("0");
            }
            List<String> limitedParts = parts.subList(0, 3);
            return TextUtils.join(".", limitedParts);
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (TextUtils.isEmpty(s)) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
