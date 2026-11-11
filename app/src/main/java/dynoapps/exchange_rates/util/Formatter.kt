package dynoapps.exchange_rates.util

import java.text.DecimalFormat
import java.text.NumberFormat

class Formatter(digits: Int, minDigits: Int = maxOf(digits - 2, 0)) {

    private val numberFormat: NumberFormat = DecimalFormat.getInstance().apply {
        maximumFractionDigits = digits
        minimumFractionDigits = minDigits
    }

    fun format(value: Float): String = numberFormat.format(value.toDouble())
}
