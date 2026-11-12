package dynoapps.exchange_rates.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import dynoapps.exchange_rates.R

/**
 * Material 3 theme for the Compose UI, matching the View theme: the accent colour for actions
 * and selection controls, the primary colour (white at night) for icons, and neutral surfaces
 * instead of Material 3's purple-tinted defaults.
 */
@Composable
fun RatesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val accent = colorResource(R.color.colorAccent)
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
            secondary = colorResource(R.color.colorPrimary),
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
    MaterialTheme(colorScheme = colorScheme, content = content)
}
