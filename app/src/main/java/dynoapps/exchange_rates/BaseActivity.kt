package dynoapps.exchange_rates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

/**
 * Base for the app's screens. Content is drawn edge-to-edge; each screen's Compose `Scaffold` keeps
 * it clear of the system bars.
 */
abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }
}
