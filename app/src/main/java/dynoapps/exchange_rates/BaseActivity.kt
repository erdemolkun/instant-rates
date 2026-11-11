package dynoapps.exchange_rates

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

abstract class BaseActivity : AppCompatActivity() {

    @get:LayoutRes
    abstract val layoutId: Int

    /** The toolbar from `toolbar_actionbar`, set as the support action bar on first access. */
    protected val actionBarToolbar: Toolbar? by lazy {
        findViewById<Toolbar?>(R.id.toolbar_actionbar)?.also { setSupportActionBar(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        applySystemBarInsets()
    }

    /**
     * Since targetSdk 35 activities are drawn edge-to-edge. Pad the content view by the system bar
     * and display cutout insets so the existing layouts keep clear of the status/navigation bars.
     */
    private fun applySystemBarInsets() {
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    protected fun setNavigationIcon(@DrawableRes resId: Int) {
        ContextCompat.getDrawable(this, resId)?.let(::setNavigationIcon)
    }

    /** Sets [drawable] as the toolbar's navigation icon, tinted with the theme's icon color. */
    protected fun setNavigationIcon(drawable: Drawable) {
        val attributes = theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarIconColor))
        val color = attributes.getColor(0, Color.WHITE)
        attributes.recycle()
        drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        actionBarToolbar?.navigationIcon = drawable
    }
}
