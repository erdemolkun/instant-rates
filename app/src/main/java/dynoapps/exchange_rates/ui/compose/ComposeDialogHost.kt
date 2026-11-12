package dynoapps.exchange_rates.ui.compose

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

/**
 * Shows a Compose dialog or bottom sheet from View-based code.
 *
 * [content] is composed in an empty [ComposeView] added to the activity's content view; the
 * dialog itself draws in its own window. Calling the `dismiss` lambda passed to [content] removes
 * the view, which disposes the dialog. When [tag] is given, nothing is shown while a dialog with
 * the same tag is already showing.
 */
fun ComponentActivity.showComposeDialog(
    tag: String? = null,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    val root = findViewById<ViewGroup>(android.R.id.content)
    if (tag != null && root.findViewWithTag<ComposeView>(tag) != null) return

    val host = ComposeView(this)
    host.tag = tag
    var dismissed = false
    val dismiss = {
        if (!dismissed) {
            dismissed = true
            // Remove the host after the current event, not while Compose is dispatching it.
            root.post { root.removeView(host) }
        }
    }
    host.setContent { RatesTheme { content(dismiss) } }
    root.addView(host, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
}
