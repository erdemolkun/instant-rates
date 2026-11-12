package dynoapps.exchange_rates.ui.compose

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalInspectionMode
import dynoapps.exchange_rates.Prefs

/** Whether the app is light, dark, or follows the device's dark theme setting. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * The user's theme choice, saved in [Prefs]. It is Compose state, so every screen and dialog
 * recomposes when it changes.
 */
object ThemeSettings {

    // Created on first use, so reading the mode inside a composition never writes state.
    private val state by lazy { mutableStateOf(load()) }

    val mode: ThemeMode
        get() = state.value

    fun update(mode: ThemeMode) {
        Prefs.saveThemeMode(mode.name)
        state.value = mode
    }

    /** Whether the app is dark under [mode], with [resources] giving the device setting. */
    fun isDark(resources: Resources, mode: ThemeMode = this.mode): Boolean = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM ->
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun load(): ThemeMode =
        Prefs.getThemeMode()?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } } ?: ThemeMode.SYSTEM
}

/** Whether the app is dark, following the user's [ThemeSettings] choice. Previews follow the device. */
@Composable
fun isAppInDarkTheme(): Boolean {
    val systemDark = isSystemInDarkTheme()
    if (LocalInspectionMode.current) return systemDark
    return when (ThemeSettings.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
}
