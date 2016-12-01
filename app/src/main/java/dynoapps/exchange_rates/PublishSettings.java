package dynoapps.exchange_rates;

import android.content.Context;
import android.content.pm.PackageInfo;

import dynoapps.exchange_rates.util.AppUtils;


/**
 * Created by eolkun on 10.11.2015.
 */
public class PublishSettings {

    private static ReleaseTypes releaseType = null;

    public static ReleaseTypes getReleaseType() {
        // Lazy release type loading.
        if (releaseType == null)
            releaseType = getReleaseTypeByVersionName();
        return releaseType;
    }

    public static boolean isAlphaOrDeveloper() {
        return BuildConfig.DEBUG || getReleaseType() == ReleaseTypes.Alpha || getReleaseType() == ReleaseTypes.Debug;
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static boolean isNotReleaseType() {
        return getReleaseType() == ReleaseTypes.Alpha || getReleaseType() == ReleaseTypes.Beta || getReleaseType() == ReleaseTypes.Debug;
    }

    public static boolean isProd() {
        return getReleaseType() == ReleaseTypes.Prod;
    }

    public static boolean isAlpha() {
        return getReleaseType() == ReleaseTypes.Alpha;
    }

    public static boolean canUpdate() {
        return PublishSettings.getReleaseType() == PublishSettings.ReleaseTypes.Alpha
                || PublishSettings.getReleaseType() == PublishSettings.ReleaseTypes.Beta
                || PublishSettings.getReleaseType() == ReleaseTypes.Uat;

    }

    /**
     * Returns release type by extracting build type from version name. Build Type extracted from
     * application versionNameSuffix
     */
    private static ReleaseTypes getReleaseTypeByVersionName() {
        PackageInfo pInfo;
        Context context = App.context();
        if (context != null) {
            pInfo = AppUtils.getPackageInfo(context);
            if (pInfo.versionName.toLowerCase().contains("debug")) {
                return ReleaseTypes.Debug;
            } else if (pInfo.versionName.toLowerCase().contains("alpha")) {
                return ReleaseTypes.Alpha;
            } else if (pInfo.versionName.toLowerCase().contains("uat")) {
                return ReleaseTypes.Uat;
            } else if (pInfo.versionName.toLowerCase().contains("beta")) {
                return ReleaseTypes.Beta;
            }
        }
        return ReleaseTypes.Prod;
    }

    public enum ReleaseTypes {
        Debug,  // Only for developers
        Alpha,  //  Testers.
        Beta,   // Employees and Testers
        Uat,    // Prod release before published to store
        Prod    // This is for play store release.
    }
}
