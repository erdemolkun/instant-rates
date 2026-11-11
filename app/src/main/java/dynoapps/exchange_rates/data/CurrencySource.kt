package dynoapps.exchange_rates.data

import androidx.annotation.ColorInt

class CurrencySource(
    val name: String,
    val type: Int,
    @ColorInt val color: Int,
    var isEnabled: Boolean,
    val supportedRates: IntArray?,
) {
    var chartIndex: Int = 0

    /** Sources without an explicit list support every rate type. */
    fun isRateSupported(rateType: Int): Boolean =
        supportedRates == null || supportedRates.isEmpty() || rateType in supportedRates

    override fun toString(): String = name
}
