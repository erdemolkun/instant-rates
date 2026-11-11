package dynoapps.exchange_rates.util

import android.text.InputFilter
import android.text.Spanned

/** Limits input to [decimalDigits] digits after the decimal separator ("." or ","). */
class DecimalDigitsInputFilter(private val decimalDigits: Int) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val dotPos = dest.indexOfFirst { it == '.' || it == ',' }
        if (dotPos >= 0) {
            // Only one separator is allowed.
            if (source.toString() == "." || source.toString() == ",") return ""
            // Text entered before the separator is always fine.
            if (dend <= dotPos) return null
            if (dest.length - dotPos > decimalDigits) return ""
        }
        return null
    }
}
