package dynoapps.exchange_rates.ui.compose

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dynoapps.exchange_rates.R

/** App colours that have no Material 3 role. */
@Immutable
data class RatesColors(
    /** Top and bottom app bars. */
    val bar: Color,
    val onBar: Color,
    /** Rate cards, alarm cards and the chart background. */
    val card: Color,
    /** Source names on rate cards. */
    val cardTitle: Color,
)

private val LocalRatesColors = staticCompositionLocalOf {
    RatesColors(bar = Color.Unspecified, onBar = Color.Unspecified, card = Color.Unspecified, cardTitle = Color.Unspecified)
}

/**
 * Material 3 theme for the Compose UI, dark or light as the user chose in [ThemeSettings]. Uses the
 * accent colour for actions and selection controls, the primary colour (white at night) for icons
 * and secondary text, and neutral surfaces instead of Material 3's purple-tinted defaults.
 */
@Composable
fun RatesTheme(darkTheme: Boolean = isAppInDarkTheme(), content: @Composable () -> Unit) {
    val colors = themedColors(darkTheme)
    val accent = colors(R.color.colorAccent)
    // colorPrimary has a night variant (#222222), so it also serves as the dark bar colour.
    val primary = colors(R.color.colorPrimary)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            secondary = Color.White,
            onSecondary = Color.Black,
            background = Color(0xFF121212),
            onBackground = Color(0xFFE6E6E6),
            surface = Color(0xFF121212),
            onSurface = Color(0xFFE6E6E6),
            onSurfaceVariant = Color(0xFFBDBDBD),
            surfaceContainerLowest = Color(0xFF0E0E0E),
            surfaceContainerLow = Color(0xFF1E1E1E),
            surfaceContainer = Color(0xFF232323),
            surfaceContainerHigh = Color(0xFF2C2C2C),
            surfaceContainerHighest = Color(0xFF353535),
            outline = Color(0xFF8E8E8E),
            outlineVariant = Color(0xFF424242),
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            secondary = primary,
            onSecondary = Color.White,
            background = Color.White,
            onBackground = Color(0xFF1C1C1C),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1C),
            onSurfaceVariant = Color(0xFF616161),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainer = Color(0xFFFAFAFA),
            surfaceContainerHigh = Color.White,
            surfaceContainerHighest = Color(0xFFF3F3F3),
            outline = Color(0xFF757575),
            outlineVariant = Color(0xFFBDBDBD),
        )
    }
    val ratesColors = RatesColors(
        bar = primary,
        onBar = Color.White,
        card = colors(R.color.card_background),
        cardTitle = primary,
    )
    CompositionLocalProvider(LocalRatesColors provides ratesColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

/** Access to the app colours of [RatesTheme], next to [MaterialTheme]. */
object RatesTheme {
    val colors: RatesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRatesColors.current
}

/**
 * Reads colours from colors.xml in light or dark, whichever [darkTheme] asks for, rather than
 * following the device setting as colorResource does.
 */
@Composable
private fun themedColors(darkTheme: Boolean): (Int) -> Color {
    val context = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val themedContext = remember(context, baseConfiguration, darkTheme) {
        val configuration = Configuration(baseConfiguration)
        val night = if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
        context.createConfigurationContext(configuration)
    }
    return { id -> Color(themedContext.getColor(id)) }
}
