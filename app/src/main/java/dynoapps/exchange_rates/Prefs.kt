package dynoapps.exchange_rates

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit

object Prefs {
    private const val SOURCES = "SOURCES"
    private const val INTERVAL = "INTERVAL"
    private const val ALARM_ENABLED = "ALARM_ENABLED"

    // Same file as before; the framework PreferenceManager is deprecated but avoids a new dependency.
    @Suppress("DEPRECATION")
    private fun prefs(context: Context = App.context()): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun isAlarmEnabled(): Boolean = prefs().getBoolean(ALARM_ENABLED, true)

    fun saveAlarmEnabled(enabled: Boolean) {
        prefs().edit { putBoolean(ALARM_ENABLED, enabled) }
    }

    fun getSources(): String? = prefs().getString(SOURCES, null)

    fun saveSources(sources: String?) {
        prefs().edit { putString(SOURCES, sources) }
    }

    fun getInterval(context: Context): Long = prefs(context).getLong(INTERVAL, -1)

    fun saveInterval(context: Context, interval: Long) {
        prefs(context).edit { putLong(INTERVAL, interval) }
    }
}
