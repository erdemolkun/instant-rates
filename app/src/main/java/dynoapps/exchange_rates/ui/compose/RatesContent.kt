package dynoapps.exchange_rates.ui.compose

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// The scrims enableEdgeToEdge() uses by default behind a three-button navigation bar.
private val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

/**
 * Sets [content] in [RatesTheme] and keeps the system bars and window background in step with the
 * user's theme choice, also when it changes while the screen is open.
 */
fun ComponentActivity.setRatesContent(content: @Composable () -> Unit) {
    setContent {
        val dark = isAppInDarkTheme()
        LaunchedEffect(dark) { applyRatesWindowTheme(dark) }
        RatesTheme(darkTheme = dark, content = content)
    }
}

/**
 * Draws edge-to-edge with system bar icons that suit a [dark] or light app, and paints the window
 * background to match, so no light window shows behind a dark screen.
 */
fun ComponentActivity.applyRatesWindowTheme(dark: Boolean = ThemeSettings.isDark(resources)) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
        navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
    )
    window.setBackgroundDrawable(ColorDrawable(if (dark) Color.rgb(0x12, 0x12, 0x12) else Color.WHITE))
}
