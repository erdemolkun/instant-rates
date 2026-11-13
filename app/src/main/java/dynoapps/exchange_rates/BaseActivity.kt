package dynoapps.exchange_rates

import android.os.Bundle
import androidx.activity.ComponentActivity
import dynoapps.exchange_rates.ui.compose.applyRatesWindowTheme

/**
 * Base for the app's screens. Content is drawn edge-to-edge, with system bars and window background
 * matching the user's theme choice; each screen's Compose `Scaffold` keeps it clear of the bars.
 */
abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyRatesWindowTheme()
        super.onCreate(savedInstanceState)
    }
}
