package dynoapps.exchange_rates.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import dynoapps.exchange_rates.App

object ViewUtils {

    fun visibility(view: View?, isVisible: Boolean) {
        view?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun dpToPx(dp: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        App.context().resources.displayMetrics,
    ).toInt()

    fun calculateActionBarSize(context: Context?): Int {
        val theme = context?.theme ?: return 0
        val typedValue = TypedValue()
        if (!theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true)) {
            return 0
        }
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
    }
}
