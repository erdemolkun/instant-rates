package dynoapps.exchange_rates.util

import dynoapps.exchange_rates.App

object AppUtils {

    /** Version name without the build type suffix, e.g. "0.4.10" for "0.4.10 Debug". */
    fun getPlainVersion(): String {
        val context = App.context()
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        return versionName.orEmpty().split(" ")[0]
    }
}
