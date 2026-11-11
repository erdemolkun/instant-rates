package dynoapps.exchange_rates.util

import android.text.InputFilter
import android.text.Spanned

/** Rejects input that would make the field's value fall outside [min]..[max]. */
class InputFilterMinMax(min: Double, max: Double) : InputFilter {

    private val range = minOf(min, max)..maxOf(min, max)

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val input = RateUtils.toFloat(dest.toString() + source.toString())
        return if (input != null && input.toDouble() in range) null else ""
    }
}
