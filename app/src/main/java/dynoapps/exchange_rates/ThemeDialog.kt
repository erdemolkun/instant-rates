package dynoapps.exchange_rates

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dynoapps.exchange_rates.ui.compose.InstantChoiceDialog
import dynoapps.exchange_rates.ui.compose.RatesTheme
import dynoapps.exchange_rates.ui.compose.ThemeMode
import dynoapps.exchange_rates.ui.compose.ThemeSettings
import dynoapps.exchange_rates.ui.compose.showComposeDialog

@get:StringRes
private val ThemeMode.label: Int
    get() = when (this) {
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.SYSTEM -> R.string.theme_system
    }

/** Shows [ThemeDialog]. Each choice is saved and restyles the whole app right away. */
fun ComponentActivity.selectTheme() {
    showComposeDialog { dismiss ->
        ThemeDialog(selected = ThemeSettings.mode, onSelect = ThemeSettings::update, onDismiss = dismiss)
    }
}

/**
 * Lets the user make the app light or dark, or follow the device setting. Tapping an option calls
 * [onSelect] at once; OK closes the dialog.
 */
@Composable
fun ThemeDialog(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    InstantChoiceDialog(
        title = stringResource(R.string.theme),
        items = ThemeMode.entries.map { stringResource(it.label) },
        selected = selected.ordinal,
        confirmText = stringResource(R.string.ok),
        onSelect = { index -> onSelect(ThemeMode.entries[index]) },
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
private fun ThemeDialogPreview() = RatesTheme {
    ThemeDialog(selected = ThemeMode.SYSTEM, onSelect = {}, onDismiss = {})
}
